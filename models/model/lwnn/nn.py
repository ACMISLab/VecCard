import torch
import torch.nn as nn
import torch.nn.functional as F


class MLP(nn.Module):
    def __init__(self, in_dim, hidden_dim):
        super(MLP, self).__init__()
        self.in_dim = in_dim
        self.hidden_dim = hidden_dim

        self.fc1 = nn.Linear(self.in_dim, self.hidden_dim, bias=False)
        self.fc2 = nn.Linear(self.hidden_dim, self.hidden_dim, bias=False)
        self.output = nn.Linear(self.hidden_dim, 1, bias=False)
        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        self.to(self.device)

    def forward(self, inp):
        inp = inp.to(self.device)
        out = F.relu(self.fc1(inp))
        out = F.relu(self.fc2(out))
        out = self.output(out)
        return out
