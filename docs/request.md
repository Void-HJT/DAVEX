```json
{
  "uid": "PSI_GARNET", //字符串 不需要
  "centerId": 1, //
  "name": "PSI_GARNET",//前端指定 
  "path": "programs/PSI_BASE.mpc",//无视 
  "compileParameters": [
    {
      "name": "P0_Data", //前端指定
      "limit": {//
        "max": 999, 
        "min": 999, 
        "defaultValue": 1
      }, 
      "required": true, 
      "auto": false, 
      "limitType": "NUM", //选类型 NUM STRING ENUM
      //枚举是一个数组 value
      "posORflag": 0, //先选 POS 012345 FLAG -I
      "description": "第0方数据", //前端手动填
      "parameterType": "POS"//先选 POS FLAG
    }, 
    {
      "name": "P1_Data", 
      "limit": {
        "max": 999, 
        "min": 999, 
        "defaultValue": 1
      }, 
      "required": true, 
      "auto": false, 
      "limitType": "NUM", 
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
      "required": true, 
      "auto": false, 
      "limitType": "STRING", 
      "posORflag": null, 
      "description": "运行虚拟机", 
      "parameterType": "FLAG"
    }, 
    {
      "name": "PK", 
      "limit": {
        "defaultValue": "id"
      }, 
      "required": true, 
      "auto": false, 
      "limitType": "STRING", 
      "posORflag": null, 
      "description": "主键", 
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
    "fileID":21
  }],
  "applicationId": 0,
  "centerId": 1,
  "compileParameters": {
    "P0_Data": 10,
    "P1_Data": 100
  },
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
  "agentID2fileID": {
    "5":[1,25]
  },
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