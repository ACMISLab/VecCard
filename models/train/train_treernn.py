from common import FeatureData
import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import DataLoader
import argparse
from common import metrics
import model.treernn.treernn as TRNN
from common import PATH
import sys
from common import PredictUtils


def main():
    # args
    global args
    args = parse_args()
    print("Parsed Arguments:")
    print(args)
    ds_args = dataset_args()
    print("Data Set Arguments:")
    print(ds_args)
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    model = None

    m_test_trees, m_test_labels = FeatureData.TreeDataset('cardinality_mcx_test', args.dataset_count,
                                                          ds_args).loadData()
    y_test_trees, y_test_labels = FeatureData.TreeDataset('cardinality_yelp_test', args.dataset_count,
                                                          ds_args).loadData()
    val_m_dataset = FeatureData.SQLTreeDataset(m_test_trees, m_test_labels)
    val_y_dataset = FeatureData.SQLTreeDataset(y_test_trees, y_test_labels)

    val_m_dataloader = DataLoader(val_m_dataset, batch_size=args.batch_size, shuffle=False,
                                  collate_fn=FeatureData.collate_fn)
    val_y_dataloader = DataLoader(val_y_dataset, batch_size=args.batch_size, shuffle=False,
                                  collate_fn=FeatureData.collate_fn)

    if args.retrain:
        train_trees, train_labels = FeatureData.TreeDataset('cardinality_train', args.dataset_count, ds_args).loadData()

        print(
            f"====> 训练数据集加载完毕，共 {len(train_labels)} 条数据, 最大值: {torch.max(train_labels).item()}, 最小值: {torch.min(train_labels).item()}")

        # 将树和标签转化为数据集形式
        train_dataset = FeatureData.SQLTreeDataset(train_trees, train_labels)

        # 创建数据加载器
        train_dataloader = DataLoader(train_dataset, batch_size=args.batch_size, shuffle=True,
                                      collate_fn=FeatureData.collate_fn)

        input_dim = train_trees[0].getChildNode()[0].shape[0]
        print(f'模型输入纬度 {input_dim}')
        model = TRNN.QueryRNN(input_dim, args.mem_dim, args.hidden_dim)

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

        if args.save_model:
            print(f'=====>  save model  <=====')
            model_path = str(PATH.model_path / (args.model_name + ".pth"))
            torch.save(model.state_dict(), model_path)
    else:
        model_path = PATH.model_path / (args.model_name + ".pth")
        if model_path.exists():
            input_dim = m_test_trees[0].getChildNode()[0].shape[0]
            print(f'模型输入纬度 {input_dim}')
            model = TRNN.QueryRNN(input_dim, args.mem_dim, args.hidden_dim)
            model = model.to(device)
            model.load_state_dict(torch.load(model_path, map_location=device))
        else:
            print(f"文件 {model_path} 不存在")
            sys.exit()

    model.eval()
    print(f'=====>  mooccubex dataset validate  <=====')
    predictions_m = PredictUtils.tree(model, val_m_dataloader, device)
    metrics.print_q_error(predictions_m, m_test_labels)
    metrics.print_mean_error(predictions_m, m_test_labels)
    print("\n")
    print(f'=====>  yelp dataset validate  <=====')
    predictions_y = PredictUtils.tree(model, val_y_dataloader, device)
    metrics.print_q_error(predictions_y, y_test_labels)
    metrics.print_mean_error(predictions_y, y_test_labels)


def parse_args():
    parser = argparse.ArgumentParser(
        description='PyTorch TreeRNN')
    parser.add_argument('--model_name', default="tree-rnn",
                        help='model name')
    parser.add_argument('--dataset_count', default=None,
                        help='dataset count, default all')
    parser.add_argument('--seed', default=42,
                        help='random seed  (default: 42)')
    parser.add_argument('--save_model', default=True,
                        help='is save model')
    parser.add_argument('--retrain', default=False,
                        help='is re train')
    parser.add_argument('--batch_size', default=128,
                        help='batch_size')
    # model parser
    # input_dim, mem_dim, hidden_dim
    parser.add_argument('--epochs', default=120, type=int,
                        help='number of total epochs to run')
    parser.add_argument('--input_dim', default=466,
                        help='input_dim')
    parser.add_argument('--mem_dim', default=32,
                        help='mem_dim')
    parser.add_argument('--hidden_dim', default=80,
                        help='hidden_dim')
    parser.add_argument('--lr', default=0.0001, type=float,
                        metavar='LR', help='initial learning rate')
    args = parser.parse_args()
    return args


def dataset_args():
    parser = argparse.ArgumentParser(
        description='data set args ')
    parser.add_argument('--meta', default=True,
                        help='is use meta encode')
    parser.add_argument('--bitmap', default=False,
                        help='is use bitmap encode')
    parser.add_argument('--incre_meta', default=False,
                        help='is use incre meta encode')
    parser.add_argument('--incre_bitmap', default=False,
                        help='is use incre bitmap encode')
    args = parser.parse_args()
    return args


if __name__ == '__main__':
    main()
