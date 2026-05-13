import lancedb
import asyncio


class LanceDB:
    def __init__(self, uri, api_key, region):
        self.db = lancedb.connect(
            uri=uri,
            api_key=api_key,
            region=region
        )
        self.table = None

    def open_table(self, table_name):
        self.table = self.db.open_table(table_name)

    def table_count(self):
        return self.table.count_rows()

    async def search(self, data, filters):
        result = (
            self.table.search(data)
            .where(filters)
            .to_arrow()
        )
        return result

