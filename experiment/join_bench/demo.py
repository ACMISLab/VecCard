from pathlib import Path
import json
import os
from join_bench.query_MAXTIME import get_sql_files, natural_sort_key


current_directory = Path(__file__).resolve().parent / "job"
sql_files = get_sql_files(current_directory)
sql_files = sorted(sql_files, key=natural_sort_key)
print(f"SQL Files: {sql_files}")

times = {}
for s_f in sql_files:
    s_f = s_f.replace(".sql", "")
    times[s_f] = ""

print(times)

output_file = "bench.json"  # 输出文件名
with open(output_file, "w", encoding="utf-8") as json_file:
    json.dump(times, json_file, indent=4)