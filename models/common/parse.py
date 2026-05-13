from expression import Constants
from expression.ExpressionTree import Expression
from expression.TreeNode import LeafNode, Tree
import regex


def parseLogicalExpression(expression):
    table, expr = expression.split('.', 1)
    expression = Expression(table)

    if (Constants.AND not in expr) and (Constants.OR not in expr):  # only one predicate
        leafNode = LeafNode(expr)
        tree = Tree('and')
        tree.setChildNode([leafNode])
        expression.setTree(tree)

    if count_and_or(expr) == 1:  # only one and/or operator
        operator = Constants.AND if Constants.AND in expr else Constants.OR
        if operator == Constants.AND:
            tree = Tree(operator.strip())
            leafNodes = expr.split(operator)
            tree.setChildNode(leafNodes)
            expression.setTree(tree)
        else:
            childTree = Tree(operator.strip())
            tree = Tree(Constants.AND.strip())
            leafNodes = expr.split(operator)
            childTree.setChildNode(leafNodes)
            tree.setChildTree([childTree])
            expression.setTree(tree)

    if count_and_or(expr) == 2:  # two and/or operators
        expr = expr[1:-1]
        leafNodes = expr.split(Constants.AND)
        tree = Tree(Constants.AND.strip())
        tree.setChildNode(leafNodes[0])
        childTree = Tree(Constants.OR.strip())
        childTree.setChildNode(leafNodes[1][1:-1].split(Constants.OR))
        tree.setChildTree([childTree])
        expression.setTree(tree)

    if count_and_or(expr) == 3:
        expr = expr[1:-1]
        if expr.startswith("("):
            leafNodes = splitNthOccurrence(expr, Constants.AND, 2)
            tree = Tree(Constants.AND.strip())
            tree.setChildNode(leafNodes[0][1:-1].split(Constants.AND))
            childTree = Tree(Constants.OR.strip())
            childTree.setChildNode(leafNodes[1][1:-1].split(Constants.OR))
            tree.setChildTree([childTree])
            expression.setTree(tree)
        else:
            leafNodes = splitNthOccurrence(expr, Constants.AND, 1)
            tree = Tree(Constants.AND.strip())
            tree.setChildNode([leafNodes[0]])
            expr_1 = leafNodes[1]
            leafNodes_1 = splitNthOccurrence(expr_1, Constants.OR, 1)
            childTree = Tree(Constants.OR.strip())
            childTree.setChildNode([leafNodes_1[1]])

            childChildTree = Tree(Constants.AND.strip())
            childChildTree.setChildNode(leafNodes_1[0][1:-1].split(Constants.AND))
            childTree.setChildTree([childChildTree])
            tree.setChildTree([childTree])
            expression.setTree(tree)

    if count_and_or(expr) > 3:
        tree = parseIntricatePredicate(expr)
        expression.setTree(tree)
    return expression


def parseIntricatePredicate(predicates):
    pattern = r'\((?:[^()]++|(?R))*\)'
    predicates = predicates[1:-1]
    topSplit = predicates.split("((")
    if topSplit[0].count("(") > 0 and topSplit[0].count(")") > 0:
        leftPredicates = regex.findall(pattern, topSplit[0])[0]
    else:
        leftPredicates = topSplit[0][:-5]
    rightPredicates = "((" + topSplit[1]
    rootTree = Tree(Constants.AND.strip())
    # 先检查左边的predicates
    if leftPredicates.startswith("("):
        leftPredicates = leftPredicates[1:-1]
    leftPredicateNodes = leftPredicates.split(Constants.AND)
    rootTree.setChildNode(leftPredicateNodes)
    # 在检查右边的predicates
    rightTree = Tree(Constants.OR.strip())
    if rightPredicates.startswith("("):
        rightPredicates = rightPredicates[1:-1]
    if rightPredicates.endswith(")"):
        rightSplit = rightPredicates.split(") or (")
    else:
        rightSplit = rightPredicates.split(") or ")

    right_left_predicates = rightSplit[0]
    right_right_predicates = rightSplit[1]

    if right_left_predicates.startswith("("):
        right_left_predicates = right_left_predicates[1:]

    if right_right_predicates.endswith(")"):
        right_right_predicates = right_right_predicates[:-1]

    if atomicPredicate(right_left_predicates):
        rightTree.addChildNode([right_left_predicates])

    if atomicPredicate(right_right_predicates):
        rightTree.addChildNode([right_right_predicates])

    rightTree_childTree = []
    if not atomicPredicate(right_left_predicates):
        right_left_tree = Tree(Constants.AND.strip())
        right_left_predicate_nodes = right_left_predicates.split(Constants.AND)
        right_left_tree.setChildNode(right_left_predicate_nodes)
        rightTree_childTree.append(right_left_tree)

    if not atomicPredicate(right_right_predicates):
        right_right_tree = Tree(Constants.AND.strip())
        right_right_predicate_nodes = right_right_predicates.split(Constants.AND)
        right_right_tree.setChildNode(right_right_predicate_nodes)
        rightTree_childTree.append(right_right_tree)

    rightTree.setChildTree(rightTree_childTree)
    rootTree.setChildTree([rightTree])

    return rootTree


def atomicPredicate(predicate):
    return count_and_or(predicate) == 0


def count_and_or(expr):
    and_count = expr.count(" and ")
    or_count = expr.count(" or ")
    return and_count + or_count


def splitNthOccurrence(s, delimiter, n):
    delimiterL = len(delimiter)
    start = 0
    occurrences = 0
    while occurrences < n:
        start = s.find(delimiter, start)
        if start == -1:  # 如果没有找到分隔符，返回原字符串
            return [s]
        start += len(delimiter)  # 更新搜索的起始位置
        occurrences += 1
    return [s[:start - delimiterL], s[start - delimiterL + len(delimiter):]]


