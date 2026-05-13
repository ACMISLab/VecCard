import json
import os
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[3]


def read_json(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        return json.load(f)


file_list = ['business', 'problem', 'review', 'tip']

words = []
for file in file_list:
    file_path = Path(os.environ.get("VECCARD_SAMPLE_DIR", str(REPO_ROOT / "data" / "sample"))) / (file + ".json")
    if not file_path.exists():
        continue
    json_data = read_json(file_path)
    col_keys = list(json_data.keys())
    for col in col_keys:
        col_data = json_data[col]
        for o in col_data:
            words.append(str(o["name"]))

ex = ['=', '!=', ">", ">=", "<", "<=", '(', ')', 'city', 'state', 'popular', 'stars', 'review', 'favorites', 'avgspend',
      'popular', 'stars',
      'useful', 'funny', 'cool', 'likes', 'dislikes', 'views', 'read', 'compliment', 'amount', 'num', 'score', 'type',
      'language', 'TRUE', "FALSE"
      ]
for e in ex:
    words.append(e)
unique_words = list(set(words))

print(unique_words.__contains__('12.5'))

print(len(unique_words))

file_name = Path(os.environ.get("VECCARD_WORDS_FILE", str(Path(__file__).resolve().parent / "words.txt")))

# 打开文件进行写入操作
with open(file_name, 'w') as file:
    # 遍历列表中的每个元素
    for item in unique_words:
        # 将元素写入文件，并在末尾添加换行符
        file.write(item + '\n')
