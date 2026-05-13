from pathlib import Path
import csv
from vdb.dingodb import DingoDBPymysql
from join_bench.config import dingo as db_config, dataset_base_path,load_batch_size, TABLE_CONFIGS,exclude_table


def safe_convert_to_int(value):
    try:
        return int(value)
    except (ValueError, TypeError):
        return 0


def load_table(db_connection, table_config):
    """通用数据加载函数"""
    file_path = Path(dataset_base_path) / f"{table_config['table_name']}.csv"
    limit_row = table_config["row"]

    # 读取CSV数据
    print("正在读取数据: ", table_config['table_name'])
    csv.field_size_limit(1024 * 1024 * 1024)
    with open(file_path, newline='', encoding='utf-8') as csvfile:
        reader = csv.reader(csvfile)
        data_list = [row for i, row in enumerate(reader) if i < limit_row]

    # 数据转换
    print("正在转换数据: ", table_config['table_name'])
    converted_data = [table_config['convert_row'](row) for row in data_list]

    # 分批插入
    print("开始插入数据: ", table_config['table_name'])
    insert_sql = table_config['insert_sql']
    for i in range(0, len(converted_data), load_batch_size):
        batch = converted_data[i:i + load_batch_size]
        db_connection.batch_insert(insert_sql, batch)


if __name__ == '__main__':
    db = DingoDBPymysql(db_config["host"], db_config["port"], db_config["user"], db_config["password"],
                        db_config["database"])
    db.create_connection()

    for config in TABLE_CONFIGS:
        if config["table_name"] in exclude_table:
            continue
        else:
            print("正在加载表: ", config["table_name"])
            load_table(db, config)
            print("加载完成: ", config["table_name"])
            print("-----------------------------------------------")
    db.close_connection()
