import os
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[2]

dingo = {
    "host": os.environ.get("DINGODB_HOST", "127.0.0.1"),
    "port": int(os.environ.get("DINGODB_PORT", 3307)),
    "user": os.environ.get("DINGODB_USER", "root"),
    "password": os.environ.get("DINGODB_PASSWORD", ""),
    "database": os.environ.get("DINGODB_DATABASE", "dingo")
}

load_config = {
    "base_path": os.environ.get("VECCARD_SIMD_DATA_DIR", str(REPO_ROOT / "data" / "review")),
    "file_index": 1,
    "size": 500,
    "row_index": 0,
}

# Similarity = "L2"
Similarity = "IP"
# Similarity = "cos"


concurrency = 2
