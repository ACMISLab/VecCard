import csv
from pathlib import Path
from SIMD.config import load_config
import random


file_path = Path(load_config["base_path"]) / f"part_{load_config['file_index']}.csv"
print("正在读取数据: ", file_path)

with open(file_path, newline='', encoding='utf-8') as csvfile:
    reader = csv.reader(csvfile)
    next(reader)
    data_list = [row[10] for i, row in enumerate(reader) if i < 1000]

random_sample = random.sample(data_list, 100)

txt_file_path = "search_vector.txt"
with open(txt_file_path, "w", encoding="utf-8") as file:
    for item in random_sample:
        file.write(item + "\n")  # 每条数据后面添加换行符

print(f"数据已成功写入到文件 {txt_file_path}")

