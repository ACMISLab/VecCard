from gensim.models import FastText
import json
with open("../data/StringValueMap.json", 'r', encoding='utf-8') as f:
    mapJson = json.load(f)

cities = mapJson["business.city"]
model = FastText(cities, vector_size=128, window=3, min_count=1, sg=1)
model.save("fasttext_model.bin")
