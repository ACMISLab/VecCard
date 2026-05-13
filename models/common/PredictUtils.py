import torch
from torch.autograd import Variable


def tree(model, test_dataloader, device):
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


def fcn_pool(model, data_loader):
    preds = []
    for batch_idx, data_batch in enumerate(data_loader):
        predicates, targets = data_batch
        predicates, targets = predicates.cuda(), targets.cuda()
        predicates, targets = Variable(predicates), Variable(targets)
        outputs = model(predicates)
        for i in range(outputs.data.shape[0]):
            preds.append(outputs.data[i])
    return torch.tensor(preds, dtype=torch.float32)


def lpce(model, test_dataloader):
    predictions = []  # 初始化一个列表来存储预测结果
    with torch.no_grad():
        for tables_batch, operations_batch, predicates_batch, labels_batch in test_dataloader:
            estimate_output = model(operations_batch, tables_batch, predicates_batch)
            batch_predictions = estimate_output.squeeze()
            predictions.extend(batch_predictions.cpu().numpy())
    return predictions


def mscn(model, data_loader):
    preds = []
    for batch_idx, data_batch in enumerate(data_loader):
        samples, predicates, targets, sample_masks, predicate_masks = data_batch
        samples, predicates, targets = samples.cuda(), predicates.cuda(), targets.cuda()
        sample_masks, predicate_masks = sample_masks.cuda(), predicate_masks.cuda()
        samples, predicates, targets = Variable(samples), Variable(predicates), Variable(targets)
        sample_masks, predicate_masks = Variable(sample_masks), Variable(predicate_masks)
        outputs = model(samples, predicates, sample_masks, predicate_masks)
        for i in range(outputs.data.shape[0]):
            preds.append(outputs.data[i])
    return torch.tensor(preds, dtype=torch.float32)


def lwnn(model, test_dataloader, device):
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


def vscnn(model, test_dataloader, device):
    predictions = []  # 初始化一个列表来存储预测结果
    with torch.no_grad():
        for tables_batch, predicates_batch, _ in test_dataloader:
            tables_batch = tables_batch.to(device)
            predicates_batch = predicates_batch.to(device)
            batch_predictions = []  # 创建一个新列表来存储当前批次的预测结果
            for table, predicate in zip(tables_batch, predicates_batch):
                output = model(table, predicate)
                batch_predictions.append(output)
            predictions.extend(batch_predictions)
    predictions = torch.stack(predictions).squeeze()
    return predictions