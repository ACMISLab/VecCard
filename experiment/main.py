import argparse
import json
from pathlib import Path
import dml
import ddl
from utils import server
from utils.paths import apply_database_env


def main():
    global args
    args = parse_args()
    config = get_config(args.config)
    match args.operation:
        case "c":
            ddl.create_table(config)
        case "d":
            ddl.delete_table(config)
        case "i":
            dml.load_csv_to_db(config, args)
        case "s":
            dml.select_workload(config, args)
        case "r":
            server.restart_server(config, args)
        case "t":
            dml.test_workload(config, args)
        case _:
            print("未设置operation")


def get_config(name):
    config_path = Path(__file__).resolve().parent / "config" / (name + ".json")
    with open(config_path, 'r', encoding='utf-8') as file:
        # 加载并解析 JSON 数据
        data = json.load(file)
    return apply_database_env(data)


def parse_args():
    parser = argparse.ArgumentParser(
        description='experiment args')
    parser.add_argument('--config', default='native',
                        help='config file name')
    # create drop insert select restart test
    # c d i s r t
    parser.add_argument('--operation', default='c',
                        help='operation name')
    experiment_args = parser.parse_args()
    return experiment_args


if __name__ == '__main__':
    main()
