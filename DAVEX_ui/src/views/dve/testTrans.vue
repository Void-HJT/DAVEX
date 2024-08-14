<template>
  <el-container>
    <el-header style="height: 50px">
      <div
        style="
          background-color: antiquewhite;
          height: 40px;
          display: flex;
          justify-content: center;
          align-items: center;
        "
      >
        <p
          style="
            font-size: 20px;
            color: black;
            opacity: 100%;
            text-align: center;
          "
        >
          用户表
        </p>
      </div>
    </el-header>
    <el-main>
      <el-table
        :data="tableData"
        style="width: 100%"
        @row-click="handleRowClick"
        v-loading="taskTableLoading"
        stripe
        height="200"
        max-height="200"
      >
        <el-table-column
          fixed
          label="用户Id"
          prop="uid"
          width="180"
          align="center"
        ></el-table-column>
        <el-table-column
          label="所属中心"
          prop="centerId"
          width="180"
          align="center"
        ></el-table-column>
        <el-table-column
          label="名称"
          prop="name"
          width="180"
          align="center"
        ></el-table-column>
        <el-table-column
          label="详细信息"
          prop="description"
          width="250"
          align="center"
        ></el-table-column>
        <el-table-column
          label="所属组"
          prop="groups"
          width="250"
          align="center"
        ></el-table-column>
        <el-table-column
          fixed="right"
          label="操作"
          mid-width="60"
          header-align="center"
          align="center"
        >
          <template v-slot="scope">
            <div
              style="
                display: flex;
                justify-content: center;
                align-items: center;
              "
            >
              <el-popover
                placement="top-start"
                title="分组管理"
                :width="400"
                trigger="click"
              >
                <template #reference>
                  <div>
                    <el-button
                      link
                      type="primary"
                      size="small"
                      @click="
                        (addApplicationGroupBody.applicationId = scope.row.uid),
                          (addApplicationGroupBody.centerId =
                            scope.row.centerId)
                      "
                    >
                      管理分组
                    </el-button>
                  </div>
                </template>
                <!-- <div style="height: 20px;"><el-icon style="float:right;"> <CloseBold /></el-icon></div> -->
                <el-form
                  :model="addApplicationGroupBody"
                  label-width="60px"
                  label-position="left"
                  style="
                    display: flex;
                    justify-content: center;
                    align-items: center;
                  "
                >
                  <el-form-item label="用户组">
                    <el-input
                      v-model="addApplicationGroupBody.groupId"
                    ></el-input>
                  </el-form-item>
                </el-form>
                <div
                  style="
                    display: flex;
                    justify-content: center;
                    align-items: center;
                  "
                >
                  <el-button @click="addApplicationGroups">
                    加入该用户组
                  </el-button>
                  <el-button @click="deleteApplicationGroupMethod">
                    移出该用户组
                  </el-button>
                </div>
              </el-popover>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-main>
  </el-container>
  <el-header style="height: 50px">
    <div
      style="
        background-color: antiquewhite;
        height: 40px;
        display: flex;
        justify-content: center;
        align-items: center;
      "
    >
      <p
        style="font-size: 20px; color: black; opacity: 100%; text-align: center"
      >
        分组表
      </p>
    </div>
  </el-header>
  <el-main>
    <el-table
      stripe
      :data="userGroupData"
      style="width: 100%"
      @row-click="handleRowClick"
      v-loading="taskTableLoading"
      max-height="300"
    >
      <el-table-column
        fixed
        label="组ID"
        prop="uid"
        width="180"
        align="center"
      ></el-table-column>
      <el-table-column
        label="名称"
        prop="name"
        width="180"
        align="center"
      ></el-table-column>
      <el-table-column
        label="所属中心"
        prop="centerId"
        width="180"
        align="center"
      ></el-table-column>
      <el-table-column
        label="权限"
        prop="rule"
        width="300"
        align="center"
      ></el-table-column>
      <el-table-column
        fixed="right"
        label="操作"
        mid-width="60"
        header-align="center"
        align="center"
      >
        <template v-slot="scope">
          <div
            style="display: flex; justify-content: center; align-items: center"
          >
            <el-button
              link
              type="primary"
              @click="deleteGroupMethod(scope.row.uid, scope.row.centerId)"
              size="small"
            >
              删除分组
            </el-button>
            <el-popover
              placement="top-start"
              title="权限管理"
              :width="400"
              trigger="click"
            >
              <template #reference>
                <div>
                  <el-button
                    link
                    type="primary"
                    size="small"
                    @click="
                      (setGroupRuleBody.groupId = scope.row.uid),
                        (setGroupRulePopoverVisible = true)
                    "
                  >
                    管理权限
                  </el-button>
                </div>
              </template>
              <!-- <div style="height: 20px;"><el-icon style="float:right;"> <CloseBold /></el-icon></div> -->
              <el-form
                :model="setGroupRuleBody"
                label-width="60px"
                label-position="left"
                style="
                  display: flex;
                  justify-content: center;
                  align-items: center;
                "
              >
                <el-form-item label="权限名">
                  <el-input v-model="setGroupRuleBody.allowMethod"></el-input>
                </el-form-item>
              </el-form>
              <div
                style="
                  display: flex;
                  justify-content: center;
                  align-items: center;
                "
              >
                <el-button @click="addGroupRuleMethod">增添该权限</el-button>
                <el-button @click="deleteRuleMethod">移除该权限</el-button>
              </div>
            </el-popover>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-popover
      placement="top-start"
      title="此处输入新增组名"
      :width="400"
      trigger="click"
    >
      <template #reference>
        <div>
          <div style="height: 20px"></div>
          <el-button class="el-button mt-4" style="width: 100%">
            增加组别
          </el-button>
        </div>
      </template>
      <el-form :model="addGroupBody" label-width="80px" label-position="left">
        <el-form-item label="组名">
          <el-input v-model="addGroupBody.name"></el-input>
        </el-form-item>
        <el-button
          @click="addGroups"
          calss="el-button mt-4"
          style="width: 100%"
        >
          增加组别
        </el-button>
      </el-form>
    </el-popover>
  </el-main>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import {
  getApplication,
  getGroupByApplicationId,
  getGroup,
  addGroup,
  setGroupRule,
  getRuleByGroup,
  addApplicationGroup,
  deleteGroup,
  deleteApplicationGroup,
  deleteRule,
} from '../../api/testDve.js'

onMounted(() => {
  getApplicationList()
  getGroups()
})

const tableData = ref([])
const userGroupData = ref([])
const setGroupRulePopoverVisible = ref(false)

const getGroup1 = ref({
  agentId: 5,
  centerId: 1,
})
const getGroupBody = ref({
  agentId: '',
  centerId: '',
  applicationId: '',
})
const addGroupBody = ref({
  agentId: '5',
  centerId: '1',
  name: '',
})

const setGroupRuleBody = ref({
  agentId: '5',
  groupId: '',
  allowMethod: '',
})

const getRuleByGroupBody = ref({
  agentId: '5',
  groupId: '1',
})

const addApplicationGroupBody = ref({
  agentId: '5',
  centerId: '1',
  applicationId: '',
  groupId: '',
})

const deleteGroupBody = ref({
  agentId: '5',
  centerId: '1',
  groupId: '',
})

const deleteGroupMethod = async (uid, centerId) => {
  try {
    deleteGroupBody.value.groupId = uid
    deleteGroupBody.value.centerId = centerId
    await deleteGroup(deleteGroupBody.value)
    getGroups()
  } catch (error) {
    console.error('Failed to get group list:', error)
  }
}

const deleteApplicationGroupMethod = async () => {
  try {
    await deleteApplicationGroup(addApplicationGroupBody.value)
    getApplicationList()
  } catch (error) {
    console.error('Failed to get group list:', error)
  }
}
const addApplicationGroups = async () => {
  try {
    await addApplicationGroup(addApplicationGroupBody.value)
    getApplicationList()
  } catch (error) {
    console.error('Failed to get group list:', error)
  }
}

const addGroupRuleMethod = async () => {
  try {
    await setGroupRule(setGroupRuleBody.value)
    getGroups()
  } catch (error) {
    console.error('Failed to get group list:', error)
  }
}

const deleteRuleMethod = async () => {
  try {
    await deleteRule(setGroupRuleBody.value)
    getGroups()
  } catch (error) {
    console.error('Failed to get group list:', error)
  }
}

const addGroups = async () => {
  try {
    await addGroup(addGroupBody.value)
    getGroups()
  } catch (error) {
    console.error('Failed to get group list:', error)
  }
}

const getGroups = async () => {
  try {
    const res = await getGroup(getGroup1.value)

    //遍历res.data.data，对于每一个group，使用getRuleByGroup求得rule的值
    for (let i = 0; i < res.data.data.length; i++) {
      getRuleByGroupBody.value.agentId = 5
      getRuleByGroupBody.value.groupId = res.data.data[i].uid
      const rule = await getRuleByGroup(getRuleByGroupBody.value)
      //为res新增一个维度rule，其值为loading 的string
      res.data.data[i].rule = ''
      //若rule不为空，则遍历rule，将每个rule中的uid添加到res[i].rule中
      if (rule.data.data != null) {
        for (let j = 0; j < rule.data.data.length; j++) {
          console.log(rule.data.data[j])
          res.data.data[i].rule += rule.data.data[j].allowedMethod + ' '
        }
      }
    }

    userGroupData.value = res.data.data
  } catch (error) {
    console.error('Failed to get group list:', error)
  }
}
const getApplicationList = async () => {
  try {
    const res = await getApplication()
    // 确保 res.data 是一个数组
    if (Array.isArray(res.data.data)) {
      // 对于res.data.data中的每一个applicantion,都使用getGroupByApplicationId求得groups的值
      for (let i = 0; i < res.data.data.length; i++) {
        getGroupBody.value.agentId = 5
        getGroupBody.value.centerId = res.data.data[i].centerId
        getGroupBody.value.applicationId = res.data.data[i].uid
        const groups = await getGroupByApplicationId(getGroupBody.value)
        //为res新增一个维度groups，其值为loading 的string
        res.data.data[i].groups = ''
        //若groups不为空，则遍历groups，将每个group中的uid添加到res[i].groups中
        if (groups.data.data != null) {
          console.log(groups.data.data)
          for (let j = 0; j < groups.data.data.length; j++) {
            res.data.data[i].groups += groups.data.data[j].name + ' '
          }
        }
      }
      console.log(res.data.data)
      tableData.value = res.data.data
      // 现在可以安全地调用 includes
    } else {
      console.error('Expected an array, but got', res.data)
    }
  } catch (error) {
    console.error('Failed to get application list:', error)
  }
}
</script>

<style></style>
