# DAVEX

使用下面的命令，来防止两个`application.yml`频繁更新。

```shell
git update-index --assume-unchanged DAVEX_center/src/main/resources/application.yml
git update-index --assume-unchanged DAVEX_agent/src/main/resources/application.yml
```

当`application.yml`有结构性更新时，使用下面的命令，重新追踪两个文件。

```shell
git update-index --no-assume-unchanged DAVEX_center/src/main/resources/application.yml
git update-index --no-assume-unchanged DAVEX_agent/src/main/resources/application.yml
```


# 安装隐语所需的虚拟环境
使用venv创建名为sfenv的环境并管理
```python
#DAVEX目录下创建虚拟环境 使用python3.10 
python3.10 -m venv sfenv 
#激活虚拟环境
source sfenv/bin/activate
#安装隐语和需要的库
pip install -U secretflow
pip install unidecode

```