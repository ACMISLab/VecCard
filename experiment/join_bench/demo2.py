from join_bench.config_local_vm import TABLE_CONFIGS
import json


print(TABLE_CONFIGS)

rows = {}

for table in TABLE_CONFIGS:
    rows[table["table_name"]] = int(table["row"])
print(rows)

output_file = "rows_local_vm.json"  # 输出文件名
with open(output_file, "w", encoding="utf-8") as json_file:
    json.dump(rows, json_file, indent=4)