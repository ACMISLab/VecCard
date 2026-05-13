import time
import os

import paramiko
import concurrent.futures

# 定义远程服务器集群的 IP 地址
servers = [
    os.environ.get("DINGODB_NODE_1", "127.0.0.1"),
    os.environ.get("DINGODB_NODE_2", "127.0.0.1"),
    os.environ.get("DINGODB_NODE_3", "127.0.0.1")
]

# 定义远程服务器的用户名和密码
username = os.environ.get("DINGODB_SSH_USER", "root")
password = os.environ.get("DINGODB_SSH_PASSWORD", "")


# 定义要执行的操作
def execute_command(server, command, time_interval):
    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    client.connect(server, username=username, password=password)
    client.exec_command(command)
    time.sleep(time_interval)


def process_node(server):
    print(f"开始处理节点: {server}")

    # 执行 command1
    print(f"正在执行 command1 on {server}")
    execute_command(server, "bash /root/scripts/restart-dingo-store.sh", 10)
    print(f"command1 on {server} 执行完成")

    # 执行 command2
    print(f"正在执行 command2 on {server}")
    execute_command(server, "bash /root/scripts/restart-dingo-exector.sh", 15)
    print(f"command2 on {server} 执行完成")

    print(f"节点 {server} 处理完成")
    print("------------------------")


# 使用线程池并发执行
with concurrent.futures.ThreadPoolExecutor(max_workers=len(servers)) as executor:
    executor.map(process_node, servers)
