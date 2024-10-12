```json
{
  "uid": "PSI_GARNET",
  "centerId": 1, 
  "name": "PSI_GARNET",
  "path": "programs/PSI_BASE.mpc", 
  "compileParameters": [
    {
      "name": "P0_Data", 
      "limit": {}, 
      "required": false, 
      "limitType": "AUTO", 
      "posORflag": 0, 
      "description": "第0方数据", 
      "parameterType": "POS"
    }, 
    {
      "name": "P1_Data", 
      "limit": {}, 
      "required": false, 
      "limitType": "AUTO", 
      "posORflag": 1, 
      "description": "第1方数据", 
      "parameterType": "POS"
    }
  ], 
  "runtimeParameters": [
    {
      "name": "protocol", 
      "limit": {
        "defaultValue": "semi2k-party"
      }, 
      "required": false, 
      "limitType": "STRING", 
      "posORflag": null, 
      "description": "运行虚拟机", 
      "parameterType": "HYPER"
    }, 
    {
      "name": "PK", 
      "limit": {
        "defaultValue": "id"
      }, 
      "required": true, 
      "limitType": "STRING", 
      "posORflag": null, 
      "description": "主键", 
      "parameterType": "HYPER"
    }
  ]
}
```

```json
{
  "partInfo": [{
    "agentID":100,
    "part":1,
    "fileID":3
  }],
  "applicationId": 0,
  "centerId": 1,
  "compileParameters": {},
  "host": "10.176.34.171",
  "mpcId": "PSI_GARNET",
  "n": 2,
  "part": 0,
  "port": 6000,
  "runtimeParameters": {
    "PK": "案号",
    "protocol" : "semi2k-party"
  },
  "status": "INIT",
  "taskType": "GARNET_PSI",
  "uid": null
}
```

```json
{
  "uid": null, 
  "centerId": 1, 
  "name": "决策树训练",  
  "compileParameters": [
    {
      "name": "party_number", 
      "limit": {
        "max": 999, 
        "min": 999, 
        "defaultValue": 2
      }, 
      "required": true,
      "auto": false, 
      "limitType": "NUM", 
      "posORflag": 0, 
      "description": "共有几方参与", 
      "parameterType": "POS"
    }, 
    {
      "name": "feature_number", 
      "limit": {
        "max": 999, 
        "min": 999, 
        "defaultValue": 1
      }, 
      "required": true, 
      "auto": false, 
      "limitType": "NUM", 
      "posORflag": 1, 
      "description": "特征数", 
      "parameterType": "POS"
    },
    {
      "name": "ents.tree_h", 
      "limit": {
        "max": 999, 
        "min": 999, 
        "defaultValue": 4
      }, 
      "required": true, 
      "auto": false, 
      "limitType": "NUM", 
      "posORflag": 2, 
      "description": "树高", 
      "parameterType": "POS"
    },
    {
      "name": "ents.n_threads", 
      "limit": {
        "max": 999, 
        "min": 999, 
        "defaultValue": 4
      }, 
      "required": true, 
      "auto": false, 
      "limitType": "NUM", 
      "posORflag": 3, 
      "description": "使用的线程数", 
      "parameterType": "POS"
    },
    {
      "name": "sample_number_from_party_0", 
      "limit": {
        "max": 999, 
        "min": 999, 
        "defaultValue": 4
      }, 
      "required": true, 
      "auto": false, 
      "limitType": "NUM", 
      "posORflag": 4, 
      "description": "第0方数据", 
      "parameterType": "POS"
    },
    {
      "name": "sample_number_from_party_1", 
      "limit": {
        "max": 999, 
        "min": 999, 
        "defaultValue": 4
      }, 
      "required": true, 
      "auto": false, 
      "limitType": "NUM", 
      "posORflag": 5, 
      "description": "第1方数据", 
      "parameterType": "POS"
    },
    {
      "name": "Ring", 
      "limit": {
        "defaultValue": "64"
      }, 
      "required": false, 
      "auto": true, 
      "limitType": "STRING", 
      "posORflag": "-R", 
      "description": "所用环的大小", 
      "parameterType": "FLAG"
    }
  ], 
  "runtimeParameters": [
    {
      "name": "protocol", 
      "limit": {
        "defaultValue": "semi2k-party"
      }, 
      "required": true, 
      "auto": false, 
      "limitType": "STRING", 
      "posORflag": null, 
      "description": "运行虚拟机", 
      "parameterType": "FLAG"
    }
  ]
}
```

```json
{
  "partInfo": [{
    "agentID":5,
    "part":1,
    "fileID":25
  }],
  "applicationId": 0,
  "centerId": 1,
  "compileParameters": {
    "party_number": 2,
    "feature_number": 5,
    "ents.tree_h": 4,
    "ents.n_threads": 4,
    "sample_number_from_party_0":50,
    "sample_number_from_party_1":49
  },
  "host": "10.176.34.171",
  "mpcId": "",
  "mpcName": "string",
  "n": 2,
  "part": 0,
  "port": 6000,
  "runtimeParameters": {
    "protocol": "semi2k-with-conversion-party"
  },
  "status": "INIT",
  "taskType": "GARNET_MPC",
  "uid": null
}
```



```json
{
  "uid": null, 
  "centerId": "DAVEX-C1", 
  "name": "决策树推理",  
  "compileParameters": [
    {
      "name": "m", 
      "limit": {
        "max": 999, 
        "min": 1, 
        "defaultValue": 4
      }, 
      "required": true,
      "auto": false, 
      "limitType": "NUM", 
      "posORflag": 0, 
      "description": "特征维度", 
      "parameterType": "POS"
    }, 
    {
      "name": "test_samples", 
      "limit": {
        "max": 999, 
        "min": 1, 
        "defaultValue": 49
      }, 
      "required": true, 
      "auto": false, 
      "limitType": "NUM", 
      "posORflag": 1, 
      "description": "测试样本数", 
      "parameterType": "POS"
    },
    {
      "name": "label_number", 
      "limit": {
        "max": 999, 
        "min": 1, 
        "defaultValue": 3
      }, 
      "required": true, 
      "auto": false, 
      "limitType": "NUM", 
      "posORflag": 2, 
      "description": "标签种类数", 
      "parameterType": "POS"
    },
    {
      "name": "tree_h", 
      "limit": {
        "max": 9, 
        "min": 3, 
        "defaultValue": 4
      }, 
      "required": true, 
      "auto": false, 
      "limitType": "NUM", 
      "posORflag": 3, 
      "description": "树高", 
      "parameterType": "POS"
    },
    {
      "name": "Ring", 
      "limit": {
        "defaultValue": "64"
      }, 
      "required": false, 
      "auto": true, 
      "limitType": "STRING", 
      "posORflag": "-R", 
      "description": "所用环的大小", 
      "parameterType": "FLAG"
    }
  ], 
  "runtimeParameters": [
    {
      "name": "protocol", 
      "limit": {
        "defaultValue": "semi2k-party"
      }, 
      "required": true, 
      "auto": false, 
      "limitType": "STRING", 
      "posORflag": null, 
      "description": "运行虚拟机", 
      "parameterType": "FLAG"
    }
  ]
}
```

```json
{
    "agentId":"DAVEX-C1-GXX1",
    "fileId":"DAVEX-C1-GXX1-F2",
    "applicationId":"DAVEX-C1-AXX1"
}

```