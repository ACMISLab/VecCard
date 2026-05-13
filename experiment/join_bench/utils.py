from pathlib import Path
import re


def get_sql_files(directory):
    path = Path(directory)  # 创建Path对象
    sql_files = [file.name for file in path.glob('*.sql')]  # 使用glob匹配.sql文件
    return sql_files


def natural_sort_key(s):
    """
    自然排序的键函数，用于将字符串中的数字部分按数值排序。
    """
    return [int(text) if text.isdigit() else text.lower() for text in re.split('([0-9]+)', s)]
