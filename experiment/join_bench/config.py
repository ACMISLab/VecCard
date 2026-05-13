import os
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[2]

dingo = {
    "host": os.environ.get("DINGODB_HOST", "127.0.0.1"),
    "port": int(os.environ.get("DINGODB_PORT", 3307)),
    "user": os.environ.get("DINGODB_USER", "root"),
    "password": os.environ.get("DINGODB_PASSWORD", ""),
    "database": os.environ.get("DINGODB_DATABASE", "imdbload")
}

load_batch_size = 500

exclude_table = ["aka_name", "aka_title", "cast_info",
                 "char_name", "comp_cast_type", "company_name",
                 "company_type", "complete_cast", "info_type",
                 "keyword", "kind_type", "link_type", "movie_companies",
                 "movie_info_idx", "movie_keyword", "movie_link", "role_type",
                 "title", "name", "movie_info"]

# exclude_table = []

dataset_base_path = os.environ.get("IMDB_CSV_DIR", str(REPO_ROOT / "data" / "imdb" / "csv_files"))


def safe_convert_to_int(value):
    try:
        return int(value)
    except (ValueError, TypeError):
        return -1


def safe_convert_to_string(value):
    return str(value).replace("\'", "\"")


TABLE_CONFIGS = [
    {
        'table_name': 'aka_name',
        'row': 901343,
        'insert_sql': "INSERT INTO aka_name VALUES (%s, %s, %s, %s, %s, %s, %s,%s)",
        'convert_row': lambda row: (
            int(row[0]),
            int(row[1]),
            safe_convert_to_string(row[2]),
            safe_convert_to_string(row[3]),
            safe_convert_to_string(row[4]),
            safe_convert_to_string(row[5]),
            safe_convert_to_string(row[6]),
            safe_convert_to_string(row[7])
        )
    },
    {
        'table_name': 'aka_title',
        'row': 377960,
        'insert_sql': "INSERT INTO aka_title VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)",
        'convert_row': lambda row: (
            int(row[0]),
            int(row[1]),
            safe_convert_to_string(row[2]),
            safe_convert_to_string(row[3]),
            safe_convert_to_int(row[4]),
            safe_convert_to_int(row[5]),
            safe_convert_to_string(row[6]),
            safe_convert_to_int(row[7]),
            safe_convert_to_int(row[8]),
            safe_convert_to_int(row[9]),
            safe_convert_to_string(row[10]),
            safe_convert_to_string(row[11])
        )
    },
    {
        'table_name': 'cast_info',
        'row': 1244344,
        'insert_sql': "INSERT INTO cast_info VALUES (%s, %s, %s, %s, %s, %s, %s)",
        'convert_row': lambda row: (
            int(row[0]),
            int(row[1]),
            safe_convert_to_int(row[2]),
            safe_convert_to_int(row[3]),
            safe_convert_to_string(row[4]),
            safe_convert_to_int(row[5]),
            safe_convert_to_int(row[6])
        )
    },
    {
        'table_name': 'char_name',
        'row': 1140339,
        'insert_sql': "INSERT INTO char_name VALUES (%s, %s, %s, %s, %s, %s, %s)",
        'convert_row': lambda row: (
            int(row[0]),
            safe_convert_to_string(row[1]),
            safe_convert_to_string(row[2]),
            safe_convert_to_int(row[3]),
            safe_convert_to_string(row[4]),
            safe_convert_to_string(row[5]),
            safe_convert_to_string(row[6])
        )
    },
    {
        'table_name': 'comp_cast_type',
        'row': 4,
        'insert_sql': "INSERT INTO comp_cast_type VALUES (%s, %s)",
        'convert_row': lambda row: (
            int(row[0]),
            safe_convert_to_string(row[1])
        )
    },
    {
        'table_name': 'company_name',
        'row': 234997,
        'insert_sql': "INSERT INTO company_name VALUES (%s, %s, %s, %s, %s, %s, %s)",
        'convert_row': lambda row: (
            int(row[0]),
            safe_convert_to_string(row[1]),
            safe_convert_to_string(row[2]),
            safe_convert_to_int(row[3]),
            safe_convert_to_string(row[4]),
            safe_convert_to_string(row[5]),
            safe_convert_to_string(row[6])
        )
    },
    {
        'table_name': 'company_type',
        'row': 4,
        'insert_sql': "INSERT INTO company_type VALUES (%s, %s)",
        'convert_row': lambda row: (
            int(row[0]),
            safe_convert_to_string(row[1]),
        )
    },
    {
        'table_name': 'complete_cast',
        'row': 135086,
        'insert_sql': "INSERT INTO complete_cast VALUES (%s, %s, %s, %s)",
        'convert_row': lambda row: (
            int(row[0]),
            safe_convert_to_int(row[1]),
            safe_convert_to_int(row[2]),
            safe_convert_to_int(row[3])
        )
    },
    {
        'table_name': 'info_type',
        'row': 113,
        'insert_sql': "INSERT INTO info_type VALUES (%s, %s)",
        'convert_row': lambda row: (
            int(row[0]),
            safe_convert_to_string(row[1]),
        )
    },
    {
        'table_name': 'keyword',
        'row': 134170,
        'insert_sql': "INSERT INTO keyword VALUES (%s, %s, %s)",
        'convert_row': lambda row: (
            int(row[0]),
            safe_convert_to_string(row[1]),
            safe_convert_to_string(row[2])
        )
    },
    {
        'table_name': 'kind_type',
        'row': 7,
        'insert_sql': "INSERT INTO kind_type VALUES (%s, %s)",
        'convert_row': lambda row: (
            int(row[0]),
            safe_convert_to_string(row[1])
        )
    },
    {
        'table_name': 'link_type',
        'row': 18,
        'insert_sql': "INSERT INTO link_type VALUES (%s, %s)",
        'convert_row': lambda row: (
            int(row[0]),
            safe_convert_to_string(row[1])
        )
    },
    {
        'table_name': 'movie_companies',
        'row': 1609129,
        'insert_sql': "INSERT INTO movie_companies VALUES (%s, %s, %s, %s, %s)",
        'convert_row': lambda row: (
            int(row[0]),
            safe_convert_to_int(row[1]),
            safe_convert_to_int(row[2]),
            safe_convert_to_int(row[3]),
            safe_convert_to_string(row[4])
        )
    },
    {
        'table_name': 'movie_info_idx',
        'row': 1380035,
        'insert_sql': "INSERT INTO movie_info_idx VALUES (%s, %s, %s, %s, %s)",
        'convert_row': lambda row: (
            int(row[0]),
            safe_convert_to_int(row[1]),
            safe_convert_to_int(row[2]),
            safe_convert_to_string(row[3]),
            safe_convert_to_string(row[4])
        )
    },
    {
        'table_name': 'movie_keyword',
        'row': 1523930,
        'insert_sql': "INSERT INTO movie_keyword VALUES (%s, %s, %s)",
        'convert_row': lambda row: (
            int(row[0]),
            safe_convert_to_int(row[1]),
            safe_convert_to_int(row[2])
        )
    },
    {
        'table_name': 'movie_link',
        'row': 29997,
        'insert_sql': "INSERT INTO movie_link VALUES (%s, %s, %s, %s)",
        'convert_row': lambda row: (
            int(row[0]),
            safe_convert_to_int(row[1]),
            safe_convert_to_int(row[2]),
            safe_convert_to_int(row[3])
        )
    },
    {
        'table_name': 'name',
        'row': 1167491,
        'insert_sql': "INSERT INTO name VALUES (%s, %s, %s, %s, %s, %s, %s,%s, %s)",
        'convert_row': lambda row: (
            int(row[0]),
            safe_convert_to_string(row[1]),
            safe_convert_to_string(row[2]),
            safe_convert_to_int(row[3]),
            safe_convert_to_string(row[4]),
            safe_convert_to_string(row[5]),
            safe_convert_to_string(row[6]),
            safe_convert_to_string(row[7]),
            safe_convert_to_string(row[8])
        )
    },
    {
        'table_name': 'role_type',
        'row': 12,
        'insert_sql': "INSERT INTO role_type VALUES (%s, %s)",
        'convert_row': lambda row: (
            int(row[0]),
            safe_convert_to_string(row[1])
        )
    },
    {
        'table_name': 'title',
        'row': 1528312,
        'insert_sql': "INSERT INTO title VALUES (%s, %s, %s, %s, %s, %s, %s,%s, %s,%s,%s,%s)",
        'convert_row': lambda row: (
            int(row[0]),
            safe_convert_to_string(row[1]),
            safe_convert_to_string(row[2]),
            safe_convert_to_int(row[3]),
            safe_convert_to_int(row[4]),
            safe_convert_to_int(row[5]),
            safe_convert_to_string(row[6]),
            safe_convert_to_int(row[7]),
            safe_convert_to_int(row[8]),
            safe_convert_to_int(row[9]),
            safe_convert_to_string(row[10]),
            safe_convert_to_string(row[11])
        )
    },
    {
        'table_name': 'movie_info',
        'row': 1200000,
        'insert_sql': "INSERT INTO movie_info VALUES (%s, %s, %s, %s, %s)",
        'convert_row': lambda row: (
            int(row[0]),
            safe_convert_to_int(row[1]),
            safe_convert_to_int(row[2]),
            safe_convert_to_string(row[3]),
            safe_convert_to_string(row[4])
        )
    },
    {
        'table_name': 'person_info',
        'row': 963664,
        'insert_sql': "INSERT INTO person_info VALUES (%s, %s, %s, %s, %s)",
        'convert_row': lambda row: (
            int(row[0]),
            safe_convert_to_int(row[1]),
            safe_convert_to_int(row[2]),
            safe_convert_to_string(row[3]),
            safe_convert_to_string(row[4])
        )
    }
]
