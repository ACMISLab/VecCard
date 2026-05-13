import os
import re

workload_num = 50
current_dir = os.getcwd()
log_files = [f for f in os.listdir(current_dir) if f.endswith('.log')]

# 遍历每个 .log 文件并逐行读取内容
for file_name in log_files:
    file_path = os.path.join(current_dir, file_name)  # 获取文件的完整路径
    print(f"读取文件: {file_name}")
    query_time_content = []
    metrics_content = []
    try:
        with open(file_path, 'r', encoding='utf-8') as file:  # 打开文件
            for line_number, line in enumerate(file, start=1):  # 逐行读取
                if line.startswith("查询执行时间"):
                    match = re.search(r"\d+\.\d+", line)  # 匹配浮点数
                    if match:
                        number = float(match.group())  # 提取匹配到的数字并转换为浮点数
                        query_time_content.append(number)
                elif line.startswith("metrics"):
                    match = re.search(r"\d+\.\d+", line)
                    if match:
                        number = float(match.group())
                        metrics_content.append(number)

        if len(metrics_content) == 0:
            # 计算查询执行时间的平均值
            success_rate = round(len(query_time_content) / workload_num, 4)
            failure_rate = round(1 - success_rate, 4)
            print(f"成功率: {success_rate}, 失败率: {failure_rate}")
            avg_query_time = round(sum(query_time_content) / len(query_time_content), 4)
            print(f"查询执行时间平均值: {avg_query_time}")
            QPS = round(1 / avg_query_time, 4)
            print(f"QPS: {QPS}")
            new_lines = [
                "\n",
                "\n",
                "metrics_成功率: {}\n".format(success_rate),
                "metrics_失败率: {}\n".format(failure_rate),
                "metrics_查询执行时间平均值: {} seconds\n".format(avg_query_time),
                "metrics_QPS: {} \n".format(QPS)
            ]
            with open(file_path, "a", encoding="utf-8") as file:
                file.writelines(new_lines)

    except Exception as e:
        print(f"读取文件 {file_name} 时出错: {e}")
    print("-" * 40)
