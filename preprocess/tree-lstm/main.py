from utils import ParseSqlUtils
import pandas as pd
from sqlalchemy import create_engine
from sqlalchemy.orm import scoped_session, sessionmaker
import os
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[2]


class pg:
    def __init__(self):
        self.engine = create_engine(os.environ.get("POSTGRES_URL", "postgresql+psycopg2://postgres@localhost/yelp"))
        self.db = scoped_session(sessionmaker(bind=self.engine))

    def select(self, query):
        return self.db.execute(query).fetchall()


def read_csv(file_path):
    if not Path(file_path).exists():
        return pd.DataFrame()
    return pd.read_csv(file_path, header=None)


def main():
    db = pg()
    csv_file_path = os.environ.get("VECCARD_PREDICATE_CSV", str(REPO_ROOT / "data" / "predicate" / "douban_movie.csv"))
    csv_data = read_csv(csv_file_path)
    if csv_data.empty:
        return
    expressions = csv_data.iloc[:, 1]
    trees = []

    for i in range(0, len(expressions)):
        expression = expressions[i]
        try:
            tree = ParseSqlUtils.parseLogicalExpression(db, expression)
            trees.append(tree.structures())
        except Exception:
            trees.append(None)
            continue
        if i % 10 == 0:
            print(i)

    csv_data.columns = ['serial', 'expression']
    csv_data['cardinality'] = trees
    output_path = os.environ.get("VECCARD_CARDINALITY_CSV", str(REPO_ROOT / "data" / "predicate" / "douban_movie_cardinality.csv"))
    Path(output_path).parent.mkdir(parents=True, exist_ok=True)
    csv_data.to_csv(output_path, index=False)


if __name__ == '__main__':
    main()

# expression = "business.((city='Antioch' and state!='PA') and ((popular = TRUE and state!='IL' and stars!=2.0) or (review!=79 and favorites>=78 and avgspend<=41)))"
# tree = ParseSqlUtils.parseLogicalExpression(pg, expression)
# print(tree.structures())
