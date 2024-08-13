<template>
    <el-button @click="getApplicationList">获取用户表</el-button>
    <el-table 
    :data="tableData" 
    style="width: 100%" 
    @row-click="handleRowClick"
    v-loading="taskTableLoading"
    >
      <el-table-column label="用户Id" prop="uid" width="180"> </el-table-column>
      <el-table-column label="所属中心" prop="centerId" width="180">
      </el-table-column
      ><el-table-column label="名称" prop="name" width="180">
      </el-table-column>
      <el-table-column label="详细信息" prop="description" width="250">
      </el-table-column>
      <el-table-column label="所属组" prop="groups" width="250">
      </el-table-column>
      <!-- <el-table-column fixed="right" label="操作" width="240" header-align="center">
        <template v-slot="scope">
          <el-button
            link
            type="primary"
            @click="handleUploadFile(scope.row.id,scope.row.pN,scope.row.status)"
            size="small"
            >指定数据</el-button
          >
          <el-button
            link
            type="primary"
            @click="handleRun(scope.row.id,scope.row.status)"
            size="small"
            >运行</el-button
          >
          <el-button
            link
            type="primary"
            @click="handleResult(scope.row.id, scope.row.status)"
            size="small"
            >获取结果</el-button
          >
          <el-button link type="primary" size="small" @click = "deleteTaskByID(scope.row.id)">删除</el-button>
        </template>
      </el-table-column> -->
    </el-table>
    <el-button @click="getGroups">获取组别</el-button>
    <el-table 
    :data="userGroupData" 
    style="width: 100%" 
    @row-click="handleRowClick"
    v-loading="taskTableLoading"
    >
      <el-table-column label="组Id" prop="uid" width="180"> </el-table-column>
      <el-table-column label="所属中心" prop="centerId" width="180">
      </el-table-column
      ><el-table-column label="名称" prop="name" width="180">
      </el-table-column>
      <el-table-column label="权限" prop="rule" width="180">
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="60" header-align="center">
        <template v-slot="scope">
          <el-button
            link
            type="primary"
            @click="deleteGroupMethod(scope.row.uid,scope.row.centerId)"
            size="small"
            >删除</el-button
          >
          <!-- <el-button
            link
            type="primary"
            @click="handleRun(scope.row.id,scope.row.status)"
            size="small"
            >运行</el-button
          >
          <el-button
            link
            type="primary"
            @click="handleResult(scope.row.id, scope.row.status)"
            size="small"
            >获取结果</el-button
          >
          <el-button link type="primary" size="small" @click = "deleteTaskByID(scope.row.id)">删除</el-button> -->
        </template>
      </el-table-column>
    </el-table>
    <el-form :model="addApplicationGroupBody" label-width="80px">
      <!-- <el-form-item label="代理Id">
        <el-input v-model="addApplicationGroupBody.agentId"></el-input>
      </el-form-item>
      <el-form-item label="中心Id">
        <el-input v-model="addApplicationGroupBody.centerId"></el-input>
      </el-form-item> -->
      <el-form-item label="用户Id">
        <el-input v-model="addApplicationGroupBody.applicationId"></el-input>
      </el-form-item>
      <el-form-item label="组Id">
        <el-input v-model="addApplicationGroupBody.groupId"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="addApplicationGroups">为用户分配组别</el-button>
        <el-button @click="deleteApplicationGroupMethod">将用户从某个组别删除</el-button>
      </el-form-item>
    </el-form>
    
    <!-- 创建一个表单 里面有一个元素，用来填写组名 一个按钮，点击后执行addGroup函数 -->
    <el-form :model="addGroupBody" label-width="80px">
      <!-- <el-form-item label="中心Id">
        <el-input v-model="addGroupBody.centerId"></el-input>
      </el-form-item>
      <el-form-item label="代理Id">
        <el-input v-model="addGroupBody.agentId"></el-input> -->
      <!-- </el-form-item> -->
      <el-form-item label="组名">
        <el-input v-model="addGroupBody.name"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="addGroups">增加组别</el-button>
      </el-form-item>
    </el-form>
    <el-form :model="setGroupRuleBody" label-width="80px">
      <!-- <el-form-item label="代理Id">
        <el-input v-model="setGroupRuleBody.agentId"></el-input>
      </el-form-item> -->
      <el-form-item label="组Id">
        <el-input v-model="setGroupRuleBody.groupId"></el-input>
      </el-form-item>
      <el-form-item label="权限">
        <el-input v-model="setGroupRuleBody.allowMethod"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="addGroupRuleMethod">增添组别权限</el-button>
        <el-button @click="deleteRuleMethod">移除组别权限</el-button>
      </el-form-item>
    </el-form>
    

    
</template>

<script lang="ts" setup>
import { ref, } from 'vue';
import {getApplication,getGroupByApplicationId,getGroup,addGroup,setGroupRule,
  getRuleByGroup,addApplicationGroup,deleteGroup,deleteApplicationGroup,deleteRule} from '../../api/testDve.js';

const tableData = ref([]);
const userGroupData = ref([]);

const getGroup1 = ref({
  agentId:5,
  centerId:1
})
const getGroupBody = ref({
  agentId:'',
  centerId:'',
  applicationId: '',
})
const addGroupBody = ref({
  agentId:'5',
  centerId:'1',
  name: '',
})

const setGroupRuleBody = ref({
  agentId:'5',
  groupId:'',
  allowMethod: '',
})

const getRuleByGroupBody = ref({
  agentId:'5',
  groupId:'1'
})


const addApplicationGroupBody = ref({
  agentId:'5',
  centerId:'1',
  applicationId:'',
  groupId:''
})

const deleteGroupBody = ref({
  agentId:'5',
  centerId:'1',
  groupId:''
})




const deleteGroupMethod = async(uid,centerId) => {
  try {
    deleteGroupBody.value.groupId = uid;
    deleteGroupBody.value.centerId = centerId;
    await deleteGroup(deleteGroupBody.value);
    getGroups();
  } catch (error) {
    console.error('Failed to get group list:', error);
  }
}


const deleteApplicationGroupMethod = async() => {
  try {
    await deleteApplicationGroup(addApplicationGroupBody.value);
    getApplicationList();
  } catch (error) {
    console.error('Failed to get group list:', error);
  }
}
const addApplicationGroups = async() => {
  try {
    await addApplicationGroup(addApplicationGroupBody.value);
    getApplicationList();
  } catch (error) {
    console.error('Failed to get group list:', error);
  }
}



const addGroupRuleMethod = async() => {
  try {
    await setGroupRule(setGroupRuleBody.value);
    getGroups();
  } catch (error) {
    console.error('Failed to get group list:', error);
  }
}

const deleteRuleMethod = async() => {
  try {
    await deleteRule(setGroupRuleBody.value);
    getGroups();
  } catch (error) {
    console.error('Failed to get group list:', error);
  }
}

const addGroups = async() => {
  try {
    await addGroup(addGroupBody.value);
    getGroups();
  } catch (error) {
    console.error('Failed to get group list:', error);
  }
}


const getGroups = async() => {
  try {
    const res = await getGroup(getGroup1.value);
    
    //遍历res.data.data，对于每一个group，使用getRuleByGroup求得rule的值
    for (let i = 0; i < res.data.data.length; i++) {
      getRuleByGroupBody.value.agentId = 5;
      getRuleByGroupBody.value.groupId = res.data.data[i].uid;
      const rule = await getRuleByGroup(getRuleByGroupBody.value);
      //为res新增一个维度rule，其值为loading 的string
      res.data.data[i].rule = '';
      //若rule不为空，则遍历rule，将每个rule中的uid添加到res[i].rule中
      if (rule.data.data!=null) {
        for (let j = 0; j < rule.data.data.length; j++) {
          console.log(rule.data.data[j]);
          res.data.data[i].rule += rule.data.data[j].allowedMethod+' ';
        }
      }
    }

    userGroupData.value = res.data.data;
  } catch (error) {
    console.error('Failed to get group list:', error);
  }
}
const getApplicationList = async () => {
  try {
        const res = await getApplication();
        // 确保 res.data 是一个数组
        if (Array.isArray(res.data.data)) {
            // 对于res.data.data中的每一个applicantion,都使用getGroupByApplicationId求得groups的值
            for (let i = 0; i < res.data.data.length; i++) {
                getGroupBody.value.agentId = 5;
                getGroupBody.value.centerId = res.data.data[i].centerId;
                getGroupBody.value.applicationId = res.data.data[i].uid;
                const groups = await getGroupByApplicationId(getGroupBody.value);
                //为res新增一个维度groups，其值为loading 的string
                res.data.data[i].groups = '';
                //若groups不为空，则遍历groups，将每个group中的uid添加到res[i].groups中
                if (groups.data.data!=null) {
                  console.log(groups.data.data);
                    for (let j = 0; j < groups.data.data.length; j++) {
                        res.data.data[i].groups += groups.data.data[j].name + ' ';
                    }
                }
            }
            console.log(res.data.data);
            tableData.value = res.data.data;
            // 现在可以安全地调用 includes
        } else {
            console.error('Expected an array, but got', res.data);
        }
    } catch (error) {
        console.error('Failed to get application list:', error);
    }
};


</script>

<style></style>