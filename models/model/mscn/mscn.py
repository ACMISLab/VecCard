import torch
import torch.nn as nn
import torch.nn.functional as F


class SetConv(nn.Module):
    def __init__(self, sample_feats, predicate_feats, hid_units):
        super(SetConv, self).__init__()
        self.sample_mlp1 = nn.Linear(sample_feats, hid_units)
        self.sample_mlp2 = nn.Linear(hid_units, hid_units)
        self.predicate_mlp1 = nn.Linear(predicate_feats, hid_units)
        self.predicate_mlp2 = nn.Linear(hid_units, hid_units)
        self.out_mlp1 = nn.Linear(hid_units * 2, hid_units)
        self.out_mlp2 = nn.Linear(hid_units, 1)

    def forward(self, samples,  predicates, sample_mask, predicate_mask):
        hid_sample = F.relu(self.sample_mlp1(samples))
        hid_sample = F.relu(self.sample_mlp2(hid_sample))
        hid_sample = hid_sample * sample_mask
        hid_sample = torch.sum(hid_sample, dim=1, keepdim=False)
        sample_norm = sample_mask.sum(1, keepdim=False)
        hid_sample = hid_sample / sample_norm

        hid_predicate = F.relu(self.predicate_mlp1(predicates))
        hid_predicate = F.relu(self.predicate_mlp2(hid_predicate))
        hid_predicate = hid_predicate * predicate_mask
        hid_predicate = torch.sum(hid_predicate, dim=1, keepdim=False)
        predicate_norm = predicate_mask.sum(1, keepdim=False)
        hid_predicate = hid_predicate / predicate_norm

        hid = torch.cat((hid_sample, hid_predicate), 1)
        hid = F.relu(self.out_mlp1(hid))
        out = torch.sigmoid(self.out_mlp2(hid))
        return out
