import torch
import torch.nn as nn
import torch.nn.functional as F


class MLP(nn.Module):
    def __init__(self, in_dim, hidden_dim):
        super(MLP, self).__init__()
        self.in_dim = in_dim
        self.hidden_dim = hidden_dim

        self.fc1 = nn.Linear(in_dim, hidden_dim)
        self.fc2 = nn.Linear(hidden_dim, hidden_dim)
        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        self.to(self.device)

    def forward(self, x):
        x = x.to(self.device)
        x = F.relu(self.fc1(x))
        x = F.relu(self.fc2(x))
        x = torch.mean(x, dim=0)
        return x


class MLPOut(nn.Module):
    def __init__(self, in_dim, hidden_dim):
        super(MLPOut, self).__init__()
        self.in_dim = in_dim
        self.hidden_dim = hidden_dim
        self.fc1 = nn.Linear(in_dim, hidden_dim)
        self.fc2 = nn.Linear(hidden_dim, 1)
        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        self.to(self.device)

    def forward(self,x):
        x = x.to(self.device)
        x = F.relu(self.fc1(x))
        x = F.sigmoid(self.fc2(x))
        return x


# (m,n) 垂直卷积就是 (n,m)的水平卷积
class VerticalConvModule(nn.Module):
    def __init__(self, m, n, num_filters=3):
        super(VerticalConvModule, self).__init__()
        self.m = m
        self.n = n
        self.num_filters = num_filters

        # 创建三个不同宽度的卷积核，每个卷积核有3组不同的权重
        self.conv1 = nn.Conv2d(1, num_filters, (m, 2), stride=1, padding=0)
        self.conv2 = nn.Conv2d(1, num_filters, (m, 3), stride=1, padding=0)
        self.conv3 = nn.Conv2d(1, num_filters, (m, 4), stride=1, padding=0)

        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        self.to(self.device)

    def forward(self, x):
        x = x.to(self.device)
        x = x.t()
        if x.dim() != 4:
            x = x.unsqueeze(0).unsqueeze(0)
        conv1 = self.conv1(x)
        conv2 = self.conv2(x)
        conv3 = self.conv3(x)
        # 对每个卷积结果进行最大池化
        pool1 = F.max_pool2d(conv1, (1, conv1.size(3)))
        pool2 = F.max_pool2d(conv2, (1, conv2.size(3)))
        pool3 = F.max_pool2d(conv3, (1, conv3.size(3)))
        # 将池化后的向量拼接起来
        pooled = torch.cat((pool1, pool2, pool3), dim=1).squeeze()
        return pooled


class VSCNN(nn.Module):
    def __init__(self, mlp_input_dim, mlp_hidden_dim, m, n):
        super(VSCNN, self).__init__()
        self.mlp_input_dim = mlp_input_dim
        self.mlp_hidden_dim = mlp_hidden_dim
        self.cnn = VerticalConvModule(n, m)
        self.mlp = MLP(mlp_input_dim, mlp_hidden_dim)
        self.mlp_out = MLPOut(265, mlp_hidden_dim)

    def forward(self, table, predicate):
        t = self.mlp(table)
        p = self.cnn(predicate)
        merge = torch.cat((t, p), dim=0)
        output = self.mlp_out(merge)
        return output
