import os
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[2]


def resolve_path(path_value, base_dir=None):
    if not path_value:
        return None
    expanded = Path(os.path.expandvars(str(path_value))).expanduser()
    if expanded.is_absolute():
        return expanded
    return (base_dir or REPO_ROOT).joinpath(expanded).resolve()


def data_path(*parts):
    data_root = Path(os.environ.get("VECCARD_DATA_DIR", REPO_ROOT / "data")).expanduser()
    return data_root.joinpath(*parts).resolve()


def apply_database_env(config):
    database = config.get("database", {})
    database["host"] = os.environ.get("DINGODB_HOST", database.get("host", "127.0.0.1"))
    database["port"] = int(os.environ.get("DINGODB_PORT", database.get("port", 3307)))
    database["user"] = os.environ.get("DINGODB_USER", database.get("user", "root"))
    database["password"] = os.environ.get("DINGODB_PASSWORD", database.get("password", ""))
    database["db"] = os.environ.get("DINGODB_DATABASE", database.get("db", "dingo"))

    insert = config.get("insert", {})
    if "filePath" in insert:
        insert["filePath"] = str(resolve_path(os.environ.get("VECCARD_LOAD_CSV", insert["filePath"])))
    return config
