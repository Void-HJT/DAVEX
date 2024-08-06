```json
{
  "uid": 2, 
  "centerId": 1, 
  "name": "PSI", 
  "path": "programs/PSI_BASE.mpc", 
  "compileParameters": [
    {
      "name": "P0_Data", 
      "limit": {
        "max": 999, 
        "min": 999, 
        "defaultValue": 1
      }, 
      "required": true, 
      "auto": false, 
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