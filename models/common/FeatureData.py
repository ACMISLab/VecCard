from torch.utils.data import Dataset
from torch.utils.data import dataset
import torch
import numpy as np
import json
from common import Utils


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

    def haveChildNode(self):
        return len(self.childNode) != 0

    def haveChildTree(self):
        return len(self.childTree) != 0

    def structures(self):
        return json.dumps(self, default=lambda o: o.__dict__, sort_keys=True, indent=None)

    # 使用GPU计算
    def to(self, device):
        # 移动 childNode 到指定设备
        if self.haveChildNode():
            self.childNode = [torch.tensor(tensor).to(device) if isinstance(tensor, np.ndarray) else tensor.to(device)
                              for tensor in self.childNode]
        # 递归地将所有子树移动到指定设备
        if self.haveChildTree():
            self.childTree = [child.to(device) for child in self.childTree]
        return self


class SQLTreeDataset(Dataset):
    def __init__(self, trees, labels):
        self.trees = trees
        self.labels = labels

    def __len__(self):
        return len(self.trees)

    def __getitem__(self, idx):
        return self.trees[idx], self.labels[idx]


class SQLCnnDataset(Dataset):
    def __init__(self, tables, predicates, labels):
        self.tables = tables
        self.predicates = predicates
        self.labels = labels

    def __len__(self):
        return len(self.labels)

    def __getitem__(self, idx):
        return self.tables[idx], self.predicates[idx], self.labels[idx]


def collate_fn(batch):
    trees, labels = zip(*batch)
    labels = torch.tensor(labels, dtype=torch.float32)
    return trees, labels


def collate_fn_cnn(batch):
    tables, predicates, labels = zip(*batch)
    tables = torch.tensor(tables, dtype=torch.float32)
    predicates = torch.tensor(predicates, dtype=torch.float32)
    labels = torch.tensor(labels, dtype=torch.float32)
    return tables, predicates, labels


def collate_fn_lpce(batch):
    tables_feature, operations_feature, predicates_feature, labels = zip(*batch)
    tables_feature = torch.tensor(tables_feature, dtype=torch.float32)
    operations_feature = torch.tensor(operations_feature, dtype=torch.float32)
    predicates_feature = torch.tensor(predicates_feature, dtype=torch.float32)
    labels = torch.tensor(labels, dtype=torch.float32)
    return tables_feature, operations_feature, predicates_feature, labels





class TreeDataset:
    def __init__(self, dataset=None, head=None, args=None):
        self.dataset = dataset
        self.head = head
        self.args = args

    def loadData(self):
        device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        trees, labels = Utils.loadTreeData(self.dataset, self.head, self.args)
        labels_tensor = torch.tensor(np.array(labels), dtype=torch.float32)
        log_babels = torch.log1p(labels_tensor).to(device)
        return trees, log_babels


class MSCNDataset:
    def __init__(self, dataset, head=None, incr=False):
        self.dataset = dataset
        self.head = head
        self.incr = incr

    def loadData(self):
        device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        samples, predicates, labels = Utils.loadMSCNData(self.dataset, self.head, self.incr)
        ds = self.make_dataset(samples, predicates, labels)
        return ds, torch.log1p(torch.FloatTensor(labels))

    def make_dataset(self, samples, predicates, labels, max_num_predicates=8):
        """Add zero-padding and wrap as tensor dataset."""
        sample_masks = []
        sample_tensors = []
        for sample in samples:
            sample_tensor = np.vstack(sample)
            num_pad = max_num_predicates - sample_tensor.shape[0]
            sample_mask = np.ones_like(sample_tensor).mean(1, keepdims=True)
            sample_tensor = np.pad(sample_tensor, ((0, num_pad), (0, 0)), 'constant')
            sample_mask = np.pad(sample_mask, ((0, num_pad), (0, 0)), 'constant')
            sample_tensors.append(np.expand_dims(sample_tensor, 0))
            sample_masks.append(np.expand_dims(sample_mask, 0))
        sample_tensors = np.vstack(sample_tensors)
        sample_tensors = torch.FloatTensor(sample_tensors)
        sample_masks = np.vstack(sample_masks)
        sample_masks = torch.FloatTensor(sample_masks)

        predicate_masks = []
        predicate_tensors = []
        for predicate in predicates:
            predicate_tensor = np.vstack(predicate)
            num_pad = max_num_predicates - predicate_tensor.shape[0]
            predicate_mask = np.ones_like(predicate_tensor).mean(1, keepdims=True)
            predicate_tensor = np.pad(predicate_tensor, ((0, num_pad), (0, 0)), 'constant')
            predicate_mask = np.pad(predicate_mask, ((0, num_pad), (0, 0)), 'constant')
            predicate_tensors.append(np.expand_dims(predicate_tensor, 0))
            predicate_masks.append(np.expand_dims(predicate_mask, 0))
        predicate_tensors = np.vstack(predicate_tensors)
        predicate_tensors = torch.FloatTensor(predicate_tensors)
        predicate_masks = np.vstack(predicate_masks)
        predicate_masks = torch.FloatTensor(predicate_masks)

        target_tensor = torch.log1p(torch.FloatTensor(labels))

        return dataset.TensorDataset(sample_tensors, predicate_tensors, target_tensor, sample_masks,
                                     predicate_masks)


class LPCEDataset:
    def __init__(self, dataset, head=None):
        self.dataset = dataset
        self.head = head

    def loadData(self):
        tables_feature, operations_feature, predicates_feature, labels = Utils.loadLPCEData(self.dataset, self.head)
        tables_feature = torch.tensor(tables_feature, dtype=torch.float32)
        operations_feature = torch.tensor(operations_feature, dtype=torch.float32)
        predicates_feature = torch.tensor(predicates_feature, dtype=torch.float32)
        return tables_feature, operations_feature, predicates_feature, torch.log1p(torch.FloatTensor(labels))


class MLDataset:
    def __init__(self, dataset, head=None):
        self.dataset = dataset
        self.head = head

    def loadData(self):
        features, labels = Utils.loadMLData(self.dataset, self.head)
        log_labels = np.log1p(labels)
        return features, log_labels

    def loadTreeData(self):
        device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        features, labels = Utils.loadMLTreeData(self.dataset, self.head)
        labels_tensor = torch.tensor(np.array(labels), dtype=torch.float32)
        log_labels = torch.log1p(labels_tensor).to(device)
        return features, log_labels

    def loadTensorData(self):
        device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        features, labels = Utils.loadMLData(self.dataset, self.head)
        labels_tensor = torch.tensor(np.array(labels), dtype=torch.float32)
        log_labels = torch.log1p(labels_tensor).to(device)
        features_tensor = torch.tensor(np.array(features), dtype=torch.float32)
        return features_tensor, log_labels


class FCNPoolDataset:
    def __init__(self, dataset=None, head=None):
        self.dataset = dataset
        self.head = head

    def loadData(self):
        features, labels = Utils.loadFCNPoolData(self.dataset, self.head)
        target_tensor = torch.log1p(torch.FloatTensor(labels))
        predicate_tensors = torch.FloatTensor(features)
        return predicate_tensors, target_tensor


class VSCNNDataset:
    def __init__(self, dataset=None, head=None, incre=True):
        self.dataset = dataset
        self.head = head
        self.incre = incre

    def loadData(self):
        tables, predicates, labels = Utils.loadVSCNNData(self.dataset, self.head, self.incre)
        target_tensor = torch.log1p(torch.FloatTensor(labels))
        tables_tensor = torch.FloatTensor(tables)
        predicates_tensor = torch.FloatTensor(predicates)
        return tables_tensor, predicates_tensor, target_tensor
