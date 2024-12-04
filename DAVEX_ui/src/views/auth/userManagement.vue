<template>

<el-container>
 <!-- 表头 -->
  <el-header class="custom-header">
      <div class="icon-text">
        <el-icon><DataBoard /></el-icon>
        <span>当前服务用户</span>
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
            <div class="roles-cell">
              <span v-for="(role, index) in scope.row.roles" :key="index" class="role-item">
                {{ role }}
              </span>
            </div>
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
    <div>
    <el-button
      class="default-button"
      style="margin-top: 10px;"
      @click="handleViewAddUser"
  >
    添加认证用户
    </el-button> 
    </div>
  <el-dialog v-model="addUserVisible" title="在认证服务上添加新用户" width="30%">
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
</el-container>

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
const keycloak = "DAVEX-CXX50-KEYCLOAK"
const url = "http://10.176.37.50:10001"

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
        clientId: keycloak,
        keycloakUrl: url
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
  flex-direction: column; /* 让子元素纵向排列 */
  align-items: center; /* 左对齐 */
}

.role-item {
  margin-bottom: 4px; /* 每行之间的间距 */
  word-break: break-all; /* 自动换行，防止超出列宽 */
}
</style>