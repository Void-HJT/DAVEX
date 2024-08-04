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


```json
{
  "centerId": 1,
  "compileParameters": [
    {
        "name": "P0_Data",
        "limit": {
            "max": 999,
            "min": 999,
            "defaultValue": 1
        },
        "required": true,
        "limitType": "NUM",
        "posORflag": 0,
        "description": "第0方数据",
        "parameterType": "POS"
    },
    {
        "name": "P1_Data",
        "limit": {
            "max": 999,
            "min": 999,
            "defaultValue": 1
        },
        "required": true,
        "limitType": "NUM",
        "posORflag": 1,
        "description": "第1方数据",
        "parameterType": "POS"
    }
],
  "name": "PSI",
  "path": "string",
  "runtimeParameters": [{"name": "protocol", "limit": {"defaultValue": "semi2k-party"}, "required": true, "limitType": "STRING", "posORflag": "abc", "description": "运行虚拟机", "parameterType": "FLAG"}],
  "uid": 2
}
```

```json
{
  "agentID2fileID": {
    "100":[1,2]
  },
  "applicationId": 0,
  "centerId": 1,
  "compileParameters": {
    "P0_Data": 10,
    "P1_Data": 10
  },
  "dataId": 1,
  "host": "10.176.37.50",
  "inputID": 1,
  "mpcId": 2,
  "mpcName": "string",
  "n": 2,
  "part": 0,
  "port": 6000,
  "runtimeParameters": {
    "protocol": "semi2k-party",
    "PK": "案号"
  },
  "status": "COMPILING",
  "taskType": "GARNET_PSI",
  "uid": null
}
```



```json
[
    {
        "name": "P0_Data",
        "limit": {
            "max": 999,
            "min": 999,
            "defaultValue": 1
        },
        "required": true,
        "limitType": "NUM",
        "posORflag": 0,
        "description": "第0方数据",
        "parameterType": "POS"
    },
    {
        "name": "P1_Data",
        "limit": {
            "max": 999,
            "min": 999,
            "defaultValue": 1
        },
        "required": true,
        "limitType": "NUM",
        "posORflag": 1,
        "description": "第1方数据",
        "parameterType": "POS"
    }
]



[
    {
        "name": "protocol",
        "limit": {
            "defaultValue": "semi2k-party"
        },
        "required": true,
        "limitType": "STRING",
        "posORflag": "abc",
        "description": "运行虚拟机",
        "parameterType": "FLAG"
    }
]
```