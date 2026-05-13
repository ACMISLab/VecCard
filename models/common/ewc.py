import torch
import torch.nn as nn


def ewc_loss(model, weight, estimated_fishers, estimated_means):
    losses = []
    for param_name, param in model.named_parameters():
        estimated_mean = estimated_means[param_name]
        estimated_fisher = estimated_fishers[param_name]
        losses.append((estimated_fisher * (param - estimated_mean) ** 2).sum())
    return (weight / 2) * sum(losses)


def estimate_ewc_params(model, train_dataloader, device, max_batches=120):
    """
    估计 EWC 所需的参数平均值（θ*）和 Fisher 信息矩阵（F）

    参数:
    - model: 要估计的模型（已训练完旧任务）
    - train_dataloader: 当前任务的数据加载器
    - device: 设备（"cuda" 或 "cpu"）
    - max_batches: 用于估计的最大 batch 数，默认120

    返回:
    - estimated_mean: 参数在旧任务训练后的值（θ*）
    - estimated_fisher: 参数的 Fisher 信息估计值
    """
    model.eval()

    # 存储旧任务中的参数值（θ*）
    estimated_mean = {name: param.data.clone() for name, param in model.named_parameters()}

    # 初始化 Fisher 信息矩阵为全零
    estimated_fisher = {name: torch.zeros_like(param) for name, param in model.named_parameters()}

    criterion = nn.MSELoss()

    for i, (trees_batch, labels_batch) in enumerate(train_dataloader):
        if i >= max_batches:
            break

        trees_batch = [tree.to(device) for tree in trees_batch]
        labels_batch = labels_batch.to(device)

        model.zero_grad()
        predictions = []

        for tree in trees_batch:
            output = model(tree)  # 假设 model(tree) 输出标量
            predictions.append(output)

        predictions = torch.stack(predictions).squeeze()
        labels_batch = labels_batch.to(dtype=torch.float32)

        loss = criterion(predictions, labels_batch)
        loss.backward()

        for name, param in model.named_parameters():
            if param.grad is not None:
                # 累加 Fisher 信息估计：E[∇L^2] ≈ ∇L^2
                estimated_fisher[name] += (param.grad.data ** 2) / max_batches

    return estimated_mean, estimated_fisher
