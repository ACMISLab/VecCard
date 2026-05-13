import numpy as np
import xgboost as xgb
from common import FeatureData
from common import metrics


X_train, y_train = FeatureData.MLDataset('cardinality_train',
                                         None).loadData()
X_test, y_test = FeatureData.MLDataset('cardinality_test',
                                       None).loadData()

d_train = xgb.DMatrix(X_train, label=y_train)
d_test = xgb.DMatrix(X_test, label=y_test)

# 设置 XGBoost 参数
params = {
    'objective': 'reg:squarederror',  # 使用平方误差作为损失函数
    'max_depth': 7,  # 每棵树的最大深度
    'eta': 0.0005,  # 学习率
    'nthread': 32,  # 使用多少线程来训练
    'seed': 42,
    'colsample_bytree': 0.7,
    'subsample': 0.8
}

# 训练 XGBoost 模型
num_round = 120  # 树的数量（即提升迭代次数）
xgb_model = xgb.train(params, d_train, num_boost_round=num_round)

# 获取每个输入向量在 XGBoost 模型中的预测结果
y_predictions = []
for x in X_test:
    # 将单个样本转换为 DMatrix 格式
    d_matrix_single = xgb.DMatrix(x.reshape(1, -1))
    # 预测该样本在每棵树下的输出值
    pred_per_tree = xgb_model.predict(d_matrix_single, output_margin=True, iteration_range=(0, num_round))

    # 计算等权重（因为我们没有每棵树的特定信息）
    tree_weights = np.ones_like(pred_per_tree) / len(pred_per_tree)

    # 计算加权平均值
    weighted_average_prediction_per_sample = np.sum(pred_per_tree * tree_weights)
    y_predictions.append(weighted_average_prediction_per_sample)

metrics.print_q_error(y_predictions, y_test)
metrics.print_mean_error(y_predictions, y_test)
