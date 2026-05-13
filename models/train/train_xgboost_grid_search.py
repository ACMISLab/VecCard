import numpy as np
import xgboost as xgb
from common import FeatureData
from sklearn.model_selection import GridSearchCV


def printQerror(preds, labels):
    preds = np.expm1(preds)
    labels = np.expm1(labels)
    qerror = []
    for i in range(len(preds)):
        if preds[i] > labels[i]:
            qerror.append(preds[i] / labels[i])
        else:
            pred_value = preds[i]
            if pred_value == 0:
                pred_value += 1e-8
            qerror.append(labels[i] / pred_value)
    print("Mean: {}".format(np.mean(qerror)))
    print("Median: {}".format(np.median(qerror)))
    print("Max: {}".format(np.max(qerror)))
    print("90th percentile: {}".format(np.percentile(qerror, 90)))
    print("95th percentile: {}".format(np.percentile(qerror, 95)))
    print("99th percentile: {}".format(np.percentile(qerror, 99)))


def printOtherIndicators(preds, labels):
    preds = np.expm1(preds)
    labels = np.expm1(labels)
    mape = np.mean(np.abs((labels - preds) / labels)) * 100
    smape = np.mean(2.0 * np.abs(preds - labels) / (np.abs(preds) + np.abs(labels))) * 100
    rmse = np.sqrt(np.mean((preds - labels) ** 2))
    mae = np.mean(np.abs(preds - labels))
    print(f"Mean Absolute Percentage Error (MAPE): {mape}%")
    print(f"Symmetric Mean Absolute Percentage Error (SMAPE): {smape}%")
    print(f"Root Mean Squared Error (RMSE): {rmse}")
    print(f"Mean Absolute Error (MAE): {mae}")


X_train, y_train = FeatureData.MLDataset('cardinality_train', None).loadData()
X_test, y_test = FeatureData.MLDataset('cardinality_test', None).loadData()

d_train = xgb.DMatrix(X_train, label=y_train)
d_test = xgb.DMatrix(X_test, label=y_test)

# 设置参数网格
param_grid = {
    'objective': ['reg:squarederror'],
    'max_depth': [3, 4, 5, 6, 7, 8],
    'eta': [0.01, 0.05, 0.1, 0.2],
    'subsample': [0.7, 0.8, 0.9, 1.0],
    'colsample_bytree': [0.7, 0.8, 0.9, 1.0]
}

# 网格搜索
xgb_model = xgb.XGBRegressor(objective='reg:squarederror', nthread=4, seed=42)

# 设置GridSearchCV
grid_search = GridSearchCV(estimator=xgb_model, param_grid=param_grid, cv=5, scoring='neg_mean_squared_error')

# 训练模型
grid_search.fit(X_train, y_train)

# 输出最佳参数
print("最佳参数：", grid_search.best_params_)
num_round = 50

# 使用最佳参数训练模型
best_xgb_model = xgb.XGBRegressor(**grid_search.best_params_, objective='reg:squarederror', nthread=4, seed=42)
best_xgb_model.fit(X_train, y_train)

# 获取每个输入向量在 XGBoost 模型中的预测结果
y_predictions = []
for x in X_test:
    # 将单个样本转换为 DMatrix 格式
    d_matrix_single = xgb.DMatrix(x.reshape(1, -1))
    # 预测该样本在每棵树下的输出值
    pred_per_tree = best_xgb_model.predict(d_matrix_single, output_margin=True, iteration_range=(0, num_round))

    # 计算等权重（因为我们没有每棵树的特定信息）
    tree_weights = np.ones_like(pred_per_tree) / len(pred_per_tree)

    # 计算加权平均值
    weighted_average_prediction_per_sample = np.sum(pred_per_tree * tree_weights)
    y_predictions.append(weighted_average_prediction_per_sample)

printQerror(y_predictions, y_test)
printOtherIndicators(y_predictions, y_test)

