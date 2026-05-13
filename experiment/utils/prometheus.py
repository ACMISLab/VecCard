from collections import defaultdict
import os
import requests
import numpy as np
import paramiko
from utils import server


PROM_INSTANCE = os.environ.get("VECCARD_PROM_INSTANCE", "127.0.0.1:19256")
PROM_STORE_GROUP = os.environ.get("VECCARD_PROM_STORE_GROUP", "dingodb-store")
PROM_INDEX_GROUP = os.environ.get("VECCARD_PROM_INDEX_GROUP", "dingodb-index")
PROM_COORD_GROUP = os.environ.get("VECCARD_PROM_COORD_GROUP", "dingodb-coordinator")


def convert_to_dict(d):
    if isinstance(d, defaultdict):
        d = {k: convert_to_dict(v) for k, v in d.items()}
    return d


def get_prometheus_url(host, port):
    return "http://" + host + ":" + port + "/prometheus/api/v1/query_range"


def memory_query(group_name):
    swapped = (
        f'namedprocess_namegroup_memory_bytes{{groupname=~"{group_name}",'
        f'memtype="swapped",instance=~"{PROM_INSTANCE}"}}'
    )
    resident = (
        f'namedprocess_namegroup_memory_bytes{{groupname=~"{group_name}",'
        f'memtype="resident",instance=~"{PROM_INSTANCE}"}}'
    )
    return f'avg_over_time({swapped}[30s])+ ignoring (memtype) avg_over_time({resident}[30s])'


def get_queries():
    return {
        'cpu_utilization': '(100 - (avg by(instance) (rate(node_cpu_seconds_total{mode="idle"}[1m])) * 100))',
        'store_memory': memory_query(PROM_STORE_GROUP),
        'index_memory': memory_query(PROM_INDEX_GROUP),
        'coordinator_memory': memory_query(PROM_COORD_GROUP),

    }


def fetch_metrics(_start_time, _end_time, config):
    instance_data = defaultdict(lambda: defaultdict(dict))
    prometheus_config = config["prometheus"]
    prometheus_url = get_prometheus_url(prometheus_config["host"], prometheus_config["port"])
    for metric, query in get_queries().items():
        response = None
        try:
            # 查询Prometheus
            response = requests.get(prometheus_url, params={
                'query': query,
                'start': _start_time,
                'end': _end_time,
                'step': prometheus_config["step"]
            })
            response.raise_for_status()  # 检查HTTP请求的状态码
            data = response.json()
            # 检查响应数据
            if 'data' in data and 'result' in data['data']:
                for result in data['data']['result']:
                    # 对于时间序列值
                    for value in result['values']:
                        # 假设 value[0] 是从Prometheus获取的UTC时间戳
                        utc_timestamp = value[0]
                        instance_data[metric][utc_timestamp] = value[1]
            else:
                print(f"No data found for query: {query}")

        except requests.exceptions.RequestException as e:
            # 打印出完整的请求URL，便于调试
            print(f"An error occurred while querying Prometheus for metric '{metric}': {e}")
            if response is not None:
                print(f"Request URL: {response.request.url}")

    return instance_data


def calculation_memory(instance_data):
    store_memory = instance_data["store_memory"]
    index_memory = instance_data["index_memory"]
    coordinator_memory = instance_data["coordinator_memory"]
    s_m = []
    i_m = []
    c_m = []
    for timestamp, value in store_memory.items():
        s_m.append(float(value))
    for timestamp, value in index_memory.items():
        i_m.append(float(value))
    for timestamp, value in coordinator_memory.items():
        c_m.append(float(value))
    if not s_m or not i_m or not c_m:
        return
    s_m_max, s_m_min = np.array(s_m).max(), np.array(s_m).min()
    s_m_use = (s_m_max - s_m_min) / (1024 * 1024)
    i_m_max, i_m_min = np.array(i_m).max(), np.array(i_m).min()
    i_m_use = (i_m_max - i_m_min) / (1024 * 1024)
    c_m_max, c_m_min = np.array(c_m).max(), np.array(c_m).min()
    c_m_use = (c_m_max - c_m_min) / (1024 * 1024)
    all_m_use = s_m_use + i_m_use + c_m_use
    print(f"内存使用量：{all_m_use} Mb")


def calculation_cpu(instance_data):
    cpu_utilization = instance_data['cpu_utilization']
    c_u = []
    for timestamp, value in cpu_utilization.items():
        c_u.append(float(value))
    if not c_u:
        return
    c_u_max, c_u_min = np.array(c_u).max(), np.array(c_u).min()
    c_u_use = c_u_max - c_u_min
    print(f"CPU使用率：{c_u_use} %")


def get_metrics(_start_time, _end_time, config):
    instance_data = fetch_metrics(_start_time, _end_time, config)
    calculation_cpu(instance_data)
    calculation_memory(instance_data)
