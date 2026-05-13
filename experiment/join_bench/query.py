from pathlib import Path
import time
from datetime import datetime
from join_bench.parse import parse_sql_file
from vdb.dingodb import DingoDBPymysql
from join_bench.config_local_vm import dingo as db_config
from join_bench.utils import get_sql_files, natural_sort_key


DIR = "join"
times = []
error_files = []
EXCLUDE = ["1", "5"]
START_FILE = "1"


def bench(database_connection):
    current_directory = Path(__file__).resolve().parent / DIR
    sql_files = get_sql_files(current_directory)
    sql_files = sorted(sql_files, key=natural_sort_key)
    print(f"SQL Files: {sql_files}")

    # 获取当前时间戳并格式化为字符串
    current_time = datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
    log_file_name = f"native_execution_times_{current_time}.txt"

    with open(log_file_name, "w", encoding="utf-8") as log_file:
        start_execution = False
        for s_f in sql_files:
            if s_f == START_FILE + ".sql":
                start_execution = True
            if not start_execution:
                print(f"Skipping {s_f} (before START_FILE)")
                continue
            if s_f.replace(".sql", "") in EXCLUDE:
                print(f"Skipping {s_f} (excluded)")
                continue
            print(f"Reading File: {s_f}")
            sql = parse_sql_file(Path(current_directory) / s_f)[0]
            start_time = time.time()
            try:
                database_connection.execute(sql)
                execution_time = time.time() - start_time
                times.append(execution_time)
                log_file.write(f"{s_f}: {execution_time:.4f} seconds\n")
                print(f"Execution time for {s_f}: {execution_time:.4f} seconds")
            except Exception as e:
                error_files.append(s_f)
                print(f"Error executing {s_f}: {e}")
                print("Skipping this SQL file and continuing...")
            print("------------------------------------------------------")
            log_file.flush()

        print("############## All files have been executed ##############")
        if error_files:
            print("The following SQL files encountered errors and were skipped:")
            print(error_files)
        else:
            print("All SQL files were executed successfully.")
        if times:
            average_time = sum(times) / len(times)
            log_file.write(f"Average execution time: {average_time:.4f} seconds\n")
            print(f"Average execution time: {average_time:.4f} seconds")
        else:
            print("No SQL files were executed successfully.")


if __name__ == '__main__':
    db = DingoDBPymysql(db_config["host"], db_config["port"], db_config["user"], db_config["password"],
                        db_config["database"])
    db.create_connection()
    bench(db)
    db.close_connection()