import torch
import torch.nn as nn
import torch.nn.functional as F


class EmbeddingLayer(nn.Module):
    def __init__(self, input_dim, hidden_dim):
        super(EmbeddingLayer, self).__init__()
        self.fc1 = nn.Linear(input_dim, hidden_dim)
        # 使用GPU计算
        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        self.to(self.device)

    def forward(self, x):
        x = x.to(self.device)
        x = F.relu(self.fc1(x))
        return x


class EstimationLayer(nn.Module):
    def __init__(self, input_size, hidden_size, dropout):
        super(EstimationLayer, self).__init__()
        self.fc1 = nn.Linear(input_size, hidden_size)
        self.fc2 = nn.Linear(hidden_size, 1)
        self.dropout = nn.Dropout(dropout)
        # 使用GPU计算
        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        self.to(self.device)

    def forward(self, x):
        x = x.to(self.device)
        x = F.relu(self.fc1(x))
        x = self.dropout(x)
        x = self.fc2(x)
        return x


class TreeNNCell(nn.Module):
    def __init__(self, input_size, hidden_size):
        super(TreeNNCell, self).__init__()
        self.hidden_size = hidden_size
        self.fc1 = nn.Linear(input_size, hidden_size)
        self.fc2 = nn.Linear(hidden_size, hidden_size)
        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        self.to(self.device)

    def forward(self, x):
        x = x.to(self.device)
        x = F.relu(self.fc1(x))
        x = F.relu(self.fc2(x))
        return x


class TreeNN(nn.Module):
    def __init__(self, input_dim, embedding_hidden_dim, estimation_hidden_dim, dropout):
        super(TreeNN, self).__init__()
        self.input_dim = input_dim
        self.embedding_hidden_dim = embedding_hidden_dim
        self.lstm_hidden_dim = estimation_hidden_dim
        self.dropout = dropout

        self.embedding_layer = EmbeddingLayer(input_dim, embedding_hidden_dim)
        self.tree_nn = TreeNNCell(embedding_hidden_dim, embedding_hidden_dim)
        self.estimation_layer = EstimationLayer(embedding_hidden_dim, estimation_hidden_dim, dropout)

        # device
        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        self.to(self.device)

    def forward(self, tree):
        tree = tree.to(self.device)
        out = self.process_tree(tree)
        output = self.estimation_layer(out)
        return output

    def process_tree(self, tree):
        outputs = []
        if tree.haveChildTree():
            for childTree in tree.getChildTree():
                out = self.process_tree(childTree)
                outputs.append(out)
        # 逐步计算
        if tree.haveChildNode():  # array
            for child_node in tree.getChildNode():
                leaf_features = torch.tensor(child_node, dtype=torch.float32, device=self.device)
                leaf_output = self.embedding_layer(leaf_features)
                outputs.append(leaf_output)

        if tree.getOperator() == 'or':
            input_node = torch.max(torch.stack(outputs), dim=0).values
        else:
            input_node = torch.min(torch.stack(outputs), dim=0).values
        out = self.tree_nn(input_node)
        return out
