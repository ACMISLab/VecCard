import shelve
from gensim.models import FastText
import json
import pandas as pd
import numpy as np
from common import FeatureData
import sys
import copy
from common import PATH


class BreakOuterLoops(Exception):
    pass


# ----------------- about sql ------------------------------


# ----------------------------------------------------------

def operatorEncode(operator):
    #  ["!=", ">=", ">", "<=", "<", "="]
    if operator == '!=' or operator == '<>':
        return np.array([1, 0, 0, 0, 0, 0])
    elif operator == '>=':
        return np.array([1, 1, 0, 0, 0, 0])
    elif operator == '>':
        return np.array([1, 1, 1, 0, 0, 0])
    elif operator == '<=':
        return np.array([1, 1, 1, 1, 0, 0])
    elif operator == '<':
        return np.array([1, 1, 1, 1, 1, 0])
    elif operator == '=':
        return np.array([1, 1, 1, 1, 1, 1])


def find_operator(s):
    for operator in ["!=", ">=", ">", "<=", "<", "="]:
        if operator in s:
            return operator
    return None


def equal_operator(s):
    for operator in ["!=", ">=", ">", "<=", "<", "="]:
        if operator == s:
            return True
    return False


def splitPredicates(predicates):
    vocabs = []
    words = predicates.split(" ")
    for word in words:
        # 先处理 左括号
        left_kh = word.count("(")
        left_replace_str = "(" * left_kh
        if left_kh != 0:
            for _ in range(left_kh):
                vocabs.append("(")
            # 第一次处理word
            word = word.replace(left_replace_str, "")
        #  处理操作符
        operator = find_operator(word)
        bool_operator = equal_operator(word)
        if not bool_operator:
            if operator is not None:
                pres = word.split(operator)
                suffix = pres[1]
                vocabs.append(pres[0])
                vocabs.append(operator)
                # 看看是否有右括号
                right_kh = suffix.count(")")
                right_replace_str = ")" * right_kh
                if right_kh != 0:
                    suffix = suffix.replace(right_replace_str, "")
                    vocabs.append(suffix)
                    for _ in range(right_kh):
                        vocabs.append(")")
                else:
                    vocabs.append(suffix)
            else:
                right_kh = word.count(")")
                right_replace_str = ")" * right_kh
                if right_kh != 0:
                    word = word.replace(right_replace_str, "")
                    vocabs.append(word)
                    for _ in range(right_kh):
                        vocabs.append(")")
                else:
                    vocabs.append

    return vocabs


def handleData(value, max_value, min_value, dim):
    if max_value - min_value == 0:
        max_value += 1e-10
    value = float(value)
    min_value = float(min_value)
    max_value = float(max_value)
    normalisation_value = (value - min_value) / (max_value - min_value)
    normalisation_vector = np.array([normalisation_value])
    padding_size = (0, dim - len(normalisation_vector))
    padded_vector = np.pad(normalisation_vector, pad_width=padding_size, mode='constant', constant_values=0)
    return padded_vector


def getMetadata():
    metadata = {}
    file_path = str(PATH.data_path / "metadata/metadata.db")
    with shelve.open(file_path) as db:
        metadata["tables"] = db["tables"]
        metadata["tables_row"] = db["tables_row"]
        metadata["columns"] = db["columns"]
    return metadata


def getIncreaseMetadata():
    metadata = {}
    file_path = str(PATH.data_path / "metadata/metadata-ewc.db")
    with shelve.open(file_path) as db:
        metadata["tables"] = db["tables"]
        metadata["tables_row"] = db["tables_row"]
        metadata["columns"] = db["columns"]
    return metadata


def getLargeBitmap():
    sample_bitmap = {}
    file_path = str(PATH.data_path / "bitmap/large_bitmap.db")
    with shelve.open(file_path) as db:
        sample_bitmap["sample_bitmap"] = db["sample_bitmap"]
    return sample_bitmap


def getSampleBitmap():
    sample_bitmap = {}
    file_path = str(PATH.data_path / "bitmap/sample_bitmap.db")
    with shelve.open(file_path) as db:
        sample_bitmap["sample_bitmap"] = db["sample_bitmap"]
    return sample_bitmap


def getDataFrame(dataset=None, head=None):
    if dataset is None:
        return pd.DataFrame()
    file_path = PATH.data_path / (dataset + ".csv")
    if not file_path.exists():
        return pd.DataFrame(columns=["expression", "cardinality", "feature"])
    df = pd.read_csv(file_path)
    if head is not None:
        df = df.head(head)
    return df


def get_column_type(metadata, table, predicate):
    predicate = predicate.replace("(", "").replace(")", "")
    operator = find_operator(predicate)
    column = predicate.strip().split(operator)[0]
    column = column.strip()
    columns = metadata["columns"]
    key = table + "." + column
    return columns[key]["type"]


def numeric_column(metadata, table, predicate):
    t = get_column_type(metadata, table, predicate)
    if t == 'FLOAT' or t == 'INT':
        return True
    else:
        return False


# table and it's metadata encode
def table_metadata_encode(metadata, table):
    tables = metadata['tables']
    tables_row = metadata['tables_row']
    log_transformed_data = np.log1p(tables_row)
    min_val = np.min(log_transformed_data)
    max_val = np.max(log_transformed_data)
    normalized_data = (log_transformed_data - min_val) / (max_val - min_val)
    table_vector = np.zeros(len(tables), dtype=int)
    table_vector[tables.index(table)] = 1
    table_vector = np.append(table_vector, normalized_data[tables.index(table)])
    return np.array(table_vector)


def column_name_encode(metadata, table, predicate):
    if isinstance(predicate, dict):
        predicate = predicate["predicate"]
    # dim = 21 or 29
    predicate = predicate.replace("(", "").replace(")", "")
    operator = find_operator(predicate)
    column = predicate.strip().split(operator)[0]
    column = column.strip()
    columns = metadata['columns']
    column_keys = list(columns.keys())
    column_index = column_keys.index(table + "." + column)
    column_name = column_keys[column_index]
    column_name_vector = np.zeros(len(column_keys), dtype=int)
    column_name_vector[column_keys.index(column_name)] = 1
    return np.array(column_name_vector)


def column_metadata_encode(metadata, table, predicate):
    if isinstance(predicate, dict):
        predicate = predicate["predicate"]
    # dim = 4+2 = 6
    predicate = predicate.replace("(", "").replace(")", "")
    operator = find_operator(predicate)
    column = predicate.strip().split(operator)[0]
    column = column.strip()
    column_types = ['VARCHAR', 'FLOAT', 'INT', 'BOOL']
    columns = metadata['columns']
    column_keys = list(columns.keys())
    column_index = column_keys.index(table + "." + column)
    column_name = column_keys[column_index]
    column_data = columns[column_name]
    column_type = column_data["type"]
    column_type_vector = np.zeros(4, dtype=int)
    column_type_vector[column_types.index(column_type)] = 1
    column_metadata_vector = np.concatenate(
        (column_type_vector, np.array([column_data["max_card"], column_data["min_card"]])))
    return column_metadata_vector


def bitmap_encode(sample_bitmap, table, predicate, dim):
    if isinstance(predicate, dict):
        predicate = predicate["predicate"]
    predicate = predicate.replace("(", "").replace(")", "")
    operator = find_operator(predicate)
    column = predicate.strip().split(operator)[0]
    column = column.strip()
    bitmap_data = sample_bitmap["sample_bitmap"]
    table_data = bitmap_data[table]
    if column in list(table_data.keys()):
        column_data = table_data[column]
        column_keys = list(column_data.keys())
        if predicate in column_keys:
            bitmap_vector = np.array(column_data[predicate])
        else:
            bitmap_vector = np.zeros(dim, dtype=int)
    else:
        bitmap_vector = np.zeros(dim, dtype=int)
    return bitmap_vector


def table_info_encode(table, metadata, samples, dim, col_num):
    tables = metadata['tables']
    tables_row = metadata['tables_row']
    log_transformed_data = np.log1p(tables_row)
    min_val = np.min(log_transformed_data)
    max_val = np.max(log_transformed_data)
    normalized_data = (log_transformed_data - min_val) / (max_val - min_val)
    table_vector = np.zeros(4, dtype=int)
    table_vector[tables.index(table)] = 1
    table_vector = np.append(table_vector, normalized_data[tables.index(table)])
    samples_vec = []
    sample_bitmap = samples["sample_bitmap"]
    bitmaps = sample_bitmap[table]
    col_keys = list(bitmaps.keys())
    for col in col_keys:
        col_bitmaps = bitmaps[col]
        for c in col_bitmaps:
            samples_vec.append(np.concatenate((table_vector, col_bitmaps[c])))

    diff = col_num - len(samples_vec)
    for _ in range(diff):
        samples_vec.append(np.concatenate((table_vector, np.zeros(dim, dtype=float))))
    return np.array(samples_vec)


# operator and  value encode
def predicate_encode(model, metadata, table, predicate):
    if isinstance(predicate, dict):
        predicate = predicate["predicate"]
    predicate = predicate.replace("(", "").replace(")", "")
    # 128 dim
    dim = 128
    operator = find_operator(predicate)
    operator_vector = operatorEncode(operator)
    column, value = predicate.strip().split(operator)
    column = column.strip()
    value = value.strip()
    value = value.replace("'", "")
    key = table + "." + column.strip()
    columns = metadata["columns"]
    column_data = columns[key]
    column_type = column_data["type"]
    if column_type == 'FLOAT' or column_type == 'INT':
        max_value = column_data["value"][1]
        min_value = column_data["value"][0]
        value_vector = handleData(value, max_value, min_value, dim)
    elif column_type == 'BOOL':
        true_pre = column_data["value"][0]
        false_pre = column_data["value"][1]
        temp_vector = np.array([true_pre, false_pre])
        padding_size = (0, dim - len(temp_vector))
        value_vector = np.pad(temp_vector, pad_width=padding_size, mode='constant', constant_values=0)
    else:
        if table == 'business' and column == 'city':
            value_vector = model.wv[value]
        elif table == 'problem' and column == 'score':
            value = float(value)
            metadata_value = column_data["value"]
            value_vector = np.zeros(dim, dtype=int)
            value_vector[metadata_value.index(value)] = 1
        elif table == 'problem' and column == 'type':
            value = int(value)
            metadata_value = column_data["value"]
            value_vector = np.zeros(dim, dtype=int)
            value_vector[metadata_value.index(value)] = 1
        else:
            metadata_value = column_data["value"]
            metadata_value = [str(item) for item in metadata_value]
            value_vector = np.zeros(dim, dtype=int)
            value_vector[metadata_value.index(value)] = 1
    predicate_vector = np.concatenate((operator_vector, value_vector))
    return predicate_vector


def column_predicate_encode(model, metadata, table, predicate):
    column_name_v = column_name_encode(metadata, table, predicate)
    predicate_v = predicate_encode(model, metadata, table, predicate)
    return np.concatenate((column_name_v, predicate_v))


def tree_data_encode(metadata, bitmap, model, table, predicate, args):
    table_v = table_metadata_encode(metadata, table)
    column_name_v = column_name_encode(metadata, table, predicate)
    column_metadata_v = column_metadata_encode(metadata, table, predicate)
    predicate_v = predicate_encode(model, metadata, table, predicate)
    if args.incre_bitmap:
        dim = 1000
    else:
        dim = 300
    if (not args.meta) and (not args.bitmap):
        return np.concatenate((column_name_v, predicate_v)).astype(np.float32)
    elif args.meta and (not args.bitmap):
        metadata_v = np.concatenate((table_v, column_name_v, column_metadata_v))
        return np.concatenate((metadata_v, predicate_v)).astype(np.float32)
    elif (not args.meta) and args.bitmap:
        bitmap_v = bitmap_encode(bitmap, table, predicate, dim)
        return np.concatenate((column_name_v, predicate_v, bitmap_v)).astype(np.float32)
    else:
        metadata_v = np.concatenate((table_v, column_name_v, column_metadata_v))
        bitmap_v = bitmap_encode(bitmap, table, predicate, dim)
        return np.concatenate((metadata_v, predicate_v, bitmap_v)).astype(np.float32)


def tree_data_encode_act(metadata, model, table, predicate):
    table_v = table_metadata_encode(metadata, table)
    column_name_v = column_name_encode(metadata, table, predicate)
    column_metadata_v = column_metadata_encode(metadata, table, predicate)
    predicate_v = predicate_encode(model, metadata, table, predicate)
    metadata_v = np.concatenate((table_v, column_name_v, column_metadata_v))
    return np.concatenate((metadata_v, predicate_v))


def range_encode(metadata, table, predicate):
    predicate = predicate.replace("(", "").replace(")", "")
    operator = find_operator(predicate)
    column, value = predicate.strip().split(operator)
    column = column.strip()
    value = value.strip()
    value = value.replace("'", "")
    value = float(value)
    key = table + "." + column.strip()
    columns = metadata["columns"]
    column_data = columns[key]
    return np.array([column_data["value"][0], value, column_data["value"][1]])


def SQLEncoding(metadata, word2vec_model, treeExpression):
    data = json.loads(treeExpression)
    table = data["table"]
    tree = data["tree"]
    child_node = tree["childNode"]
    root = FeatureData.Tree("and")
    root_child_node_vector = []
    root_child_tree_vector = []
    if isinstance(child_node, list):
        for predicate in child_node:
            vector = tree_data_encode_act(metadata, word2vec_model, table, predicate)
            root_child_node_vector.append(vector)
    else:
        vector = tree_data_encode_act(metadata, word2vec_model, table, child_node)
        root_child_node_vector.append(vector)
    root.setChildNode(root_child_node_vector)
    #  2th layer
    child_trees_2_layer = tree["childTree"]  # 获取子树
    for childTree_2 in child_trees_2_layer:  # 遍历子树
        tree_2_layer = FeatureData.Tree(childTree_2["operator"])
        tree_2_layer_child_node_vector = []
        tree_2_layer_child_tree_vector = []
        # 先寻找第3层树的信息
        child_trees_3_layer = childTree_2["childTree"]
        for childTree_3 in child_trees_3_layer:
            # 第三层没有下一层的子树
            tree_3_layer = FeatureData.Tree(childTree_3["operator"])
            tree_3_layer_child_node_vector = []
            for node in childTree_3["childNode"]:
                vector = tree_data_encode_act(metadata, word2vec_model, table, node)
                tree_3_layer_child_node_vector.append(vector)
            tree_3_layer.setChildNode(tree_3_layer_child_node_vector)
            tree_2_layer_child_tree_vector.append(tree_3_layer)
        tree_2_layer.setChildTree(tree_2_layer_child_tree_vector)
        # 再寻找第2层自己的子节点
        for node in childTree_2["childNode"]:
            vector = tree_data_encode_act(metadata, word2vec_model, table, node)
            tree_2_layer_child_node_vector.append(vector)
        tree_2_layer.setChildNode(tree_2_layer_child_node_vector)
        root_child_tree_vector.append(tree_2_layer)
    root.setChildTree(root_child_tree_vector)
    return root


def loadTreeData(dataset=None, head=None, args=None):
    trees = []
    labels = []
    df = getDataFrame(dataset, head)
    if args.incre_meta:
        metadata = getIncreaseMetadata()
    else:
        metadata = getMetadata()
    if args.incre_bitmap:
        bitmap = getLargeBitmap()
    else:
        bitmap = getSampleBitmap()

    file_path = str(PATH.word2vec_path / "fasttext_model.bin")
    model = FastText.load(file_path)
    df_cardinality = df["cardinality"]
    for i in range(0, len(df_cardinality)):
        try:
            data = json.loads(df_cardinality[i])
            table = data["table"]
            tree = data["tree"]
            child_node = tree["childNode"]
            root = FeatureData.Tree("and")
            root_child_node_vector = []
            root_child_tree_vector = []
            if isinstance(child_node, list):
                for predicate in child_node:
                    vector = tree_data_encode(metadata, bitmap, model, table, predicate, args)
                    root_child_node_vector.append(vector)
            else:
                vector = tree_data_encode(metadata, bitmap, model, table, child_node, args)
                root_child_node_vector.append(vector)
            root.setChildNode(root_child_node_vector)
            #  2th layer
            child_trees_2_layer = tree["childTree"]  # 获取子树
            for childTree_2 in child_trees_2_layer:  # 遍历子树
                tree_2_layer = FeatureData.Tree(childTree_2["operator"])
                tree_2_layer_child_node_vector = []
                tree_2_layer_child_tree_vector = []
                # 先寻找第3层树的信息
                child_trees_3_layer = childTree_2["childTree"]
                for childTree_3 in child_trees_3_layer:
                    # 第三层没有下一层的子树
                    tree_3_layer = FeatureData.Tree(childTree_3["operator"])
                    tree_3_layer_child_node_vector = []
                    for node in childTree_3["childNode"]:
                        vector = tree_data_encode(metadata, bitmap, model, table, node, args)
                        tree_3_layer_child_node_vector.append(vector)
                    tree_3_layer.setChildNode(tree_3_layer_child_node_vector)
                    tree_2_layer_child_tree_vector.append(tree_3_layer)
                tree_2_layer.setChildTree(tree_2_layer_child_tree_vector)
                # 再寻找第2层自己的子节点
                for node in childTree_2["childNode"]:
                    vector = tree_data_encode(metadata, bitmap, model, table, node, args)
                    tree_2_layer_child_node_vector.append(vector)
                tree_2_layer.setChildNode(tree_2_layer_child_node_vector)
                root_child_tree_vector.append(tree_2_layer)
            root.setChildTree(root_child_tree_vector)
            trees.append(root)
            labels.append(data["cardinality"])
        except Exception as e:
            pass
    return trees, labels


#  for mscn
def loadMSCNData(dataset=None, head=None, incr=False):
    labels = []
    df = getDataFrame(dataset, head)
    if incr:
        metadata = getIncreaseMetadata()
        bitmap = getLargeBitmap()
    else:
        metadata = getMetadata()
        bitmap = getSampleBitmap()
    file_path = str(PATH.word2vec_path / "fasttext_model.bin")
    model = FastText.load(file_path)
    df_cardinality = df["cardinality"]
    max_dim = 0
    df_predicates = []
    df_tables = []
    df_labels = []
    for i in range(0, len(df_cardinality)):
        predicates = []
        try:
            data = json.loads(df_cardinality[i])
            table = data["table"]
            tree = data["tree"]
            child_node = tree["childNode"]
            if isinstance(child_node, list):
                for predicate in child_node:
                    predicates.append(predicate)
            else:
                predicates.append(child_node)
            #  2_th layer
            child_trees_2_layer = tree["childTree"]
            for childTree_2 in child_trees_2_layer:
                # 先寻找第3层树的信息
                child_trees_3_layer = childTree_2["childTree"]
                for childTree_3 in child_trees_3_layer:
                    # 第三层没有下一层的子树
                    for node in childTree_3["childNode"]:
                        predicates.append(node)
                for node in childTree_2["childNode"]:
                    predicates.append(node)
            if len(predicates) > max_dim:
                max_dim = len(predicates)
            df_predicates.append(predicates)
            df_tables.append(table)
            df_labels.append(data["cardinality"])
        except Exception as e:
            pass
    samples_features = []
    predicates_features = []
    if incr:
        dim = 1000
    else:
        dim = 300
    for i in range(len(df_predicates)):
        samples_v = []
        predicates_v = []
        predicates = df_predicates[i]
        table = df_tables[i]
        for predicate in predicates:
            bitmap_v = bitmap_encode(bitmap, table, predicate, dim)
            column_name_v = column_name_encode(metadata, table, predicate)
            predicate_v = predicate_encode(model, metadata, table, predicate)
            table_v = table_metadata_encode(metadata, table)
            v_1 = np.concatenate((table_v, bitmap_v))
            v_2 = np.concatenate((column_name_v, predicate_v))
            samples_v.append(v_1)
            predicates_v.append(v_2)
        samples_features.append(np.array(samples_v))
        predicates_features.append(np.array(predicates_v))
        labels.append(df_labels[i])
    return samples_features, predicates_features, labels


def loadMLData(dataset=None, head=None):
    features = []
    labels = []
    tables = []
    df = getDataFrame(dataset, head)
    metadata = getMetadata()
    df_cardinality = df["cardinality"]
    df_expression = df["expression"]
    max_dim = 0
    for i in range(0, len(df_cardinality)):
        if str(df_expression[i]).count(" or ") > 0:
            continue
        predicates = []
        have_no_numeric = False
        try:
            data = json.loads(df_cardinality[i])
            table = data["table"]
            tree = data["tree"]
            child_node = tree["childNode"]
            if isinstance(child_node, list):
                for predicate in child_node:
                    if numeric_column(metadata, table, predicate):
                        predicates.append(predicate)
                    else:
                        have_no_numeric = True
                        raise BreakOuterLoops()
            else:
                if numeric_column(metadata, table, child_node):
                    predicates.append(child_node)
                else:
                    have_no_numeric = True
            if have_no_numeric:
                continue

            #  2_th layer
            child_trees_2_layer = tree["childTree"]
            for childTree_2 in child_trees_2_layer:
                # 先寻找第3层树的信息
                child_trees_3_layer = childTree_2["childTree"]
                for childTree_3 in child_trees_3_layer:
                    # 第三层没有下一层的子树
                    for node in childTree_3["childNode"]:
                        if numeric_column(metadata, table, node):
                            predicates.append(node)
                        else:
                            have_no_numeric = True
                            raise BreakOuterLoops()

                if have_no_numeric:
                    raise BreakOuterLoops()

                for node in childTree_2["childNode"]:
                    if numeric_column(metadata, table, node):
                        predicates.append(node)
                    else:
                        have_no_numeric = True
                        raise BreakOuterLoops()
                if have_no_numeric:
                    raise BreakOuterLoops()

            if len(predicates) > max_dim:
                max_dim = len(predicates)
            features.append(predicates)
            labels.append(data["cardinality"])
            tables.append(table)
        except Exception as e:
            pass

    features_ = copy.deepcopy(features)
    features = []
    for i in range(len(features_)):
        feature_v = []
        for predicate in features_[i]:
            predicate_v = range_encode(metadata, tables[i], predicate)
            feature_v.append(predicate_v)

        feature_v = np.concatenate(feature_v).ravel()
        if len(feature_v) - max_dim * 3 > 0:
            diff = len(feature_v) - max_dim * 3
            feature_v = np.concatenate((feature_v, np.zeros(diff, dtype=int)))
        features.append(feature_v)
    return np.array(features), np.array(labels)


def loadMLTreeData(dataset=None, head=None):
    features = []
    labels = []
    tables = []
    df = getDataFrame(dataset, head)
    metadata = getMetadata()
    df_cardinality = df["cardinality"]
    df_expression = df["expression"]
    max_dim = 0
    for i in range(0, len(df_cardinality)):
        if str(df_expression[i]).count(" or ") > 0:
            continue
        predicates = []
        have_no_numeric = False
        try:
            data = json.loads(df_cardinality[i])
            table = data["table"]
            tree = data["tree"]
            child_node = tree["childNode"]
            if isinstance(child_node, list):
                for predicate in child_node:
                    if numeric_column(metadata, table, predicate):
                        predicates.append(predicate)
                    else:
                        have_no_numeric = True
                        raise BreakOuterLoops()
            else:
                if numeric_column(metadata, table, child_node):
                    predicates.append(child_node)
                else:
                    have_no_numeric = True
            if have_no_numeric:
                continue

            #  2_th layer
            child_trees_2_layer = tree["childTree"]
            for childTree_2 in child_trees_2_layer:
                # 先寻找第3层树的信息
                child_trees_3_layer = childTree_2["childTree"]
                for childTree_3 in child_trees_3_layer:
                    # 第三层没有下一层的子树
                    for node in childTree_3["childNode"]:
                        if numeric_column(metadata, table, node):
                            predicates.append(node)
                        else:
                            have_no_numeric = True
                            raise BreakOuterLoops()

                if have_no_numeric:
                    raise BreakOuterLoops()

                for node in childTree_2["childNode"]:
                    if numeric_column(metadata, table, node):
                        predicates.append(node)
                    else:
                        have_no_numeric = True
                        raise BreakOuterLoops()
                if have_no_numeric:
                    raise BreakOuterLoops()

            if len(predicates) > max_dim:
                max_dim = len(predicates)
            features.append(predicates)
            labels.append(data["cardinality"])
            tables.append(table)
        except Exception as e:
            pass

    features_ = copy.deepcopy(features)
    features = []
    for i in range(len(features_)):
        root = FeatureData.Tree("and")
        feature_v = []
        for predicate in features_[i]:
            predicate_v = range_encode(metadata, tables[i], predicate)
            feature_v.append(predicate_v)
        root.setChildNode(feature_v)
        features.append(root)
    return features, labels


def loadFCNPoolData(dataset=None, head=None):
    labels = []
    df = getDataFrame(dataset, head)
    metadata = getMetadata()
    file_path = str(PATH.word2vec_path / "fasttext_model.bin")
    model = FastText.load(file_path)
    df_cardinality = df["cardinality"]
    max_dim = 0
    df_predicates = []
    df_tables = []
    df_labels = []
    for i in range(0, len(df_cardinality)):
        predicates = []
        try:
            data = json.loads(df_cardinality[i])
            table = data["table"]
            tree = data["tree"]
            child_node = tree["childNode"]
            if isinstance(child_node, list):
                for predicate in child_node:
                    predicates.append(predicate)
            else:
                predicates.append(child_node)
            #  2_th layer
            child_trees_2_layer = tree["childTree"]
            for childTree_2 in child_trees_2_layer:
                # 先寻找第3层树的信息
                child_trees_3_layer = childTree_2["childTree"]
                for childTree_3 in child_trees_3_layer:
                    # 第三层没有下一层的子树
                    for node in childTree_3["childNode"]:
                        predicates.append(node)
                for node in childTree_2["childNode"]:
                    predicates.append(node)
            if len(predicates) > max_dim:
                max_dim = len(predicates)
            df_predicates.append(predicates)
            df_tables.append(table)
            df_labels.append(data["cardinality"])
        except Exception as e:
            pass
    predicates_features = []
    dim = 0
    for i in range(len(df_predicates)):
        predicates_v = []
        predicates = df_predicates[i]
        table = df_tables[i]
        for predicate in predicates:
            column_name_v = column_name_encode(metadata, table, predicate)
            predicate_v = predicate_encode(model, metadata, table, predicate)
            p_v = np.concatenate((column_name_v, predicate_v))
            dim = len(p_v)
            predicates_v.append(p_v)
        diff = max_dim - len(predicates_v)
        for _ in range(diff):
            predicates_v.append(np.ones(dim))
        predicates_features.append(np.array(predicates_v))
        labels.append(df_labels[i])
    return predicates_features, labels


def loadVSCNNData(dataset=None, head=None, incr=True):
    tables = []
    predicates = []
    labels = []
    df = getDataFrame(dataset, head)
    metadata = getMetadata()
    if incr:
        dim = 1000
        bitmap = getLargeBitmap()
    else:
        dim = 300
        bitmap = getSampleBitmap()

    skip_gram_path = str(PATH.word2vec_path / "skip-gram.bin")
    model = FastText.load(skip_gram_path)
    df_cardinality = df["cardinality"]
    df_expression = df["expression"]
    max_dim = 0
    col_num = len(metadata["columns"].keys())
    for i in range(0, len(df_expression)):
        try:
            data = json.loads(df_cardinality[i])
            expression = str(df_expression[i])
            table, sql = expression.split('.', 1)
            table_info_v = table_info_encode(table, metadata, bitmap, dim, col_num)
            words_v = []
            vocabs = splitPredicates(sql)
            for vocab in vocabs:
                words_v.append(np.array(model.wv[vocab]))
            if len(words_v) > max_dim:
                max_dim = len(words_v)
            tables.append(table_info_v)
            predicates.append(words_v)
            labels.append(data["cardinality"])
        except Exception as e:
            pass

    for i in range(len(predicates)):
        p_v = predicates[i]
        diff = max_dim - len(p_v)
        for _ in range(diff):
            predicates[i].append(np.zeros(161))
    return tables, predicates, labels


def loadLPCEData(dataset=None, head=None):
    labels = []
    df = getDataFrame(dataset, head)
    metadata = getMetadata()
    file_path = str(PATH.word2vec_path / "fasttext_model.bin")
    model = FastText.load(file_path)
    df_cardinality = df["cardinality"]
    df_predicates = []
    df_tables = []
    df_operations = []
    # Parsing SQL statements line by line
    for i in range(0, len(df_cardinality)):
        predicates = []
        operations = []
        try:
            data = json.loads(df_cardinality[i])
            table = data["table"]
            tree = data["tree"]
            child_node = tree["childNode"]
            if isinstance(child_node, list):
                for predicate in child_node:
                    predicates.append(predicate)
            else:
                predicates.append(child_node)

            #  2_th layer
            child_trees_2_layer = tree["childTree"]
            for childTree_2 in child_trees_2_layer:
                # 先寻找第3层树的信息
                child_trees_3_layer = childTree_2["childTree"]
                for childTree_3 in child_trees_3_layer:
                    operations.append(childTree_3["operator"])
                    # 第三层没有下一层的子树
                    for node in childTree_3["childNode"]:
                        predicates.append(node)
                operations.append(childTree_2["operator"])
                for node in childTree_2["childNode"]:
                    predicates.append(node)
            operations.append(tree["operator"])
            # key
            df_operations.append(operations)
            df_predicates.append(predicates)
            df_tables.append(table)
            labels.append(data["cardinality"])
        except Exception as e:
            pass

    predicates_feature = []
    operations_feature = []
    tables_feature = []
    predicate_max_dim = 1072
    operation_max_dim = 0

    for i in range(len(df_predicates)):
        predicates = df_predicates[i]
        operations = df_operations[i]
        table = df_tables[i]
        # ---
        table_v = table_metadata_encode(metadata, table)
        tables_feature.append(np.array(table_v))
        predicate_v = np.array([])
        for predicate in predicates:
            encoded = predicate_encode(model, metadata, table, predicate)
            predicate_v = np.append(predicate_v, encoded)
        if predicate_v.size >= predicate_max_dim:
            predicate_max_dim = predicate_v.size

        operation_v = np.array([])
        for operation in operations:
            if operation == "and":
                op_encode = np.array([1, 0])
            else:
                op_encode = np.array([0, 1])
            operation_v = np.append(operation_v, op_encode)

        if operation_v.size >= operation_max_dim:
            operation_max_dim = operation_v.size

        predicates_feature.append(np.array(predicate_v))
        operations_feature.append(np.array(operation_v))

    for i in range(0, len(predicates_feature)):
        predicate_v = predicates_feature[i]
        if predicate_v.size < predicate_max_dim:
            # 计算需要补全的零的数量
            padding_size = predicate_max_dim - predicate_v.size
            # 创建一个全零数组
            zero_padding = np.zeros(padding_size)
            # 将原数组和全零数组拼接
            predicate_v = np.concatenate((predicate_v, zero_padding))
        predicates_feature[i] = predicate_v

        # 对操作特征进行补全
        operation_v = operations_feature[i]
        if operation_v.size < operation_max_dim:
            padding_size = operation_max_dim - operation_v.size
            zero_padding = np.zeros(padding_size)
            operation_v = np.concatenate((operation_v, zero_padding))
        operations_feature[i] = operation_v

    return tables_feature, operations_feature, predicates_feature, labels
