import re


def parse_sql_file(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    # 移除注释（根据实际需要可调整）
    content = re.sub(r'--.*', '', content)  # 移除单行注释
    content = re.sub(r'/\*.*?\*/', '', content, flags=re.DOTALL)  # 移除多行注释
    statements = []
    for stmt in re.split(r';\s*', content):
        stmt = stmt.strip()
        if stmt:
            # 添加分号保持语句完整
            statements.append(stmt + ';')
    return statements


if __name__ == '__main__':
    sql_statements = parse_sql_file('imdb.sql')
    for i, stmt in enumerate(sql_statements, 1):
        print(f'Statement {i}:')
        print(stmt)
        print('-' * 80)
