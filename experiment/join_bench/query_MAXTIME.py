from pathlib import Path
import time
from datetime import datetime
from join_bench.parse import parse_sql_file
from vdb.dingodb import DingoDBPymysql
from join_bench.config import dingo as db_config
import threading
from join_bench.utils import get_sql_files, natural_sort_key


def execute_sql_with_timeout(db_connection, sql, timeout):
    """
    带超时的 SQL 执行函数
    """
    result = {"success": False, "error": None}

    def target():
        try:
            db_connection.execute(sql)
            result["success"] = True
        except Exception as e:
            result["error"] = e

    thread = threading.Thread(target=target)
    thread.start()
    thread.join(timeout)  # 等待指定的超时时间

    if thread.is_alive():
        # 超时处理
        print(f"Execution of SQL timed out after {timeout} seconds.")
        thread.join()  # 确保线程结束
        return False, "Timeout"
    else:
        if result["success"]:
            return True, None
        else:
            return False, result["error"]




DIR = "job"
times = []
MAX_EXECUTION_TIME = 300
error_files = []
EXCLUDE = ["8c", "8d"]
START_FILE = "9a"


def bench(database_connection):
    current_directory = Path(__file__).resolve().parent / DIR
    sql_files = get_sql_files(current_directory)
    sql_files = sorted(sql_files, key=natural_sort_key)
    print(f"SQL Files: {sql_files}")

    # 获取当前时间戳并格式化为字符串
    current_time = datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
    log_file_name = f"execution_times_{current_time}.txt"

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
            # print(f"The parsed SQL is: {sql}")
            start_time = time.time()
            success, error = execute_sql_with_timeout(database_connection, sql, MAX_EXECUTION_TIME)
            end_time = time.time()
            if success:
                execution_time = end_time - start_time
                times.append(execution_time)
                log_file.write(f"{s_f}: {execution_time:.4f} seconds\n")
                print(f"Execution time for {s_f}: {execution_time:.4f} seconds")
            else:
                if error == "Timeout":
                    execution_time = MAX_EXECUTION_TIME
                    log_file.write(f"{s_f}: {execution_time:.4f} seconds\n")
                    times.append(execution_time)
                    print(f"Execution of {s_f} exceeded the maximum allowed time and was terminated.")
                else:
                    # execution_time = end_time - start_time
                    error_files.append(s_f)
                    print(f"Error executing {s_f}: {error}")
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
