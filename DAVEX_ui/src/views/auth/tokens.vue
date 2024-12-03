<template>
  <el-container>
    <el-header class="custom-header">
      <div class="icon-text">
        <el-icon><DataBoard /></el-icon>
        <span>当前服务已保存的Token</span>
      </div>
    </el-header>
    <el-table
      :data="tokensData"
      style="width: 100%"
      stripe
      height="200"
      max-height="200"
    >
    <el-table-column
        label="认证服务"
        prop="clientId"
        width="250"
        align="center"
    ></el-table-column>
    <el-table-column
        label="认证服务地址"
        prop="keycloakUrl"
        width="220"
        align="center"
    ></el-table-column>
    <el-table-column
        label="认证目标"
        prop="targetId"
        width="200"
        align="center"
    ></el-table-column>
    <el-table-column
        label="当前Token"
        width="180"
        align="center"
    >
      <template #default="scope">
        <el-tooltip class="item" effect="dark" :content="scope.row.accessToken" placement="top">
          <span>{{ scope.row.accessToken.length > 20
              ? scope.row.accessToken.slice(0, 20) + '...'
              : scope.row.accessToken }}</span>
        </el-tooltip>
      </template>
    </el-table-column>
    <el-table-column
        label="更新时间"
        prop="updateTime"
        width="180"
        align="center"
    ></el-table-column>
    <el-table-column
        fixed="right"
        label="操作"
        width="120"
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
          <el-button
              link
              type="primary"
              @click="handleViewTokenIsValid(scope.row)"
              size="small"
          >
            检查有效性
          </el-button>
          <el-button
              link
              type="danger"
              @click="handleDeleteToken(scope.row)"
              size="small"
          >
            删除
          </el-button>
        </div>
      </template>
    </el-table-column>
    </el-table>
  <div>
    <el-button
      class="default-button"
      style="margin-top: 10px;"
      @click="handleViewAddToken"
  >
    添加Token
  </el-button> 
  </div>
  <el-dialog v-model="isTokenExpiredVisible" title="有效性检查" width="30%">
    <!-- 显示消息 -->
    <span>{{ isTokenExpiredMessage }}</span>

    <!-- 底部操作按钮 -->
    <template #footer>
      <div class="dialog-footer">
        <!-- 返回按钮 -->
        <el-button @click="isTokenExpiredVisible = false">返回</el-button>
        <!-- 更新按钮，只有特定条件下显示 -->
        <el-button
            v-if="isTokenExpiredMessage === 'Token expired but Refresh Token is valid'"
            type="primary"
            @click="handleUpdateToken(isTokenExpiredInfo)">
          更新
        </el-button>
      </div>
    </template>
  </el-dialog>

  <el-dialog v-model="addTokenVisible" title="获取目标所在认证服务Token" width="30%">
    <el-form :model="addTokenData" label-width="100px">
      <el-form-item label="用户名">
        <el-input v-model="addTokenData.username" placeholder="请输入"></el-input>
      </el-form-item>
      <el-form-item label="密码">
        <el-input v-model="addTokenData.password" placeholder="请输入"></el-input>
      </el-form-item>
      <el-form-item label="目标">
        <el-input v-model="addTokenData.targetId" placeholder="请输入"></el-input>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button @click="addTokenVisible = false">取消</el-button>
      <el-button type="primary" @click="handleAddToken(addTokenData)">确定</el-button>
    </div>

  </el-dialog>

  </el-container>
</template>


<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus';


import {
  getTokenCache, isTokenExpired, updateToken,addToken,deleteToken
} from '../../api/auth.js'

//初始化
onMounted(() => {
  getTokenCacheList();
})

// 控制对话框的变量
const isTokenExpiredVisible = ref(false)
const addTokenVisible = ref(false)
const isTokenExpiredMessage = ref('');
const isTokenExpiredInfo = ref('');

// 表格数据
const tokensData = ref([])
const addTokenData = ref(
    {
      username:'',
      password:'',
      targetId:'',
    }
)

// 点击按钮处理函数
const handleViewTokenIsValid = async (row) => {

  const res = await isTokenExpired(row.targetId)
  console.log(row.targetId)
  isTokenExpiredMessage.value = res.data
  isTokenExpiredInfo.value = row.targetId
  isTokenExpiredVisible.value = true
}

const handleUpdateToken = async (targetId) => {
  try {
    const res = await updateToken(targetId); // 调用更新接口

    if (res.data == true) {
      // 更新成功的逻辑
      ElMessage.success('更新成功！'); // 弹出成功提示
      await getTokenCacheList(); // 刷新表格数据
    } else {
      // 更新失败的逻辑
      ElMessage.error('更新失败！'); // 弹出失败提示
    }
  } catch (error) {
    console.error('更新失败，错误信息:', error);
    ElMessage.error('更新失败，发生错误！'); // 捕获错误提示
  }
};

const handleViewAddToken =  async () => {
  try {
    addTokenVisible.value = true
  } catch (error) {
    ElMessage.error(error); // 捕获错误提示
  }
};

const handleAddToken =  async (addTokenData) => {
  try {
    await addToken(addTokenData.username,addTokenData.password,addTokenData.targetId);
    ElMessage.success("获取成功")
    addTokenVisible.value = false
    await getTokenCacheList()
  } catch (error) {
    console.error('错误信息:', error);
    ElMessage.error('发生错误！'+error); // 捕获错误提示
  }
};

const handleDeleteToken = async (row) => {
  try {

    const res = await deleteToken(row.targetId);
    if (res.data == true) {
      // 更新成功的逻辑
      ElMessage.success('删除成功！'); // 弹出成功提示
      await getTokenCacheList()
    } else {
      // 更新失败的逻辑
      ElMessage.error('删除失败！'); // 弹出失败提示
    }

  } catch (error) {
    console.error('错误信息:', error);
    ElMessage.error('发生错误！'+error); // 捕获错误提示
  }
};

//函数
const getTokenCacheList = async () => {
  try {
    const res = await getTokenCache(); // 调用接口获取数据

    if (Array.isArray(res.data)) {
      // 解析返回数据
      tokensData.value = res.data.map(entry => {
        const keycloakUrl = Object.keys(entry)[0]; // 获取 keycloakUrl
        const tokenResult = entry[keycloakUrl]; // 获取 TokenResult 对象

        return {
          keycloakUrl, // 对应表格的 keycloakUrl 列
          targetId: tokenResult.targetId,
          clientId: tokenResult.clientId, // 从 TokenResult 中提取 clientId
          accessToken: tokenResult.accessToken, // 从 TokenResult 中提取 accessToken
          updateTime: new Date(tokenResult.updateTime).toLocaleString() // 格式化时间戳为可读格式
        };
      });
    } else {
      console.error('Expected an array, but got', res.data);
    }
  } catch (error) {
    console.error('Failed to get cache list:', error);
  }
};


</script>
<style scoped></style>