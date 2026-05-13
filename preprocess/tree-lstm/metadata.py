import json
import os
from pathlib import Path


def read_json(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        return json.load(f)


REPO_ROOT = Path(__file__).resolve().parents[2]
file = Path(os.environ.get("VECCARD_SAMPLE_DIR", str(REPO_ROOT / "data" / "sample"))) / "db_movie.json"
if not file.exists():
    raise SystemExit(0)

json_data = read_json(file)

key = "region"
json_list = json_data[key]
values = []
cards = []
for item in json_list:
    values.append(item["name"])
    cards.append(int(item["num"]))

# print("Values:", [min(values), max(values)])
print("Values:", values)
print("Cards:", [min(cards), max(cards)])


