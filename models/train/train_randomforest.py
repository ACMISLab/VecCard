import numpy as np
from sklearn.ensemble import RandomForestRegressor
from common import FeatureData
from common import metrics


X_train, y_train = FeatureData.MLDataset('cardinality_train',
                                         None).loadData()
X_test, y_test = FeatureData.MLDataset('cardinality_test',
                                       None).loadData()

# 定义随机森林模型的参数
t = 120  # 二叉树的数量
v = 7  # 每棵树的最大叶子数

# 初始化随机森林回归模型
rf_model = RandomForestRegressor(
    n_estimators=t,  # 树的数量
    max_leaf_nodes=v,  # 每棵树的最大叶子数
    random_state=42,
    criterion='squared_error'  # 使用平方误差作为分裂标准
)

# 训练随机森林模型
rf_model.fit(X_train, y_train)

# 获取每个输入向量在随机森林中的预测结果
y_predictions = []
for x in X_test:
    # 计算每个样本的预测值
    individual_tree_predictions = []
    for tree in rf_model.estimators_:
        # 获取输入向量到达的叶节点索引
        leaf_index = tree.apply(x.reshape(1, -1))[0]
        # 获取叶节点的预测值
        leaf_value = tree.tree_.value[leaf_index][0][0]
        individual_tree_predictions.append(leaf_value)

    # 计算每棵树的叶节点预测值的平均值
    average_prediction_per_sample = np.mean(individual_tree_predictions)
    y_predictions.append(average_prediction_per_sample)

metrics.print_q_error(y_predictions, y_test)
metrics.print_mean_error(y_predictions, y_test)
