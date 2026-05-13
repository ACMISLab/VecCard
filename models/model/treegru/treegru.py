import torch
import torch.nn as nn
import torch.nn.functional as F


class EmbeddingLayer(nn.Module):
    def __init__(self, input_dim, hidden_dim, output_dim, dropout):
        super(EmbeddingLayer, self).__init__()
        self.fc1 = nn.Linear(input_dim, hidden_dim)
        self.fc2 = nn.Linear(hidden_dim, output_dim)
        self.dropout = nn.Dropout(dropout)
        # 使用GPU计算
        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        self.to(self.device)

    def forward(self, x):
        x = x.to(self.device)
        x = F.relu(self.fc1(x))
        x = self.dropout(x)
        x = F.relu(self.fc2(x))
        return x


class EstimationLayer(nn.Module):
    def __init__(self, input_size, hidden_size, dropout, use_attention):
        super(EstimationLayer, self).__init__()
        self.fc1 = nn.Linear(input_size, hidden_size)
        self.fc2 = nn.Linear(hidden_size, 1)
        self.use_attention = use_attention
        self.attention_layer = Attention(input_size, hidden_size)
        self.dropout = nn.Dropout(dropout)
        # 使用GPU计算
        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        self.to(self.device)

    def forward(self, x):
        x = x.to(self.device)
        if self.use_attention:
            x = F.relu(self.attention_layer(x))
        else:
            x = F.relu(self.fc1(x))
        x = self.dropout(x)
        x = self.fc2(x)
        return x


class Attention(nn.Module):
    def __init__(self, input_dim, output_dim):
        super(Attention, self).__init__()
        self.query = nn.Linear(input_dim, output_dim)
        self.key = nn.Linear(input_dim, output_dim)
        self.value = nn.Linear(input_dim, output_dim)
        self.input_dim = input_dim
        self.output_dim = output_dim

    def forward(self, h):
        # 确保 h 是至少二维的
        if h.dim() == 1:
            h = h.unsqueeze(0)  # 添加一个批次维度

        query = self.query(h)  # (batch_size, seq_len, input_dim)
        key = self.key(h)  # (batch_size, seq_len, input_dim)
        value = self.value(h)  # (batch_size, seq_len, input_dim)

        # 计算注意力权重
        attention_scores = torch.matmul(query, key.transpose(-2, -1)) / (self.input_dim ** 0.5)
        attention_weights = F.softmax(attention_scores, dim=-1)

        # 应用注意力权重
        attention_output = torch.matmul(attention_weights, value)
        return attention_output.squeeze(0)  # 如果需要，移除批次维度


class ChildSumTreeGRUCell(nn.Module):
    def __init__(self, input_size, hidden_size):
        super(ChildSumTreeGRUCell, self).__init__()
        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        self.to(self.device)
        self.hidden_size = hidden_size
        self.gru = nn.GRUCell(input_size, hidden_size)

    def forward(self, x, child_h):
        x = x.to(self.device)
        if child_h:
            h_sum = torch.sum(torch.stack(child_h), dim=0)
        else:
            h_sum = torch.zeros(self.hidden_size, device=self.device)
        h = self.gru(x, h_sum)
        return h


class TreeGRU(nn.Module):
    def __init__(self, input_dim, embedding_hidden_dim, gru_hidden_dim, estimation_hidden_dim, use_attention, dropout):
        super(TreeGRU, self).__init__()
        self.input_dim = input_dim
        self.embedding_hidden_dim = embedding_hidden_dim
        self.gru_hidden_dim = gru_hidden_dim
        self.estimation_hidden_dim = estimation_hidden_dim
        self.use_attention = use_attention

        # model
        self.embedding_layer = EmbeddingLayer(input_dim, embedding_hidden_dim, gru_hidden_dim, dropout)
        self.tree_gru = ChildSumTreeGRUCell(gru_hidden_dim, gru_hidden_dim)
        self.estimation_layer = EstimationLayer(gru_hidden_dim, estimation_hidden_dim, dropout,use_attention)
        # self.attention_layer = Attention(gru_hidden_dim, gru_hidden_dim)  # 添加注意力层

        # device
        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        self.to(self.device)

    def forward(self, tree):
        tree = tree.to(self.device)
        h = self.process_tree(tree)
        output = self.estimation_layer(h)
        # if self.use_attention:
        #     # 使用注意力层计算注意力加权的 h
        #     h_with_attention = self.attention_layer(h)
        #     # 传入估计层
        #     output = self.estimation_layer(h_with_attention)
        # else:
        #     output = self.estimation_layer(h)
        return output

    def process_tree(self, tree):
        outputs = []
        child_h = []
        if tree.haveChildTree():
            for childTree in tree.getChildTree():
                h_t = self.process_tree(childTree)
                outputs.append(h_t)
                child_h.append(h_t)
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
        h = self.tree_gru(input_node, child_h)
        return h
