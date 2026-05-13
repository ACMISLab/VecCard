from SIMD.config import dingo as db_config
import vdb.dingodb as dg
import asyncio
from dml import parallel_execute_query, calculate_metrics
from SIMD.config import Similarity, concurrency
import time


def getSymbol(sim):
    if sim == "L2":
        return "-"
    elif sim == "IP":
        return "*"
    elif sim == "cos":
        return "="


def getVectors():
    vectors = []
    file_path = "search_vector.txt"
    with open(file_path, "r", encoding="utf-8") as file:
        for line in file:
            vectors.append(line.strip())
    return vectors


def getSearchSql(vectors):
    search_sql = []
    for vector in vectors:
        similarity_operator = getSymbol(Similarity)
        query = (
            f"SELECT id, feature<{similarity_operator}>array{vector} AS distance "
            f"FROM review "
            f"ORDER BY distance DESC "
            f"LIMIT 10"
        )
        search_sql.append(query)
    return search_sql


async def bench():
    vectors = getVectors()
    search_sql = getSearchSql(vectors)
    dingodb = dg.DingoDB(db_config["host"], db_config["port"], db_config["user"], db_config["password"],
                         db_config["database"])

    await dingodb.create_pool()

    tasks = [parallel_execute_query(sql, dingodb, 180) for sql in search_sql]

    semaphore = asyncio.Semaphore(concurrency)

    task_start_time = time.time()

    async def run_task(task):
        async with semaphore:
            return await task

    results = await asyncio.gather(*(run_task(task) for task in tasks))
    calculate_metrics(results, task_start_time, 180)

    await dingodb.close_pool()

if __name__ == '__main__':
    asyncio.run(bench())
