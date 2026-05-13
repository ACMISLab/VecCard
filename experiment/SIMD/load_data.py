from SIMD.config import dingo as db_config
from SIMD.config import load_config
from pathlib import Path
import csv
from vdb.dingodb import DingoDBPymysql


def load_data(db):
    file_path = Path(load_config["base_path"]) / f"part_{load_config['file_index']}.csv"
    print("正在读取数据: ", file_path)

    with open(file_path, newline='', encoding='utf-8') as csvfile:
        reader = csv.reader(csvfile)
        next(reader)
        data_list = [((load_config["file_index"] - 1) * 100000 + i + 1, row[10]) for i, row in enumerate(reader) if
                     i >= load_config["row_index"]]

    insert_sql = "INSERT INTO review (id,feature) VALUES "
    load_batch_size = load_config["size"]
    start_index = (load_config["file_index"] - 1) * 100000 + load_config["row_index"]
    for i in range(0, len(data_list), load_batch_size):
        values = []
        current_batch = data_list[i:i + load_batch_size]
        for k, line_data in enumerate(current_batch):
            values.append(f"({line_data[0]}, ARRAY{line_data[1]})")
        batch_sql = insert_sql + ",".join(values)
        batch_start_index = start_index + i
        batch_end_index = batch_start_index + len(current_batch) - 1
        print(f"正在插入数据: {batch_start_index} ===========> {batch_end_index}")
        db.insert(batch_sql)


if __name__ == '__main__':
    db = DingoDBPymysql(db_config["host"], db_config["port"], db_config["user"], db_config["password"],
                        db_config["database"])
    db.create_connection()
    load_data(db)
    db.close_connection()
