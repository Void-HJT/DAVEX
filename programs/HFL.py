import secretflow as sf
import os
import argparse
# Check the version of your SecretFlow
print("The version of SecretFlow: {}".format(sf.__version__))

# In case you have a running secretflow runtime already.
# sf.init()  # 确保 SecretFlow 初始化

# sf.shutdown()

# 创建解析器
parser = argparse.ArgumentParser(description="指定结果文件夹的存储路径")
parser.add_argument("--result_dir", type=str, default="result", help="结果文件夹的路径")
args = parser.parse_args()

sf.init(parties=['DAVEX-C1', 'DAVEX-C1-G1','DAVEX-C1-G3'], address='10.176.34.173:9876')
# sf.init(["alice", "bob", "charlie"], address="local")
alice, bob, charlie = sf.PYU("DAVEX-C1"), sf.PYU("DAVEX-C1-G1"), sf.PYU("DAVEX-C1-G3")
# alice = sf.PYU('alice')
# bob = sf.PYU('bob')
# charlie = sf.PYU('charlie')


spu = sf.SPU(sf.utils.testing.cluster_def(["DAVEX-C1", "DAVEX-C1-G1"]))
# 创建结果文件夹
result_dir = args.result_dir
os.makedirs(result_dir, exist_ok=True)

# 判断文件夹是否创建成功
if os.path.exists(result_dir):
    print(f"文件夹 '{result_dir}' 已成功创建或已存在。")
else:
    print(f"文件夹 '{result_dir}' 创建失败。")

    pu = sf.SPU(sf.utils.testing.cluster_def(["alice", "bob"]))

from secretflow_fl.utils.simulation.datasets_fl import load_mnist

(x_train, y_train), (x_test, y_test) = load_mnist(
    parts={alice: 0.15, bob: 0.85},
    normalized_x=True,
    categorical_y=True,
    is_torch=False,
)

import numpy as np
from secretflow.utils.simulation.datasets import dataset

print(x_test)

mnist = np.load(dataset("mnist"), allow_pickle=True)
np.save('minst.npy',mnist)
image = mnist["x_train"]
label = mnist["y_train"]
from matplotlib import pyplot as plt


def create_conv_model(input_shape, num_classes, name="model"):
    def create_model():
        from tensorflow import keras
        from tensorflow.keras import layers

        # Create model
        model = keras.Sequential(
            [
                keras.Input(shape=input_shape),
                layers.Conv2D(32, kernel_size=(3, 3), activation="relu"),
                layers.MaxPooling2D(pool_size=(2, 2)),
                layers.Conv2D(64, kernel_size=(3, 3), activation="relu"),
                layers.MaxPooling2D(pool_size=(2, 2)),
                layers.Flatten(),
                layers.Dropout(0.5),
                layers.Dense(num_classes, activation="softmax"),
            ]
        )
        # Compile model
        model.compile(
            loss="categorical_crossentropy", optimizer="adam", metrics=["accuracy"]
        )
        return model

    return create_model


from secretflow.security.aggregation import SPUAggregator, SecureAggregator
from secretflow_fl.ml.nn import FLModel

num_classes = 10
input_shape = (28, 28, 1)
model = create_conv_model(input_shape, num_classes)

device_list = [alice, bob]

secure_aggregator = SecureAggregator(charlie, [alice, bob])
spu_aggregator = SPUAggregator(spu)

fed_model = FLModel(
    server=charlie,
    device_list=device_list,
    model=model,
    aggregator=secure_aggregator,
    strategy="fed_avg_w",
    backend="tensorflow",
)

history = fed_model.fit(
    x_train,
    y_train,
    validation_data=(x_test, y_test),
    epochs=10,
    sampler_method="batch",
    batch_size=128,
    aggregate_freq=1,
)

# Draw accuracy values for training & validation
plt.plot(history["global_history"]["accuracy"])
plt.plot(history["global_history"]["val_accuracy"])
plt.title("FLModel accuracy")
plt.ylabel("Accuracy")
plt.xlabel("Epoch")
plt.legend(["Train", "Valid"], loc="upper left")
# 保存图像
accuracy_path = os.path.join(result_dir, "accuracy.png")
plt.savefig(accuracy_path)

# 打印保存成功的提示信息
print(f"图像已成功保存至: {accuracy_path}")

plt.show()
plt.close()

# Draw loss for training & validation
plt.plot(history["global_history"]["loss"])
plt.plot(history["global_history"]["val_loss"])
plt.title("FLModel loss")
plt.ylabel("Loss")
plt.xlabel("Epoch")
plt.legend(["Train", "Valid"], loc="upper left")
plt.savefig(os.path.join(result_dir, "loss.png"))  # 保存图像
plt.show()
plt.close()

global_metric = fed_model.evaluate(x_test, y_test, batch_size=128)
print(global_metric)
with open(os.path.join(result_dir, "global_metric.txt"), "w") as f:
    f.write(str(global_metric))
