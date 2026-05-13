import json
from . import Constants
from expression.ExpressionTree import Expression
from expression.TreeNode import LeafNode, Tree
import re
import regex
import numpy as np
import copy
from sklearn.preprocessing import MinMaxScaler
import pandas as pd


def parseLogicalExpression(db, expression):
    table, expr = expression.split('.', 1)
    expression = Expression(table)
    cardinality = calculateExpressionCardinality(db, table, expr)
    expression.setCardinality(cardinality)

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
    if topSplit[0].count("(") >0 and topSplit[0].count(")") > 0:
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


def calculateExpressionCardinality(db, table, expression):
    sql = "SELECT COUNT(*) FROM " + table + " WHERE " + expression
    cardinality = db.select(sql)
    return cardinality[0][0]


def computeOperatorNodeCardinality(db, table, expression):
    sql = "SELECT COUNT(*) FROM " + table + " WHERE " + expression
    cardinality = db.select(sql)
    return cardinality[0][0]


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


def parseComplexPredicate(predicates):
    pattern = r'\(([^()]+)\)'
    matches = re.findall(pattern, predicates)
    return matches


def encodePredicate(table, predicate, mapJson, df):
    operator = find_operator(predicate)
    column, value = predicate.strip().split(operator)
    column = column.strip()
    value = value.strip()
    value = value.replace("'", "")
    key = table + "." + column.strip()
    result = df[df['column'] == key.strip()]
    # 表名编码
    table_code = ontHotCode(table, Constants.TABLE_NAME, Constants.TABLE_NAME_ENCODE_LEN)

    # 列类型编码
    column_type = result['type'].values[0]
    column_type_code = ontHotCode(column_type, Constants.COLUMN_TYPE, Constants.COLUMN_TYPE_ENCODE_LEN)

    # 列metadata编码
    if column_type == "BOOL":
        column_metadata = result.iloc[:, 7:9].to_numpy()
        column_metadata_code = standardizeMetadataBoolean(column_metadata[0])
    else:
        column_metadata = result.iloc[:, 2:7].to_numpy()
        column_metadata_code = standardizeMetadata(column_metadata[0])

    # 列名编码
    column_name_code = ontHotCode(key, Constants.COLUMN_NAME, Constants.COLUMN_NAME_ENCODE_LEN)

    # 操作符编码
    operator_code = ontHotCode(operator, Constants.OPERATORS, Constants.OPERATORS_ENCODE_LEN)

    # 列值编码
    if column_type == "INT" or column_type == "FLOAT":
        min = result['min'].values[0]
        max = result['max'].values[0]
        column_value_code = normalizedMetadatNumerical(value, min, max, Constants.COLUMN_VALUE_ENCODE_LEN)
    elif column_type == "BOOL":
        if table == "problem":
            if value == "Chinese":
                b = True
            else:
                b = False
        else:
            if value == "TRUE":
                b = True
            else:
                b = False
        if b:
            b_pre = result['true_pre'].values[0]
        else:
            b_pre = result['false_pre'].values[0]
        column_value_code = boolTypeColValueEncode(b_pre, Constants.COLUMN_VALUE_ENCODE_LEN)
    else:
        column_value_code = ontHotCode(value, mapJson[key], Constants.COLUMN_VALUE_ENCODE_LEN)
    code = np.concatenate(
        (table_code, column_name_code, column_type_code, column_metadata_code, operator_code, column_value_code))
    return code


def find_operator(s):
    for operator in Constants.OPERATORS:
        if operator in s:
            return operator
    return None


def ontHotCode(value, values, length):
    values = [str(item) for item in values]
    one_hot = np.zeros(length, dtype=int)
    one_hot[values.index(value)] = 1
    return one_hot


# 归一化
def normalizedMetadata(vectorMetadata):
    data = np.array(vectorMetadata).reshape(-1, 1)
    data_min = np.min(data)
    data_max = np.max(data)
    normalized_data = (data - data_min) / (data_max - data_min)
    return normalized_data.flatten()


# 标准化
def standardizeMetadata(vectorMetadata):
    data = np.array(vectorMetadata).reshape(-1, 1)
    data_mean = np.mean(data)
    data_std = np.std(data)
    standardized_data = (data - data_mean) / data_std
    return standardized_data.flatten()


# 布尔类型标准化
def standardizeMetadataBoolean(vectorMetadata):
    data = np.array(vectorMetadata)
    data_pre = data / 100
    padding_size = (0, Constants.COLUMN_METADATA_ENCODE_LEN - len(data_pre))
    padded_result = np.pad(data_pre, pad_width=padding_size, mode='constant', constant_values=0)
    return padded_result


# 数值类型归一化
def normalizedMetadatNumerical(value, min_val, max_val, length):
    value = np.array(value, dtype=float)
    min_val = np.array([min_val], dtype=float)
    max_val = np.array([max_val], dtype=float)
    original_range = max_val - min_val
    normalized_value = (value - min_val) / original_range
    normalized_value = np.array(normalized_value)
    padding_size = (0, length - normalized_value.size)
    padded_result = np.pad(normalized_value, pad_width=padding_size, mode='constant', constant_values=0)
    return padded_result


def boolTypeColValueEncode(value, length):
    data = np.array([value])
    data_pre = data / 100
    padding_size = (0, length - len(data_pre))
    padded_result = np.pad(data_pre, pad_width=padding_size, mode='constant', constant_values=0)
    return padded_result
