# VecCard: A Learned Cardinality Estimator for Predicate Filtering Queries on Vector Databases

**VecCard** is a learned cardinality estimator for predicate filtering workloads in vector databases. It uses a query-driven Tree-LSTM model to encode table metadata and nested Boolean predicates, predicts result cardinalities for hybrid vector queries, and supports an EWC-based incremental learning workflow so the estimator can adapt to new data and query patterns without full retraining. The overall architecture is shown in [Fig. 1](Fig%201.pdf), which illustrates how training data construction, cardinality prediction, and optimizer integration work together in the VecCard pipeline.

## Repository Layout

```text
learning-query-optimizer/
  models/        Tree-LSTM, Tree-GRU, Tree-RNN, MSCN, XGBoost and related model code
  preprocess/    Predicate extraction, metadata construction and vector preprocessing scripts
  experiment/    Python experiment runners for native, enhanced and distributed DingoDB setups
  generator/     Java workload, loading, explain-plan and metric utilities
```

## Requirements

Python 3.9+ is recommended for the Python components.

```bash
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

The Java utilities use Maven and Java 17.

```bash
cd generator
mvn -DskipTests compile
```

## Configuration

Runtime configuration is controlled through environment variables. Defaults are local-development values, so production hosts, credentials and file roots should be supplied through the environment instead of editing source files.

Common variables:

| Variable | Purpose | Default |
| --- | --- | --- |
| `VECCARD_DATA_DIR` | Shared data root for experiment and Java utilities | `./data` |
| `VECCARD_MODEL_DATA_DIR` | Training/evaluation files for Python models | `./models/data` |
| `VECCARD_WORD2VEC_DIR` | Word2Vec/FastText model directory | `./models/word2vec` |
| `VECCARD_MODEL_PATH_DIR` | Saved neural model directory | `./models/model_path` |
| `DINGODB_HOST` | DingoDB host for Python experiment runners | `127.0.0.1` |
| `DINGODB_PORT` | DingoDB MySQL-compatible port | `3307` |
| `DINGODB_USER` | DingoDB user | `root` |
| `DINGODB_PASSWORD` | DingoDB password | empty |
| `DINGODB_DATABASE` | DingoDB database name | `dingo` |
| `DINGODB_JDBC_URL` | DingoDB JDBC URL for Java utilities/tests | local DingoDB URL |
| `VECCARD_MYSQL_URL` | MySQL-compatible JDBC URL for explain/metrics utilities | local DingoDB URL |
| `VECCARD_CLICKHOUSE_URL` | ClickHouse JDBC URL for MyScale experiments | local ClickHouse URL |
| `VECCARD_PG_USER` / `VECCARD_PG_PASSWORD` | PostgreSQL credentials for pgvector utilities | local defaults |
| `POSTGRES_URL` | SQLAlchemy URL for preprocessing cardinalities | local PostgreSQL URL |
| `VECCARD_PROMETHEUS_URL` | Prometheus query-range endpoint for Java metrics | local endpoint |
| `VECCARD_PROM_INSTANCE` | Prometheus instance label | local instance |

Cloud object storage upload in `preprocess/cos.py` is also environment-driven:

```bash
export QCLOUD_SECRET_ID=...
export QCLOUD_SECRET_KEY=...
export QCLOUD_COS_BUCKET=...
export VECCARD_COS_FILE=data/dingo.zip
python preprocess/cos.py
```

## Data Layout

By default, experiment and Java helpers resolve runtime data from `VECCARD_DATA_DIR`. A typical layout is:

```text
data/
  dataset/
  predicate/
  sample/
  vector_dim/
```

Model-specific assets live under `models/` by default:

```text
models/
  data/
  model_path/
  word2vec/
```

All of these locations can be redirected with the environment variables listed above.

## Running Model Training

Run model scripts from the `models` directory so the local package imports resolve as intended.

```bash
cd models
python train/train_treelstm.py --retrain True
```

Other model trainers follow the same pattern:

```bash
python train/train_treegru.py --retrain True
python train/train_treernn.py --retrain True
python train/train_xgboost.py
```

Incremental learning examples are under `models/train/Incremental_learning/`:

```bash
python train/Incremental_learning/train_treelstm_ewc.py
```

## Running Experiments

Experiment configuration files are in `experiment/config/`. Database settings in those JSON files are local defaults and can be overridden with the `DINGODB_*` environment variables.

```bash
cd experiment
python main.py --config native --operation s
python main_enhance.py --config enhance --operation s
python main_distributed.py --config distributed --operation s
```

The operation flag is passed through the original experiment runner. Common values are:

| Operation | Meaning |
| --- | --- |
| `i` | Insert/load workload data |
| `s` | Run search/query workload |
| `r` | Restart configured server/container before the experiment |

## Java Utilities

The Java module contains loaders, metrics, explain-plan helpers and query examples:

```bash
cd generator
mvn -DskipTests package
```

Integration-style JUnit tests are opt-in. Enable them only when the target database is available:

```bash
export VECCARD_RUN_INTEGRATION_TESTS=true
export DINGODB_JDBC_URL=jdbc:dingo:thin:url=127.0.0.1:8765/dingo
mvn test
```
