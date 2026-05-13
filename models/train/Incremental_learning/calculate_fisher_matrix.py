from common import FeatureData
import torch
from torch.utils.data import DataLoader
import model.treelstm.treelstm as TLSTM
import argparse
from common import PATH
import time
from common.ElasticWeightConsolidation import EWC


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
    # 将树和标签转化为数据集形式  task a
    train_dataset_a = FeatureData.SQLTreeDataset(train_trees_a, train_labels_a)

    train_dataloader_a = DataLoader(train_dataset_a, batch_size=args.batch_size, shuffle=True,
                                    collate_fn=FeatureData.collate_fn)

    input_dim = train_trees_a[0].getChildNode()[0].shape[0]
    print("=======================> 开始加载模型")
    model = TLSTM.TreeLSTM(input_dim, args.embedding_hidden_dim, args.lstm_hidden_dim,
                           args.estimation_hidden_dim, args.attention, args.dropout)
    model_path = PATH.model_path / (args.model_name + ".pth")
    model = model.to(device)
    model.load_state_dict(torch.load(model_path, map_location=device))
    # 设为评估模式
    model.eval()
    print("=======================> 开始计算信息矩阵 ")
    start_time = time.time()
    ewc = EWC(model, train_dataloader_a, device)
    ewc.estimate_ewc_params()
    end_time = time.time()
    run_time = end_time - start_time
    print("代码运行时间：", run_time, "秒")
    print(ewc.get_fisher())
    print(ewc.compute_ewc_loss(1000))


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
    parser.add_argument('--input_dim', default=466,
                        help='input_dim')
    parser.add_argument('--embedding_hidden_dim', default=384,
                        help='embedding_hidden_dim')
    parser.add_argument('--lstm_hidden_dim', default=256,
                        help='lstm_hidden_dim')
    parser.add_argument('--estimation_hidden_dim', default=128,
                        help='estimation_hidden_dim')
    parser.add_argument('--lr', default=0.0001, type=float,
                        metavar='LR', help='initial learning rate')
    parser.add_argument('--attention', default=False, type=bool,
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
    parser.add_argument('--incre_meta', default=False,
                        help='is use incre meta encode')
    parser.add_argument('--incre_bitmap', default=False,
                        help='is use incre bitmap encode')
    args = parser.parse_args()
    return args


if __name__ == '__main__':
    main()
