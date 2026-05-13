import os
from pathlib import Path

current_file = Path(__file__).resolve()
current_dir = current_file.parent
project_root = current_dir.parent
repo_root = project_root.parent


def _path_from_env(name, default):
    return Path(os.environ.get(name, default)).expanduser().resolve()


data_path = _path_from_env("VECCARD_MODEL_DATA_DIR", project_root / "data")
word2vec_path = _path_from_env("VECCARD_WORD2VEC_DIR", project_root / "word2vec")
model_path = _path_from_env("VECCARD_MODEL_PATH_DIR", project_root / "model_path")
