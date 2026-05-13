from common import FeatureData
import torch
import torch.nn as nn
from torch.utils.data import DataLoader
import model.lpce.sru as SRU
import argparse
from common import metrics
from torch.autograd import Variable
from torch.utils.data import dataset
from common import PredictUtils


def main():
    global args
    args = parse_args()
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

    # tables_feature, operations_feature, predicates_feature, labels
    train_tables, train_operations, train_predicates, train_labels = FeatureData.LPCEDataset('cardinality_train',
                                                                                             args.dataset_count).loadData()
    m_test_tables, m_test_operations, m_test_predicates, m_test_labels = FeatureData.LPCEDataset('cardinality_mcx_test',
                                                                                                 args.dataset_count).loadData()
    y_test_tables, y_test_operations, y_test_predicates, y_test_labels = FeatureData.LPCEDataset(
        'cardinality_yelp_test', args.dataset_count).loadData()

    print("数据加载完毕")
    train_dataset = dataset.TensorDataset(train_tables, train_operations, train_predicates, train_labels)
    val_m_dataset = dataset.TensorDataset(m_test_tables, m_test_operations, m_test_predicates, m_test_labels)
    val_y_dataset = dataset.TensorDataset(y_test_tables, y_test_operations, y_test_predicates, y_test_labels)

    train_dataloader = DataLoader(train_dataset, batch_size=args.batch_size, shuffle=True
                                  )
    val_m_dataloader = DataLoader(val_m_dataset, batch_size=args.batch_size, shuffle=False
                                  )
    val_y_dataloader = DataLoader(val_y_dataset, batch_size=args.batch_size, shuffle=False
                                  )

    model = SRU.SRU(256, 1024, 1024)
    optimizer = torch.optim.Adam(model.parameters(), lr=args.lr)

    criterion = nn.MSELoss()
    num_epochs = args.epochs

    min_delta = 1e-4  # 损失变化的阈值
    patience = 5  # 检测到多少个连续epoch的损失变化小于阈值后停止训练

    best_loss = float('inf')
    patience_counter = 0

    for epoch in range(num_epochs):
        model.train()
        total_loss = 0
        for tables_batch, operations_batch, predicates_batch, labels_batch in train_dataloader:
            optimizer.zero_grad()
            # Forward pass
            estimate_output = model(operations_batch, tables_batch, predicates_batch)

            predictions = estimate_output.squeeze()
            labels_batch = torch.tensor(labels_batch, dtype=torch.float32)
            labels_batch = labels_batch.to(device)
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

    model.eval()
    print(f'=====>  mooccubex dataset validate  <=====')
    predictions_m = PredictUtils.lpce(model, val_m_dataloader)
    metrics.print_q_error(predictions_m, m_test_labels)
    metrics.print_mean_error(predictions_m, m_test_labels)
    print(f'=====>  yelp dataset validate  <=====')
    predictions_y = PredictUtils.lpce(model, val_y_dataloader)
    metrics.print_q_error(predictions_y, y_test_labels)
    metrics.print_mean_error(predictions_y, y_test_labels)


def parse_args():
    parser = argparse.ArgumentParser(
        description='LPCE')
    parser.add_argument('--dataset_count', default=None,
                        help='dataset count, default all')
    parser.add_argument('--seed', default=42,
                        help='random seed (default: 42)')
    parser.add_argument('--batch_size', default=50,
                        help='batch_size')
    parser.add_argument('--epochs', default=120, type=int,
                        help='number of total epochs to run')
    parser.add_argument('--lr', default=0.0001, type=float,
                        metavar='LR', help='initial learning rate')
    args = parser.parse_args()
    return args


if __name__ == '__main__':
    main()
