from gensim.models import FastText
import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import Dataset, DataLoader
import re


class SkipGramDataset(Dataset):
    def __init__(self, vocab, word_to_ix, window_size):
        self.vocab = vocab
        self.word_to_ix = word_to_ix
        self.window_size = window_size
        self.data = self.generate_data()

    def generate_data(self):
        data = []
        for sentence in self.vocab:
            # 现在句子是单个词，不需要再拆分
            word = sentence
            for context_word in self.vocab:
                if word != context_word:
                    data.append((self.word_to_ix[word], self.word_to_ix[context_word]))
        return data

    def __len__(self):
        return len(self.data)

    def __getitem__(self, idx):
        return self.data[idx]


# model = FastText(vocab, vector_size=161, window=3, min_count=1, sg=1)
# model.save("skip-gram.bin")


# 定义Skip-Gram模型
class SkipGramModel(nn.Module):
    def __init__(self, vocab_size, embedding_dim):
        super(SkipGramModel, self).__init__()
        self.embeddings = nn.Embedding(vocab_size, embedding_dim)
        self.linear1 = nn.Linear(embedding_dim, 128)
        self.relu = nn.ReLU()
        self.linear2 = nn.Linear(128, vocab_size)

    def forward(self, input_word):
        embed = self.embeddings(input_word)
        out = self.linear1(embed)
        out = self.relu(out)
        out = self.linear2(out)
        return out


def train_skip_gram():
    file_name = './words.txt'
    vocab = []
    with open(file_name, 'r', encoding='ISO-8859-1') as file:
        for line in file:
            s = re.sub(r'\s+', '', line.strip())
            vocab.append(s)
    words = []
    for v in vocab:
        words.extend(v.split())

    word_to_ix = {word: i for i, word in enumerate(set(words))}
    ix_to_word = {i: word for word, i in word_to_ix.items()}

    # 定义窗口大小
    window_size = 2

    # 实例化数据集
    dataset = SkipGramDataset(vocab, word_to_ix, window_size)
    dataloader = DataLoader(dataset, batch_size=32, shuffle=True)

    # 设置超参数
    vocab_size = len(word_to_ix)
    embedding_dim = 161

    # 实例化模型
    model = SkipGramModel(vocab_size, embedding_dim)

    # 定义损失函数和优化器
    criterion = nn.CrossEntropyLoss()
    optimizer = optim.Adam(model.parameters(), lr=0.001)

    # 训练模型
    num_epochs = 50
    for epoch in range(num_epochs):
        for inputs, targets in dataloader:
            optimizer.zero_grad()
            outputs = model(inputs)
            loss = criterion(outputs, targets)
            loss.backward()
            optimizer.step()
        print(f'Epoch [{epoch + 1}/{num_epochs}], Loss: {loss.item()}')

    # 获取嵌入层权重
    embeddings = model.embeddings.weight.data

    # 输出一些词向量
    for word in vocab:
        print(f'{word}: {embeddings[word_to_ix[word]]}')


def train_fast_text():
    file_name = './words.txt'
    vocab = []
    with open(file_name, 'r', encoding='ISO-8859-1') as file:
        for line in file:
            s = re.sub(r'\s+', '', line.strip())
            vocab.append(s)
    model = FastText(vocab, vector_size=161, window=3, min_count=1, sg=1)
    model.save("skip-gram.bin")


if __name__ == '__main__':
    # train_skip_gram()
    train_fast_text()
    # model = FastText.load("skip-gram.bin")
    # print(model.wv[sql].shape)
