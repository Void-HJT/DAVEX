<template>
  <el-container>
    <el-header class="custom-header">
      <div class="icon-text">
        <el-icon><Document /></el-icon>
        <span>操作历史记录</span>
      </div>
      <div class="header-actions">
        <el-button class="default-button" @click="refreshHistory">
          <el-icon><Refresh /></el-icon> 刷新
        </el-button>
        <el-popconfirm
          title="确定要清空所有操作历史记录吗？"
          confirm-button-text="确定"
          cancel-button-text="取消"
          @confirm="clearHistory"
        >
          <template #reference>
            <el-button class="small-delete-button">
              <el-icon><Delete /></el-icon> 清空历史
            </el-button>
          </template>
        </el-popconfirm>
      </div>
    </el-header>
    <el-main>
      <!-- 筛选区域 -->
      <div class="filter-section">
        <el-form :inline="true" class="filter-form">
          <el-form-item label="操作类型">
            <el-select
              v-model="filterType"
              placeholder="全部类型"
              clearable
              style="width: 200px"
              @change="applyFilter"
            >
              <el-option
                v-for="type in operationTypes"
                :key="type"
                :label="type"
                :value="type"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="代理">
            <el-select
              v-model="filterAgent"
              placeholder="全部代理"
              clearable
              style="width: 200px"
              @change="applyFilter"
            >
              <el-option
                v-for="agent in agentList"
                :key="agent"
                :label="agent"
                :value="agent"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="操作结果">
            <el-select
              v-model="filterResult"
              placeholder="全部结果"
              clearable
              style="width: 150px"
              @change="applyFilter"
            >
              <el-option label="成功" value="成功" />
              <el-option label="失败" value="失败" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>

      <!-- 操作历史表格 -->
      <el-table
        :data="filteredHistory"
        style="width: 100%"
        v-loading="loading"
        max-height="600"
        stripe
        border
      >
        <el-table-column
          type="index"
          label="序号"
          width="70"
          align="center"
        />
        <el-table-column label="操作时间" min-width="180" align="center">
          <template #default="scope">
            <div style="display: flex; align-items: center; justify-content: center">
              <el-icon><Timer /></el-icon>
              <span style="margin-left: 8px">{{ formatDate(scope.row.time) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          label="操作类型"
          prop="operationType"
          min-width="150"
          align="center"
        >
          <template #default="scope">
            <el-tag :type="getOperationTypeTag(scope.row.operationType)">
              {{ scope.row.operationType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="目标代理"
          prop="agentId"
          min-width="150"
          align="center"
        >
          <template #default="scope">
            {{ scope.row.agentId || '-' }}
          </template>
        </el-table-column>
        <el-table-column
          label="操作对象"
          prop="operationObject"
          min-width="200"
          align="center"
          show-overflow-tooltip
        >
          <template #default="scope">
            {{ scope.row.operationObject || '-' }}
          </template>
        </el-table-column>
        <el-table-column
          label="操作结果"
          prop="result"
          min-width="100"
          align="center"
        >
          <template #default="scope">
            <el-tag :type="scope.row.result === '成功' ? 'success' : 'danger'">
              {{ scope.row.result || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="备注"
          prop="remark"
          min-width="200"
          align="center"
          show-overflow-tooltip
        >
          <template #default="scope">
            {{ scope.row.remark || '-' }}
          </template>
        </el-table-column>
        <el-table-column
          fixed="right"
          label="操作"
          width="100"
          align="center"
        >
          <template #default="scope">
            <el-popconfirm
              title="确定要删除这条记录吗？"
              confirm-button-text="确定"
              cancel-button-text="取消"
              @confirm="deleteHistory(scope.row.uid)"
            >
              <template #reference>
                <el-button link type="danger" size="small">
                  删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-section">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="filteredHistory.length"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-main>
  </el-container>
</template>

<script lang="ts" setup>
import { onMounted, ref, computed } from 'vue'
import { getAllOperationHistory, deleteOperationHistory, clearAllOperationHistory } from '../../api/operationHistory.js'
import { Document, Timer, Refresh, Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// 加载状态
const loading = ref(false)

// 操作历史数据
const historyList = ref([])

// 筛选条件
const filterType = ref('')
const filterAgent = ref('')
const filterResult = ref('')

// 分页
const currentPage = ref(1)
const pageSize = ref(20)

// 获取操作类型列表（从数据中提取）
const operationTypes = computed(() => {
  const types = new Set(historyList.value.map(item => item.operationType).filter(Boolean))
  return Array.from(types)
})

// 获取代理列表（从数据中提取）
const agentList = computed(() => {
  const agents = new Set(historyList.value.map(item => item.agentId).filter(Boolean))
  return Array.from(agents)
})

// 筛选后的历史记录
const filteredHistory = computed(() => {
  let result = historyList.value

  if (filterType.value) {
    result = result.filter(item => item.operationType === filterType.value)
  }

  if (filterAgent.value) {
    result = result.filter(item => item.agentId === filterAgent.value)
  }

  if (filterResult.value) {
    result = result.filter(item => item.result === filterResult.value)
  }

  return result
})

// 获取所有操作历史
const getHistory = async () => {
  loading.value = true
  try {
    const res = await getAllOperationHistory()
    if (res.data && res.data.code === 1) {
      historyList.value = res.data.data || []
    } else {
      ElMessage.error(res.data?.message || '获取操作历史失败')
      historyList.value = []
    }
  } catch (error) {
    console.error('获取操作历史失败:', error)
    ElMessage.error('获取操作历史失败，请稍后重试')
    historyList.value = []
  } finally {
    loading.value = false
  }
}

// 刷新历史记录
const refreshHistory = () => {
  getHistory()
}

// 应用筛选
const applyFilter = () => {
  currentPage.value = 1
}

// 删除单条记录
const deleteHistory = async (uid) => {
  try {
    const res = await deleteOperationHistory(uid)
    if (res.data && res.data.code === 1) {
      ElMessage.success('删除成功')
      getHistory()
    } else {
      ElMessage.error(res.data?.message || '删除失败')
    }
  } catch (error) {
    console.error('删除操作历史失败:', error)
    ElMessage.error('删除失败，请稍后重试')
  }
}

// 清空所有记录
const clearHistory = async () => {
  try {
    const res = await clearAllOperationHistory()
    if (res.data && res.data.code === 1) {
      ElMessage.success('清空成功')
      historyList.value = []
    } else {
      ElMessage.error(res.data?.message || '清空失败')
    }
  } catch (error) {
    console.error('清空操作历史失败:', error)
    ElMessage.error('清空失败，请稍后重试')
  }
}

// 格式化日期时间
const formatDate = (cellValue) => {
  if (!cellValue) return '-'
  const date = new Date(cellValue)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 获取操作类型对应的标签样式
const getOperationTypeTag = (type) => {
  const typeMap = {
    '创建文件夹': 'success',
    '上传文件': 'primary',
    '删除文件': 'danger',
    '删除文件夹': 'danger',
    '重命名文件': 'warning',
    '重命名文件夹': 'warning',
    '上传MPC文件': 'primary',
    '获取文件': 'info',
    '类案检索': 'success',
    '查看结果': 'info',
    '预览文件': 'info',
    '证据对比': 'warning',
    '判决对比': 'warning'
  }
  return typeMap[type] || ''
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
}

const handleCurrentChange = (val) => {
  currentPage.value = val
}

// 组件挂载时获取数据
onMounted(() => {
  getHistory()
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

.header-actions {
  display: flex;
  gap: 10px;
}

.filter-section {
  margin-bottom: 16px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.pagination-section {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
