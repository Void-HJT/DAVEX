<template>

  <div class="user-table-container">
    <!-- 表头 -->
    <el-header style="height: 50px">
      <div class="table-header">
        <p class="table-title">当前认证服务用户</p>
      </div>
    </el-header>

    <!-- 动态高度表格 -->
    <el-table
        :data="keycloakUserData"
        style="width: 100%"
        stripe
        :height="tableHeight"
    >
      <!-- 客户端ID -->
      <el-table-column
          label="认证服务"
          prop="clientId"
          width="250"
          align="center"
      ></el-table-column>

      <!-- 认证服务地址 -->
      <el-table-column
          label="认证服务地址"
          prop="keycloakUrl"
          width="220"
          align="center"
      ></el-table-column>

      <!-- 用户名 -->
      <el-table-column
          label="用户名"
          prop="username"
          width="200"
          align="center"
      ></el-table-column>

      <!-- 用户权限 -->
      <el-table-column
          label="用户权限"
          width="280"
          align="center"
      >
        <template #default="scope">
          <el-tooltip class="item" effect="dark" :content="scope.row.roles.join('\n')" placement="top">
            <div class="roles-cell">
              <span v-for="(role, index) in scope.row.roles.slice(0, 3)" :key="index" class="role-item">
                {{ role }}
              </span>
              <span v-if="scope.row.roles.length > 3" class="role-ellipsis">...</span>
            </div>
          </el-tooltip>
        </template>
      </el-table-column>

      <!-- 操作列 -->
      <el-table-column
          fixed="right"
          label="操作"
          width="120"
          header-align="center"
          align="center"
      >
        <template v-slot="scope">
          <el-button
              link
              type="danger"
              @click="handleDeleteUser(scope.row)"
              size="small"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 底部按钮 -->
    <div class="table-actions">
      <el-button type="primary" @click="handleViewAddUser">添加用户</el-button>
    </div>
  </div>


  <el-dialog v-model="addUserVisible" title="获取目标所在认证服务Token" width="30%">
    <el-form :model="addUserData" label-width="100px">
      <el-form-item label="用户名">
        <el-input v-model="addUserData.username" placeholder="请输入"></el-input>
      </el-form-item>
      <el-form-item label="密码">
        <el-input v-model="addUserData.password" placeholder="请输入"></el-input>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button @click="addUserVisible = false">取消</el-button>
      <el-button type="primary" @click="handleAddUser(addUserData)">确定</el-button>
    </div>

  </el-dialog>

</template>


<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus';


import {
  getUser, deleteUser, addUser
} from '../../api/auth.js'

//初始化
onMounted(() => {
  getKeycloakUserList();
})

// 控制对话框的变量
const addUserVisible = ref(false)

// 表格数据
const keycloakUserData = ref([])
const tableHeight = ref()
const addUserData = ref(
    {
      username:'',
      password:'',
    }
)

// 点击按钮处理函数

const handleDeleteUser = async (row) => {
  try {

    const res = await deleteUser(row.username);
    if (res.data == true) {
      // 更新成功的逻辑
      ElMessage.success('删除成功！'); // 弹出成功提示
      await getKeycloakUserList()
    } else {
      // 更新失败的逻辑
      ElMessage.error('删除失败！'); // 弹出失败提示
    }

  } catch (error) {
    console.error('错误信息:', error);
    ElMessage.error('发生错误！'+error); // 捕获错误提示
  }
};


const handleViewAddUser =  async () => {
  try {
    addUserVisible.value = true
  } catch (error) {
    ElMessage.error(error); // 捕获错误提示
  }
};


const handleAddUser =  async (addUserData) => {
  try {

    const res = await addUser(addUserData.username,addUserData.password);
    if (res.data == true) {
      // 更新成功的逻辑
      ElMessage.success('添加成功！'); // 弹出成功提示
      addUserVisible.value = false;
      await getKeycloakUserList()
    } else {
      // 更新失败的逻辑
      ElMessage.error('添加失败！'); // 弹出失败提示
    }

  } catch (error) {
    console.error('错误信息:', error);
    ElMessage.error('发生错误！'+error); // 捕获错误提示
  }
};

//函数
const getKeycloakUserList = async () => {
  try {
    const res = await getUser(); // 调用接口获取数据

    if (Array.isArray(res.data)) {
      // 解析返回数据
      keycloakUserData.value = res.data.map(item => ({
        username: item.username,
        roles: item.roles,
        clientId: '假设客户端ID',
        keycloakUrl: '假设认证服务地址'
      }));
      tableHeight.value = `${Math.min(this.keycloakUserData.length * 50 + 60, 600)}px`; // 最大600px
    } else {
      console.error('Expected an array, but got', res.data);
    }
  } catch (error) {
    console.error('Failed to get cache list:', error);
  }
};

</script>
<style scoped>
.roles-cell {
  display: flex;
  flex-direction: column;
  max-height: 80px; /* 限制高度，控制显示行数 */
  overflow: hidden;
}

.role-item {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.role-ellipsis {
  color: gray;
}
</style>