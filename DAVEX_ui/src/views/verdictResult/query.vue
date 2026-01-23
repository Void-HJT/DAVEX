<template>
  <el-container>
    <el-header class="custom-header">
      <div class="icon-text">
        <el-icon><Document /></el-icon>
        <span>类案检索任务结果</span>
      </div>
      <el-button @click="refreshTasks">刷新</el-button>
    </el-header>
    <el-main>
      <el-table 
        :data="filteredTasks" 
        style="width: 100%"
        v-loading="loading"
        max-height="600"
      >
        <el-table-column label="任务ID" prop="uid" min-width="180"></el-table-column>
        <el-table-column label="目标代理" prop="agentId" min-width="200"></el-table-column>
        <el-table-column label="任务发起时间" min-width="300">
          <template #default="scope">
            <div style="display: flex; align-items: center">
              <el-icon><Timer /></el-icon>
              <span style="margin-left: 10px">{{ formatDate(scope.row.startTime) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          fixed="right"
          label="操作"
          min-width="150"
          header-align="center"
        >
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click="viewResult(scope.row)"
              size="small"
            >
              查看结果
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-main>
  </el-container>
</template>

<script lang="ts" setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getAllTasks } from '../../api/verdict.js'
import { Document, Timer } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { addOperationHistory } from '../../api/operationHistory.js'

const router = useRouter()

// 加载状态
const loading = ref(false)

// 任务列表数据
const tasks = ref([])

// 筛选出 resultIds 不为空的任务
const filteredTasks = computed(() => {
  return tasks.value.filter(task => {
    return task.resultIds && task.resultIds.trim() !== ''
  })
})

// 获取所有任务
const getTasks = async () => {
  loading.value = true
  try {
    const res = await getAllTasks()
    if (res.data && res.data.code === 1) {
      tasks.value = res.data.data || []
    } else {
      ElMessage.error(res.data?.message || '获取任务列表失败')
      tasks.value = []
    }
  } catch (error) {
    console.error('获取任务列表失败:', error)
    ElMessage.error('获取任务列表失败，请稍后重试')
    tasks.value = []
  } finally {
    loading.value = false
  }
}

// 刷新任务列表
const refreshTasks = () => {
  getTasks()
}

// 查看结果
const viewResult = async (row) => {
  // 记录操作历史
  await addOperationHistory({
    agentId: row.agentId,
    operationType: '查看结果',
    operationObject: `任务 ${row.uid}`,
    result: '成功',
    remark: `查看类案检索任务 ${row.uid} 的结果`
  })
  // 跳转到结果详情页，传递任务信息
  router.push({
    name: 'verdictResultDetail',
    params: {
      taskId: row.uid
    },
    query: {
      agentId: row.agentId,
      resultIds: row.resultIds
    }
  })
}

// 格式化日期时间
const formatDate = (cellValue) => {
  if (!cellValue) return ''
  const date = new Date(cellValue)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 组件挂载时获取任务列表
onMounted(() => {
  getTasks()
})
</script>

<style scoped>
.custom-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 20px;
}

.icon-text {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 500;
}
</style>
