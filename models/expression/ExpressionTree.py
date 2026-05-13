import json


class Expression:
    def __init__(self, table):
        self.tree = None
        self.table = table
        self.cardinality = -1

    def getTree(self):
        return self.tree

    def getTable(self):
        return self.table

    def getCardinality(self):
        return self.cardinality

    def setTree(self, tree):
        self.tree = tree

    def setCardinality(self, cardinality):
        self.cardinality = cardinality

    def setTable(self, table):
        self.table = table

    def structures(self):
        return json.dumps(self, default=lambda o: o.__dict__, sort_keys=True, indent=None)
