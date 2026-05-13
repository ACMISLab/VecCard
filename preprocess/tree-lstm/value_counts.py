import json

import numpy as np
import os
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[2]


def read_json(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        return json.load(f)


value_counts = {}
files = ['business', 'problem', 'review', 'tip', 'yelp_user', 'mcx_user']
for file in files:
    table = {}
    file_path = Path(os.environ.get("VECCARD_SAMPLE_DIR", str(REPO_ROOT / "data" / "sample"))) / (file + ".json")
    if not file_path.exists():
        continue
    json_data = read_json(file_path)
    col_keys = list(json_data.keys())
    col_list = []
    for col in col_keys:
        col_data = json_data[col]
        if file == 'business' and col == 'city':
            for o in col_data:
                if int(o["num"]) >= 86:
                    col_list.append({
                        "value": o["name"],
                        "count": o["num"]
                    })
                else:
                    continue
        elif file == 'business' and col == 'review':
            for o in col_data:
                if int(o["num"]) >= 36:
                    col_list.append({
                        "value": o["name"],
                        "count": o["num"]
                    })
                else:
                    continue
        elif file == 'business' and col == 'favorites':
            for o in col_data:
                if int(o["num"]) >= 580:
                    col_list.append({
                        "value": o["name"],
                        "count": o["num"]
                    })
                else:
                    continue
        else:
            for o in col_data:
                col_list.append({
                    "value": o["name"],
                    "count": o["num"]
                })
        table[col] = col_list
    value_counts[file] = table

output_path = Path(os.environ.get("VECCARD_VALUE_COUNT_JSON", str(REPO_ROOT / "data" / "sample" / "value_count.json")))
output_path.parent.mkdir(parents=True, exist_ok=True)
with open(output_path, 'w') as json_file:
    json.dump(value_counts, json_file)
