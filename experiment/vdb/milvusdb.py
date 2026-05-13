import aiomysql
import asyncio
from pymilvus import MilvusClient


class MilvusDB:
    def __init__(self, uri, token):
        self.client = MilvusClient(
            uri=uri,
            token=token
        )

    def insert(self, collection_name, data):
        return self.client.insert(
            collection_name=collection_name,
            data=data
        )

    async def search(self, collection_name, data, filters):
        return self.client.search(
            collection_name=collection_name,
            data=data,
            limit=10,
            filter=filters,
            output_fields=["city", "state", "stars", "review", "favorites", "avgspend", "popular"]
        )

    async def list_collection(self):
        return self.client.list_collections()

    async def close(self):
        return self.client.close()
