import os
import json
from pathlib import Path

import pandas as pd
from utils import ParseSqlUtils


REPO_ROOT = Path(__file__).resolve().parents[2]

def main():
    map_path = REPO_ROOT / "data" / "StringValueMap.json"
    metadata_path = REPO_ROOT / "data" / "metadata.csv"
    cardinality_path = Path(os.environ.get(
        "VECCARD_CARDINALITY_CSV",
        str(REPO_ROOT / "data" / "predicate" / "predicates_cardinality_all.csv"),
    ))
    if not map_path.exists() or not metadata_path.exists() or not cardinality_path.exists():
        return

    with open(map_path, 'r', encoding='utf-8') as f:
        map_json = json.load(f)

    df = pd.read_csv(metadata_path)
    crad = pd.read_csv(cardinality_path)
    head_cardinality = crad.head(10)["Cardinality"]
    for item in head_cardinality:
        tree_data = json.loads(item)
        tree = tree_data["root"]
        table = tree_data["table"]
        for leaf_node in tree["childLeafNode"]:
            predicate = leaf_node["predicate"]
            vector = ParseSqlUtils.encodePredicate(table, predicate, map_json, df)
            print(vector)


if __name__ == "__main__":
    main()
