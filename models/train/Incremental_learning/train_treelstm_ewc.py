from common import FeatureData, ewc
import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import DataLoader
import model.treelstm.treelstm as TLSTM
import argparse
from common import metrics
from common import PATH
from common.ElasticWeightConsolidation import EWC


def predict(model, test_dataloader, device):
    model.eval()
    predictions = []  # 初始化一个列表来存储预测结果
    with torch.no_grad():
        for trees_batch, _ in test_dataloader:
            trees_batch = [tree.to(device) for tree in trees_batch]
            batch_predictions = []  # 创建一个新列表来存储当前批次的预测结果
            for tree in trees_batch:
                output = model(tree)
                batch_predictions.append(output)
            predictions.extend(batch_predictions)
    predictions = torch.stack(predictions).squeeze()
    return predictions


def main():
    # args
    global args
    args = parse_args()
    print("Parsed Arguments:")
    print(args)
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

    ds_args = dataset_args()
    print("Data Set Arguments:")
    print(ds_args)

    # task a
    train_trees_a, train_labels_a = FeatureData.TreeDataset('cardinality_train', args.dataset_count, ds_args).loadData()
    test_trees_a, test_labels_a = FeatureData.TreeDataset('cardinality_test', args.dataset_count, ds_args).loadData()

    # task b
    train_trees_b, train_labels_b = FeatureData.TreeDataset('task_4_train',
                                                            args.dataset_count, ds_args).loadData()
    test_trees_b, test_labels_b = FeatureData.TreeDataset('task_4_test',
                                                          args.dataset_count, ds_args).loadData()
    if not train_trees_a or not train_trees_b:
        return

    # 将树和标签转化为数据集形式  task a
    train_dataset_a = FeatureData.SQLTreeDataset(train_trees_a, train_labels_a)
    val_dataset_a = FeatureData.SQLTreeDataset(test_trees_a, test_labels_a)

    train_dataset_b = FeatureData.SQLTreeDataset(train_trees_b, train_labels_b)
    val_dataset_b = FeatureData.SQLTreeDataset(test_trees_b, test_labels_b)

    # 创建数据加载器
    train_dataloader_a = DataLoader(train_dataset_a, batch_size=args.batch_size, shuffle=True,
                                    collate_fn=FeatureData.collate_fn)
    val_dataloader_a = DataLoader(val_dataset_a, batch_size=args.batch_size, shuffle=False,
                                  collate_fn=FeatureData.collate_fn)

    train_dataloader_b = DataLoader(train_dataset_b, batch_size=args.batch_size, shuffle=True,
                                    collate_fn=FeatureData.collate_fn)
    val_dataloader_b = DataLoader(val_dataset_b, batch_size=args.batch_size, shuffle=False,
                                  collate_fn=FeatureData.collate_fn)

    input_dim = train_trees_a[0].getChildNode()[0].shape[0]

    model = TLSTM.TreeLSTM(input_dim, args.embedding_hidden_dim, args.lstm_hidden_dim,
                           args.estimation_hidden_dim, args.attention, args.dropout)
    model_path = PATH.model_path / (args.model_name + ".pth")
    model = model.to(device)
    model.load_state_dict(torch.load(model_path, map_location=device))
    model.eval()

    print("=====================> validate task a <=====================")
    predictions_a = predict(model, val_dataloader_a, device)
    metrics.print_q_error(predictions_a, test_labels_a)
    metrics.print_mean_error(predictions_a, test_labels_a)

    print("=====================> validate task b <=====================")
    predictions_b = predict(model, val_dataloader_b, device)
    metrics.print_q_error(predictions_b, test_labels_b)
    metrics.print_mean_error(predictions_b, test_labels_b)

    print("=====================> calculate the fisher matrix <=====================")

    ewc = EWC(model, train_dataloader_a, device)
    ewc.estimate_ewc_params()


    print("===============> EWC Incremental Learning <===============")
    min_delta = 1e-4  # 损失变化的阈值
    patience = 5  # 检测到多少个连续epoch的损失变化小于阈值后停止训练

    best_loss = float('inf')
    patience_counter = 0
    optimizer = optim.Adam(model.parameters(), lr=args.lr, weight_decay=args.weight_decay)
    criterion = nn.MSELoss()
    num_epochs = args.epochs

    for epoch in range(num_epochs):
        model.eval()
        total_loss_b = 0
        for trees_batch, labels_batch in train_dataloader_b:
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
            loss = criterion(predictions, labels_batch) + ewc.compute_ewc_loss(args.weight)
            loss.backward()
            optimizer.step()
            total_loss_b += loss.item()
        average_loss = total_loss_b / len(train_dataloader_b)
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

    print("=====================> validate task a <=====================")
    predictions = predict(model, val_dataloader_a, device)
    metrics.print_q_error(predictions, test_labels_a)
    metrics.print_mean_error(predictions, test_labels_a)

    print("=====================> validate task b <=====================")
    predictions_b = predict(model, val_dataloader_b, device)
    metrics.print_q_error(predictions_b, test_labels_b)
    metrics.print_mean_error(predictions_b, test_labels_b)


def parse_args():
    parser = argparse.ArgumentParser(
        description='PyTorch TreeLSTM')
    parser.add_argument('--model_name', default="tree-lstm-attention",
                        help='model name')
    parser.add_argument('--dataset_count', default=None,
                        help='dataset count, default all')
    parser.add_argument('--seed', default=42,
                        help='random seed (default: 42)')
    parser.add_argument('--save_model', default=True,
                        help='is save model')
    parser.add_argument('--retrain', default=False,
                        help='is re train')
    parser.add_argument('--batch_size', default=128,
                        help='batch_size')
    parser.add_argument("--weight-decay", type=float, default=1e-4,
                        help="Weight decay for L2 regularization")
    # model parser
    parser.add_argument('--epochs', default=120, type=int,
                        help='number of total epochs to run')
    parser.add_argument('--dropout', default=0.5,
                        help='dropout')
    parser.add_argument('--input_dim', default=176,
                        help='input_dim')
    parser.add_argument('--embedding_hidden_dim', default=384,
                        help='embedding_hidden_dim')
    parser.add_argument('--lstm_hidden_dim', default=256,
                        help='lstm_hidden_dim')
    parser.add_argument('--estimation_hidden_dim', default=128,
                        help='estimation_hidden_dim')
    parser.add_argument('--lr', default=0.0001, type=float,
                        metavar='LR', help='initial learning rate')
    parser.add_argument('--weight', default=100000,
                        help='weight')
    parser.add_argument('--attention', default=True, type=bool,
                        help='use attention')
    args = parser.parse_args()
    return args


def dataset_args():
    parser = argparse.ArgumentParser(
        description='data set args ')
    parser.add_argument('--meta', default=True,
                        help='is use meta encode')
    parser.add_argument('--bitmap', default=False,
                        help='is use bitmap encode')
    parser.add_argument('--incre_meta', default=True,
                        help='is use incre meta encode')
    parser.add_argument('--incre_bitmap', default=False,
                        help='is use incre bitmap encode')
    args = parser.parse_args()
    return args


if __name__ == '__main__':
    main()
