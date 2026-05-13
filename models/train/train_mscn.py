from common import FeatureData
import torch
import torch.nn as nn
from torch.utils.data import DataLoader
import model.mscn.mscn as MSCN
import argparse
from common import metrics
from torch.autograd import Variable
from common import PredictUtils


def main():
    # args
    global args
    args = parse_args()
    train_data, train_labels = FeatureData.MSCNDataset('cardinality_train', args.dataset_count, True).loadData()
    m_test_data, m_test_labels = FeatureData.MSCNDataset('cardinality_mcx_test', args.dataset_count,
                                                         True).loadData()
    y_test_data, y_test_labels = FeatureData.MSCNDataset('cardinality_yelp_test', args.dataset_count,
                                                         True).loadData()
    print(
        f"====> 训练数据集加载完毕，共 {len(train_labels)} 条数据, 最大值: {torch.max(train_labels).item()}, 最小值: {torch.min(train_labels).item()}")

    print(
        f"====> 测试数据集1 加载完毕，共 {len(m_test_labels)} 条数据, 最大值: {torch.max(m_test_labels).item()}, 最小值: {torch.min(m_test_labels).item()}")

    print(
        f"====> 测试数据集2 加载完毕，共 {len(y_test_labels)} 条数据, 最大值: {torch.max(y_test_labels).item()}, 最小值: {torch.min(y_test_labels).item()}")

    train_data_loader = DataLoader(train_data, batch_size=args.batch_size)
    val_m_dataloader = DataLoader(m_test_data, batch_size=args.batch_size)
    val_y_dataloader = DataLoader(y_test_data, batch_size=args.batch_size)

    sample_feats = 1007
    predicate_feats = 163

    model = MSCN.SetConv(sample_feats, predicate_feats, 163)

    optimizer = torch.optim.Adam(model.parameters(), lr=args.lr)
    model.cuda()

    # Loss Function
    # criterion = metrics.CELossFunction()
    criterion = nn.MSELoss()
    num_epochs = args.epochs

    min_delta = 1e-4  # 损失变化的阈值
    patience = 5  # 检测到多少个连续epoch的损失变化小于阈值后停止训练

    best_loss = float('inf')
    patience_counter = 0
    for epoch in range(num_epochs):
        model.train()
        total_loss = 0
        for batch_idx, data_batch in enumerate(train_data_loader):
            samples, predicates, targets, sample_masks, predicate_masks = data_batch
            samples, predicates, targets = samples.cuda(), predicates.cuda(), targets.cuda()
            sample_masks, predicate_masks = sample_masks.cuda(), predicate_masks.cuda()
            samples, predicates, targets = Variable(samples), Variable(predicates), Variable(targets)
            sample_masks, predicate_masks = Variable(sample_masks), Variable(predicate_masks)
            optimizer.zero_grad()
            outputs = model(samples, predicates, sample_masks, predicate_masks)
            loss = criterion(outputs, targets.float())
            total_loss += loss.item()
            loss.backward()
            optimizer.step()
        average_loss = total_loss / len(train_data_loader)
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
    predictions_m = PredictUtils.mscn(model, val_m_dataloader)
    metrics.print_q_error(predictions_m, m_test_labels)
    metrics.print_mean_error(predictions_m, m_test_labels)
    print(f'=====>  yelp dataset validate  <=====')
    predictions_y = PredictUtils.mscn(model, val_y_dataloader)
    metrics.print_q_error(predictions_y, y_test_labels)
    metrics.print_mean_error(predictions_y, y_test_labels)


def parse_args():
    parser = argparse.ArgumentParser(
        description='PyTorch TreeLSTM')
    parser.add_argument('--dataset_count', default=None,
                        help='dataset count, default all')
    parser.add_argument('--seed', default=42,
                        help='random seed (default: 42)')
    parser.add_argument('--batch_size', default=128,
                        help='batch_size')
    parser.add_argument('--epochs', default=120, type=int,
                        help='number of total epochs to run')
    parser.add_argument('--lr', default=0.0001, type=float,
                        metavar='LR', help='initial learning rate')
    args = parser.parse_args()
    return args


if __name__ == '__main__':
    main()
