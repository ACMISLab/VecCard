from ..utils import ParseSqlUtils
import pandas as pd
from sqlalchemy import create_engine
from sqlalchemy.orm import scoped_session, sessionmaker
import os
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[3]


class pg:
    def __init__(self):
        self.engine = create_engine(os.environ.get("POSTGRES_URL", "postgresql+psycopg2://postgres@localhost/yelp"))
        self.db = scoped_session(sessionmaker(bind=self.engine))

    def select(self, query):
        return self.db.execute(query).fetchall()


def read_csv(file_path):
    return pd.read_csv(file_path, header=None)


db = pg()
csv_file_path = Path(os.environ.get("VECCARD_PREDICATE_CSV", str(REPO_ROOT / "data" / "predicate" / "predicates_1.csv")))
if not csv_file_path.exists():
    raise SystemExit(0)
csv_data = read_csv(csv_file_path)
expressions = csv_data.iloc[:, 1]
trees = []

for i in range(0, len(expressions)):
    expression = expressions[i]
    try:
        tree = ParseSqlUtils.parseLogicalExpression(db, expression)
        trees.append(tree.structures())
    except Exception as e:
        tree = None
        trees.append(None)
        continue
    if i % 10 == 0:
        print(i)

csv_data.columns = ['SerialNumber', 'Expression']
csv_data['Cardinality'] = trees
output_path = Path(os.environ.get("VECCARD_CARDINALITY_CSV", str(REPO_ROOT / "data" / "predicate" / "predicates_cardinality_1.csv")))
output_path.parent.mkdir(parents=True, exist_ok=True)
csv_data.to_csv(output_path, index=False)
