import torch
import torch.nn as nn


class EWC:
    def __init__(self, model, dataloader, device, criterion=nn.MSELoss(), max_batches=120):
        self.model = model
        self.device = device
        self.criterion = criterion
        self.max_batches = max_batches

        self.params_mean = {}
        self.fisher = {}
        self.dataloader = dataloader

    def estimate_ewc_params(self):
        self.model.eval()

        # Step 1: Save parameter means (θ*)
        for name, param in self.model.named_parameters():
            self.params_mean[name] = param.data.clone().detach()

        # Step 2: Initialize Fisher matrix
        fisher_estimates = {name: torch.zeros_like(param, device=self.device)
                            for name, param in self.model.named_parameters()}

        effective_batches = min(self.max_batches, len(self.dataloader))

        for i, (trees_batch, labels_batch) in enumerate(self.dataloader):
            if i >= self.max_batches:
                break

            trees_batch = [tree.to(self.device) for tree in trees_batch]
            labels_batch = labels_batch.to(self.device).to(dtype=torch.float32)

            self.model.zero_grad()
            predictions = []

            for tree in trees_batch:
                output = self.model(tree)
                predictions.append(output)

            predictions = torch.stack(predictions).squeeze()
            loss = self.criterion(predictions, labels_batch)
            loss.backward()

            for name, param in self.model.named_parameters():
                if param.grad is not None:
                    fisher_estimates[name] += (param.grad.data ** 2) / effective_batches

        self.fisher = fisher_estimates

    def compute_ewc_loss(self, lambda_ewc):
        """
        EWC 正则项 loss
        """
        ewc_loss = 0.0
        for name, param in self.model.named_parameters():
            if name in self.params_mean:
                mean = self.params_mean[name]
                fisher = self.fisher[name]
                ewc_loss += (fisher * (param - mean) ** 2).sum()
        return (lambda_ewc / 2) * ewc_loss

    def get_fisher(self):
        return self.fisher

    def get_params_mean(self):
        return self.params_mean

