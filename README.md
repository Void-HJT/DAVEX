# DVE

使用下面的命令，来防止两个`application.yml`频繁更新。

```shell
git update-index --assume-unchanged DVE_center/src/main/resources/application.yml
git update-index --assume-unchanged DVE_agent/src/main/resources/application.yml
```

当`application.yml`有结构性更新时，使用下面的命令，重新追踪两个文件。

```shell
git update-index --no-assume-unchanged DVE_center/src/main/resources/application.yml
git update-index --no-assume-unchanged DVE_agent/src/main/resources/application.yml
```
