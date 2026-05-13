import json
import numpy as np
import os
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[3]


# common function
def read_json(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        return json.load(f)


# business_json_file_path = "data/sample/business.json"
# business_json = read_json(business_json_file_path)
# business_city_list = []
# for i in range(0, len(business_json["city"])):
#     columnFrequency = business_json["city"][i]
#     if int(columnFrequency["num"]) >= 86:
#         business_city_list.append(columnFrequency)
#
# print(business_city_list)


json_file_path = Path(os.environ.get("VECCARD_SAMPLE_DIR", str(REPO_ROOT / "data" / "sample"))) / "problem.json"
if not json_file_path.exists():
    raise SystemExit(0)
json_data = read_json(json_file_path)
filed_name = "type"
minValue = 0
filed_cardinality = []
name =[]
for i in range(0, len(json_data[filed_name])):
    columnFrequency = json_data[filed_name][i]
    if int(columnFrequency["num"]) >= minValue:
        name.append(columnFrequency["name"])
        filed_cardinality.append(int(columnFrequency["num"]))

print(name)
print(filed_cardinality)
print(np.array(filed_cardinality).max())
print(np.array(filed_cardinality).min())
