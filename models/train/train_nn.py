from common import FeatureData
import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import DataLoader
import argparse
from common import metrics
import model.lwnn.nn as NN
from common import PredictUtils

def main():
    # args
    global args
    args = parse_args()
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

    train_trees, train_labels = FeatureData.MLDataset('cardinality_train', args.dataset_count).loadTensorData()
    y_test_trees, y_test_labels = FeatureData.MLDataset('cardinality_yelp_test', args.dataset_count).loadTensorData()
    print(
        f"====> 训练数据集加载完毕，共 {len(train_labels)} 条数据, 最大值: {torch.max(train_labels).item()}, 最小值: {torch.min(train_labels).item()}")

    train_dataset = FeatureData.SQLTreeDataset(train_trees, train_labels)
    val_y_dataset = FeatureData.SQLTreeDataset(y_test_trees, y_test_labels)

    # 创建数据加载器
    train_dataloader = DataLoader(train_dataset, batch_size=args.batch_size, shuffle=True,
                                  collate_fn=FeatureData.collate_fn)
    val_y_dataloader = DataLoader(val_y_dataset, batch_size=args.batch_size, shuffle=False,
                                  collate_fn=FeatureData.collate_fn)

    model = NN.MLP(args.input_dim, args.hidden_dim)
    optimizer = optim.Adam(model.parameters(), lr=args.lr)

    # Loss Function
    # criterion = treelstm.CELossFunction()
    criterion = nn.MSELoss()
    num_epochs = args.epochs

    min_delta = 1e-6  # 损失变化的阈值
    patience = 5  # 检测到多少个连续epoch的损失变化小于阈值后停止训练

    best_loss = float('inf')
    patience_counter = 0

    for epoch in range(num_epochs):
        model.train()
        total_loss = 0
        for trees_batch, labels_batch in train_dataloader:
            trees_batch = [tree.to(device) for tree in trees_batch]
            labels_batch = labels_batch.to(device)
            optimizer.zero_grad()
            # Forward pass
            predictions = []
            for tree in trees_batch:
                tree = tree.to(device)
                output = model(tree)
                predictions.append(output)
            predictions = torch.stack(predictions).squeeze()
            labels_batch = torch.tensor(labels_batch, dtype=torch.float32)
            loss = criterion(predictions, labels_batch)
            loss.backward()
            optimizer.step()
            total_loss += loss.item()

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
    #
    model.eval()
    print(f'=====>  yelp dataset validate  <=====')
    predictions_y = PredictUtils.lwnn(model, val_y_dataloader, device)
    metrics.print_q_error(predictions_y, y_test_labels)
    metrics.print_mean_error(predictions_y, y_test_labels)


def parse_args():
    parser = argparse.ArgumentParser(
        description='PyTorch TreeLSTM')
    parser.add_argument('--dataset_count', default=None,
                        help='dataset count, default all')
    parser.add_argument('--save_model', default=False,
                        help='is save model')
    parser.add_argument('--batch_size', default=1,
                        help='batch_size')
    # model parser
    parser.add_argument('--epochs', default=120, type=int,
                        help='number of total epochs to run')
    parser.add_argument('--input_dim', default=6,
                        help='input_dim')
    parser.add_argument('--hidden_dim', default=1024,
                        help='hidden_dim')
    parser.add_argument('--lr', default=0.0005, type=float,
                        metavar='LR', help='initial learning rate')
    args = parser.parse_args()
    return args


if __name__ == '__main__':
    main()
