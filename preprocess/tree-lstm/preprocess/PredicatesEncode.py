from ..utils import ParseSqlUtils
import pandas as pd
import json


with open("./data/StringValueMap.json", 'r', encoding='utf-8') as f:
    mapJson = json.load(f)

df = pd.read_csv("./data/metadata.csv")

predicate = "stars>=1.0"
vector = ParseSqlUtils.encodePredicate("problem", predicate, mapJson, df)
print(vector)
