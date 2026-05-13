# query
from pathlib import Path
import csv
from vdb.dingodb import DingoDBPymysql
from join_bench.config import dingo as db_config, dataset_base_path, dataset_limit, load_batch_size


def load(db_connection):
    file_names = list(dataset_limit.keys())
    for f_n in file_names:
        data_list = None
        file_path = Path(dataset_base_path) / (f_n + ".csv")
        with open(file_path, newline='') as csvfile:
            reader = csv.reader(csvfile)
            data_list = list(reader)

        print(data_list)


def bench():
    db = DingoDBPymysql(db_config["host"], db_config["port"], db_config["user"], db_config["password"],
                        db_config["database"])
    db.create_connection()
    load(db)
    db.close_connection()


bench()
