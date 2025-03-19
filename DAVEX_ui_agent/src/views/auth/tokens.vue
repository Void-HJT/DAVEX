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
        label="中心ID"
        prop="centerId"
        width="200"
        align="center"
    ></el-table-column>
    <el-table-column
        label="Token标识"
        prop="jti"
        width="200"
        align="center"
    ></el-table-column>
    <el-table-column
        label="更新时间"
        prop="issuedTime"
        width="200"
        align="center"
    ></el-table-column>
      <el-table-column
          label="过期时间"
          prop="expiresTime"
          width="200"
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

  <el-dialog v-model="addTokenVisible" title="获取数据中心令牌" width="30%">
    <el-form :model="addTokenData" label-width="100px">
      <el-form-item label="数据中心ID">
        <el-input v-model="addTokenData.centerId" placeholder="请输入"></el-input>
      </el-form-item>
      <el-form-item label="代理ID">
        <el-input v-model="addTokenData.agentId" placeholder="请输入"></el-input>
      </el-form-item>
      <el-form-item label="密码">
        <el-input v-model="addTokenData.password" placeholder="请输入"></el-input>
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
      centerId:'',
      agentId:'',
      password:'',
    }
)

// 点击按钮处理函数
const handleViewTokenIsValid = async (row) => {

  const res = await isTokenExpired(row.centerId)
  console.log(row.centerId)
  isTokenExpiredMessage.value = res.data
  isTokenExpiredInfo.value = row.centerId
  isTokenExpiredVisible.value = true
}

const handleUpdateToken = async (centerId) => {
  try {
    const res = await updateToken(centerId); // 调用更新接口

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
    await addToken(addTokenData.centerId,addTokenData.agentId,addTokenData.password);
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

    const res = await deleteToken(row.centerId);
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
        const centerId = Object.keys(entry)[0];
        const jwtMeta = entry[centerId];

        return {
          centerId: centerId,
          jti: jwtMeta.uid,
          agentId: jwtMeta.agentUid,
          issuedTime: formatDate(parseDate(jwtMeta.issuedTime)), // 格式化时间戳为可读格式
          expiresTime: formatDate(parseDate(jwtMeta.expiresTime)), // 格式化时间戳为可读格式
          revoked: jwtMeta.revoked
        };
      });
    } else {
      console.error('Expected an array, but got', res.data);
    }
  } catch (error) {
    console.error('Failed to get cache list:', error);
  }
};

function parseDate(arr) {
  // 构造一个 JavaScript Date 对象，数组的结构是 [year, month, day, hour, minute, second, millisecond]
  return new Date(arr[0], arr[1] - 1, arr[2], arr[3], arr[4], arr[5], arr[6]);
}

function formatDate(date) {
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  });
}

</script>
<style scoped></style>