import numpy as np
import torch
import torch.nn as nn

class CELossFunction(nn.Module):
    def __init__(self):
        super(CELossFunction, self).__init__()
        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        self.to(self.device)

    def forward(self, predictions, targets):
        # 防止分母为零，确保数值稳定性
        epsilon = 1e-8

        # 计算 Cout / Clabel 和 Clabel / Cout
        ratio1 = predictions / (targets + epsilon)
        ratio2 = targets / (predictions + epsilon)
        # 计算 max(Cout/Clabel, Clabel/Cout)
        max_ratios = torch.max(ratio1, ratio2)
        # 计算 sum(max_ratios)
        sum_max_ratios = torch.sum(max_ratios)
        # 计算最终的损失值 log(sum(max_ratios))
        loss = torch.log(sum_max_ratios)
        return loss



def print_q_error(pred, labels):
    if torch.is_tensor(pred):
        pred = torch.expm1(pred).cpu().numpy()
        labels = torch.expm1(labels).cpu().numpy()
    else:
        pred = np.expm1(pred)
        labels = np.expm1(labels)

    q_error = []
    for i in range(len(pred)):
        if pred[i] > labels[i]:
            q_error.append(pred[i] / labels[i])
        else:
            pred_value = pred[i]
            if pred_value == 0:
                pred_value += 1e-8
            q_error.append(labels[i] / pred_value)
    print("Mean: {}".format(np.mean(q_error)))
    print("Median: {}".format(np.median(q_error)))
    print("Max: {}".format(np.max(q_error)))
    print("90th percentile: {}".format(np.percentile(q_error, 90)))
    print("95th percentile: {}".format(np.percentile(q_error, 95)))
    print("99th percentile: {}".format(np.percentile(q_error, 99)))


def print_mean_error(pred, labels):
    if torch.is_tensor(pred):
        pred = torch.expm1(pred).cpu().numpy()
    if torch.is_tensor(labels):
        labels = torch.expm1(labels).cpu().numpy()
    else:
        pred = np.expm1(pred)
        labels = np.expm1(labels)
    mape = np.mean(np.abs((labels - pred) / labels)) * 100
    smape = np.mean(2.0 * np.abs(pred - labels) / (np.abs(pred) + np.abs(labels))) * 100
    rmse = np.sqrt(np.mean((pred - labels) ** 2))
    mae = np.mean(np.abs(pred - labels))
    print(f"Mean Absolute Percentage Error (MAPE): {mape}%")
    print(f"Symmetric Mean Absolute Percentage Error (SMAPE): {smape}%")
    print(f"Root Mean Squared Error (RMSE): {rmse}")
    print(f"Mean Absolute Error (MAE): {mae}")
