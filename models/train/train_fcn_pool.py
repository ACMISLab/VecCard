from common import FeatureData
import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import DataLoader
import model.fcnpool.fcn_pool as FCN
import argparse
from common import metrics
from torch.utils.data import dataset
from torch.autograd import Variable
from common import PredictUtils


def main():
    global args
    args = parse_args()
    print("Parsed Arguments:")
    print(args)
    train_data, train_labels = FeatureData.FCNPoolDataset('cardinality_train', args.dataset_count).loadData()
    m_test_data, m_test_labels = FeatureData.FCNPoolDataset('cardinality_mcx_test', args.dataset_count).loadData()
    y_test_data, y_test_labels = FeatureData.FCNPoolDataset('cardinality_yelp_test', args.dataset_count).loadData()

    train_dataset = dataset.TensorDataset(train_data, train_labels)
    val_m_dataset = dataset.TensorDataset(m_test_data, m_test_labels)
    val_y_dataset = dataset.TensorDataset(y_test_data, y_test_labels)

    train_dataloader = DataLoader(train_dataset, batch_size=args.batch_size, shuffle=True)
    val_m_dataloader = DataLoader(val_m_dataset, batch_size=args.batch_size, shuffle=False)
    val_y_dataloader = DataLoader(val_y_dataset, batch_size=args.batch_size, shuffle=False)

    input_dim = train_data.shape[2]
    print(f'模型输入维度 {input_dim}')

    model = FCN.FCN_POOL(input_dim, args.hidden_dim)
    model.cuda()

    optimizer = optim.Adam(model.parameters(), lr=args.lr)
    criterion = nn.MSELoss()
    num_epochs = args.epochs

    min_delta = 1e-4  # 损失变化的阈值
    patience = 5  # 检测到多少个连续epoch的损失变化小于阈值后停止训练

    best_loss = float('inf')
    patience_counter = 0

    for epoch in range(num_epochs):
        model.train()
        total_loss = 0
        for batch_idx, data_batch in enumerate(train_dataloader):
            predicates, targets = data_batch
            predicates, targets = predicates.cuda(), targets.cuda()
            predicates, targets = Variable(predicates), Variable(targets)
            optimizer.zero_grad()
            outputs = model(predicates)
            loss = criterion(outputs, targets.float())
            total_loss += loss.item()
            loss.backward()
            optimizer.step()
        average_loss = total_loss / len(train_dataloader)
        print(f'Epoch [{epoch + 1}/{num_epochs}], Loss: {average_loss}')

        # 检查是否停止训练
        if best_loss - average_loss < min_delta:
            patience_counter += 1
            if patience_counter >= patience:
                print(f"Training stopped early at epoch {epoch + 1} due to minimal loss improvement.")
                break
        else:
            best_loss = average_loss
            patience_counter = 0  # 重置计数器

    model.eval()
    print(f'=====>  mooccubex dataset validate  <=====')
    predictions_m = PredictUtils.fcn_pool(model, val_m_dataloader)
    metrics.print_q_error(predictions_m, m_test_labels)
    metrics.print_mean_error(predictions_m, m_test_labels)
    print(f'=====>  yelp dataset validate  <=====')
    predictions_y = PredictUtils.fcn_pool(model, val_y_dataloader)
    metrics.print_q_error(predictions_y, y_test_labels)
    metrics.print_mean_error(predictions_y, y_test_labels)


def parse_args():
    parser = argparse.ArgumentParser(
        description='FCN')
    parser.add_argument('--dataset_count', default=None,
                        help='dataset count, default all')
    parser.add_argument('--batch_size', default=128,
                        help='batch_size')
    parser.add_argument('--epochs', default=120, type=int,
                        help='number of total epochs to run')
    parser.add_argument('--input_dim', default=466,
                        help='input_dim')
    parser.add_argument('--hidden_dim', default=256,
                        help='lstm_hidden_dim')
    parser.add_argument('--lr', default=0.0001, type=float,
                        metavar='LR', help='initial learning rate')
    args = parser.parse_args()
    return args


if __name__ == '__main__':
    main()
