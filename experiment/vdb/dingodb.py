import aiomysql
import asyncio
import pymysql


class DingoDB:
    def __init__(self, host, port, user, password, db):
        self.host = host
        self.port = port
        self.user = user
        self.password = password
        self.db = db
        self.conn = None
        self.cursor = None
        self.pool = None

    async def create_connection(self):
        self.conn = await aiomysql.connect(host=self.host, port=self.port, user=self.user, password=self.password,
                                           db=self.db, autocommit=True)
        self.cursor = await self.conn.cursor()

    async def create_pool(self):
        self.pool = await aiomysql.create_pool(host=self.host, port=self.port, user=self.user, password=self.password,
                                               db=self.db, minsize=1,
                                               maxsize=10,
                                               loop=asyncio.get_event_loop())

    async def close_connection(self):
        await self.cursor.close()
        self.conn.close()

    async def close_pool(self):
        self.pool.close()
        await self.pool.wait_closed()

    async def execute(self, query):
        await self.cursor.execute(query)
        return await self.cursor.fetchall()

    async def execute_pool(self, query):
        async with self.pool.acquire() as conn:
            async with conn.cursor() as cur:
                await cur.execute(query)
                return await cur.fetchall()


# 使用 pymysql 替换 aiomysql
class DingoDBPymysql:
    def __init__(self, host, port, user, password, db):
        self.host = host
        self.port = port
        self.user = user
        self.password = password
        self.db = db
        self.conn = None
        self.cursor = None

    def create_connection(self):
        self.conn = pymysql.connect(host=self.host, port=self.port, user=self.user, password=self.password,
                                    db=self.db, autocommit=True)
        self.cursor = self.conn.cursor()

    def close_connection(self):
        self.cursor.close()
        self.conn.close()

    # insert 函数 返回插入成功与失败
    def insert(self, query):
        try:
            self.cursor.execute(query)
            self.conn.commit()
            return True
        except Exception as e:
            print(e)
            return False

    def batch_insert(self, query, data):
        try:
            self.cursor.executemany(query, data)
            self.conn.commit()
            return True
        except Exception as e:
            print(f"{e}: {data}")
            return False

    def execute(self, query):
        self.cursor.execute(query)
        return self.cursor.fetchall()



