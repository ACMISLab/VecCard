from gensim.models import FastText

# 加载模型
model = FastText.load("fasttext_model.bin")

print(model.wv["Meridian"])
