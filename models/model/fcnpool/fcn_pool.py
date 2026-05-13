import torch
import torch.nn as nn


class FCN_POOL(nn.Module):
    def __init__(self, input_dim, hidden_dim):
        super(FCN_POOL, self).__init__()
        self.hidden_dim = hidden_dim
        self.fc1 = nn.Linear(input_dim, hidden_dim)
        self.fc2 = nn.Linear(input_dim, hidden_dim)
        self.fc3 = nn.Linear(input_dim, hidden_dim)
        self.fc4 = nn.Linear(hidden_dim, 1)
        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        self.to(self.device)

    def forward(self, x):
        x = x.to(self.device)
        out1 = self.fc1(x)
        out2 = self.fc2(x)
        out3 = self.fc3(x)
        pooled_out = (out1 + out2 + out3) / 3
        pooled_out = torch.sum(pooled_out, dim=1, keepdim=False)
        out = self.fc4(pooled_out)
        return out
