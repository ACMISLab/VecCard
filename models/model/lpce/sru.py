import torch
import torch.nn as nn
import torch.nn.functional as F


class SRU(nn.Module):
    def __init__(self, embed_dim, mem_dim, output_dim):
        super(SRU, self).__init__()

        self.device = torch.device("cuda:0" if torch.cuda.is_available() else "cpu")
        self.to(self.device)

        self.input_dim = embed_dim
        self.mem_dim = mem_dim
        self.output_dim = output_dim

        self.operat_dim = 8
        self.table_dim = 5
        self.filter_dim = 1072

        # Embed module: 2 layer MLP for different input features
        self.feature_mpl_operation = torch.nn.Linear(self.operat_dim, self.input_dim)
        self.feature_mpl_table = torch.nn.Linear(self.table_dim, self.input_dim)
        self.feature_mpl_filter = torch.nn.Linear(self.filter_dim, self.input_dim)

        # 2nd layer MLP for each feature
        self.feature_mpl_operation_2 = nn.Linear(self.input_dim, self.input_dim)
        self.feature_mpl_table_2 = nn.Linear(self.input_dim, self.input_dim)
        self.feature_mpl_filter_2 = nn.Linear(self.input_dim, self.input_dim)

        # Weights for combining inputs to SRU cell
        self.W_xou = nn.Linear(self.input_dim * 3, 3 * self.mem_dim)  # 3 gates: f, r, and xx (candidate)

        # Output module (MLP to predict cardinality)
        self.out_mlp1 = nn.Linear(self.mem_dim, self.output_dim).to(self.device)
        self.out_mlp2 = nn.Linear(self.output_dim, 1).to(self.device)

        # Initialize weights
        # self._initialize_weights()

    def _initialize_weights(self):
        torch.nn.init.xavier_uniform_(self.feature_mpl_operation.weight)
        torch.nn.init.constant_(self.feature_mpl_operation.bias, 0)
        torch.nn.init.xavier_uniform_(self.feature_mpl_table.weight)
        torch.nn.init.constant_(self.feature_mpl_table.bias, 0)
        torch.nn.init.xavier_uniform_(self.feature_mpl_filter.weight)
        torch.nn.init.constant_(self.feature_mpl_filter.bias, 0)
        torch.nn.init.xavier_uniform_(self.feature_mpl_operation_2.weight)
        torch.nn.init.constant_(self.feature_mpl_operation_2.bias, 0)
        torch.nn.init.xavier_uniform_(self.feature_mpl_table_2.weight)
        torch.nn.init.constant_(self.feature_mpl_table_2.bias, 0)
        torch.nn.init.xavier_uniform_(self.feature_mpl_filter_2.weight)
        torch.nn.init.constant_(self.feature_mpl_filter_2.bias, 0)
        torch.nn.init.xavier_uniform_(self.W_xou.weight)
        torch.nn.init.constant_(self.W_xou.bias, 0)
        torch.nn.init.xavier_uniform_(self.out_mlp1.weight)
        torch.nn.init.constant_(self.out_mlp1.bias, 0)
        torch.nn.init.xavier_uniform_(self.out_mlp2.weight)
        torch.nn.init.constant_(self.out_mlp2.bias, 0)

    def forward(self, op_feat, tb_feat, ft_feat):
        batch_size = op_feat.shape[0]

        # Initialize hidden and cell states
        h = torch.zeros(batch_size, self.mem_dim, device=self.device)
        c = torch.zeros(batch_size, self.mem_dim, device=self.device)

        # Embed the input features using 2-layer MLP for each
        op_feat = F.relu(self.feature_mpl_operation(op_feat))
        op_feat = F.relu(self.feature_mpl_operation_2(op_feat))

        tb_feat = F.relu(self.feature_mpl_table(tb_feat))
        tb_feat = F.relu(self.feature_mpl_table_2(tb_feat))

        ft_feat = F.relu(self.feature_mpl_filter(ft_feat))
        ft_feat = F.relu(self.feature_mpl_filter_2(ft_feat))

        # Concatenate embedded features
        x = torch.cat((op_feat, tb_feat, ft_feat), 1)

        # Compute the gates for SRU
        xou = self.W_xou(x)
        xx, ff, rr = torch.split(xou, xou.size(1) // 3, dim=1)
        ff = torch.sigmoid(ff)
        rr = torch.sigmoid(rr)

        # Initialize the first step of SRU
        h, c = self._run_init(h, c, xx, ff, rr, x)

        # Process through the SRU cell for the remaining steps (node-wise)
        for n in range(1, 1 + 1):  # Only one iteration step (in simplified form)
            h, c = self._run_SRU(n, h, c, xx, ff, rr, x)

        # Output layer to predict cardinality
        hid_output = F.relu(self.out_mlp1(h))
        out = torch.sigmoid(self.out_mlp2(hid_output)).to(self.device)

        return out

    def _run_init(self, h, c, xx, ff, rr, features):
        h = h.to(self.device)
        c = c.to(self.device)
        xx = xx.to(self.device)
        ff = ff.to(self.device)
        rr = rr.to(self.device)
        features = features.to(self.device)

        padding = torch.ones(features.shape[0], 256, device=self.device)
        features = torch.cat((features, padding), dim=1)

        # 避免就地操作，创建新的张量来存储结果
        c_new = (1 - ff) * xx
        h_new = rr * torch.tanh(c_new) + (1 - rr) * features

        return h_new, c_new  # 返回新的张量

    def _run_SRU(self, iteration, h, c, xx, ff, rr, features):
        f = ff.to(self.device)
        r = rr.to(self.device)
        h = h.to(self.device)
        c = c.to(self.device)
        xx = xx.to(self.device)
        features = features.to(self.device)
        padding = torch.zeros(features.shape[0], 256, device=self.device)
        features = torch.cat((features, padding), dim=1)

        # 避免就地操作，创建新的张量来存储结果
        c_new = f * c + (1 - f) * xx
        h_new = r * torch.tanh(c_new) + (1 - r) * features

        return h_new, c_new  # 返回新的张量
