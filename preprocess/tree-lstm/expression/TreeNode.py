class LeafNode:
    def __init__(self, predicate):
        self.predicate = predicate

    def getPredicate(self):
        return self.predicate


class Tree:
    def __init__(self, operator):
        self.operator = operator
        self.childTree = []
        self.childNode = []

    def getOperator(self):
        return self.operator

    def getChildTree(self):
        return self.childTree

    def getChildNode(self):
        return self.childNode

    def setOperator(self, operator):
        self.operator = operator

    def setChildTree(self, childTree):
        self.childTree = childTree

    def setChildNode(self, childNode):
        self.childNode = childNode

    def addChildNode(self, childNode):
        for item in childNode:
            self.childNode.append(item)


