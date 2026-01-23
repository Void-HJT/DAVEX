<template>
  <el-container>
    <el-header class="custom-header">
      <div class="icon-text">
        <el-icon><Document /></el-icon>
        <span>类案检索任务结果详情</span>
      </div>
      <el-button @click="goBack">返回</el-button>
    </el-header>
    <el-main>
      <el-table 
        :data="fileList" 
        style="width: 100%"
        v-loading="loading"
        max-height="600"
      >
        <el-table-column label="文件ID" prop="uid" min-width="200"></el-table-column>
        <el-table-column label="文件名" prop="name" min-width="250"></el-table-column>
        <el-table-column label="文件类型" prop="type" min-width="120"></el-table-column>
        <el-table-column label="文件大小" min-width="120">
          <template #default="scope">
            {{ formatFileSize(scope.row.size) }}
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="200">
          <template #default="scope">
            <div style="display: flex; align-items: center">
              <el-icon><Timer /></el-icon>
              <span style="margin-left: 10px">{{ formatDate(scope.row.createDate) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          fixed="right"
          label="操作"
          min-width="300"
          header-align="center"
        >
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click="previewFile(scope.row)"
              size="small"
            >
              预览
            </el-button>
            <el-button
              link
              type="success"
              @click="compareEvidence(scope.row)"
              size="small"
            >
              证据对比
            </el-button>
            <el-button
              link
              type="warning"
              @click="compareVerdict(scope.row)"
              size="small"
            >
              判决对比
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-main>
  </el-container>

  <!-- 文件预览对话框 -->
  <el-dialog v-model="previewVisible" title="文件预览" width="70%">
    <div class="preview-content" v-html="previewContent"></div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="previewVisible = false">关闭</el-button>
      </div>
    </template>
  </el-dialog>

  <!-- 证据/判决对比对话框 -->
  <el-dialog v-model="compareVisible" :title="compareTitle" width="90%">
    <div class="compare-container">
      <div class="compare-panel">
        <div class="panel-header">
          <h3>输入文件（{{ compareType === 'evidence' ? '证据' : '判决' }}部分）</h3>
        </div>
        <div class="panel-content" v-html="inputContent"></div>
      </div>
      <div class="compare-panel">
        <div class="panel-header">
          <h3>结果文件（{{ compareType === 'evidence' ? '证据' : '判决' }}部分）</h3>
        </div>
        <div class="panel-content" v-html="resultContent"></div>
      </div>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="compareVisible = false">关闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getFileInfo, readFileContent, compareContent } from '../../api/verdict.js'
import { Document, Timer } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { addOperationHistory } from '../../api/operationHistory.js'

const route = useRoute()
const router = useRouter()

// 加载状态
const loading = ref(false)
const previewVisible = ref(false)
const previewContent = ref('')

// 对比相关状态
const compareVisible = ref(false)
const compareTitle = ref('')
const compareType = ref('') // 'evidence' 或 'verdict'
const inputContent = ref('')
const resultContent = ref('')

// 文件列表数据
const fileList = ref([])

// 获取路由参数
const taskId = route.params.taskId
const agentId = route.query.agentId
const resultIds = route.query.resultIds

// 返回上一页
const goBack = () => {
  router.back()
}

// 解析 resultIds 字符串，获取所有 fileId
const parseResultIds = (resultIdsStr) => {
  if (!resultIdsStr || resultIdsStr.trim() === '') {
    return []
  }
  return resultIdsStr.split(',').map(id => id.trim()).filter(id => id !== '')
}

// 获取所有文件信息
const getFileList = async () => {
  if (!resultIds || !agentId) {
    ElMessage.error('缺少必要参数')
    return
  }

  loading.value = true
  const fileIds = parseResultIds(resultIds)
  
  if (fileIds.length === 0) {
    ElMessage.warning('没有结果文件')
    loading.value = false
    return
  }

  try {
    const filePromises = fileIds.map(fileId => 
      getFileInfo(fileId, agentId).catch(error => {
        console.error(`获取文件 ${fileId} 信息失败:`, error)
        return null
      })
    )

    const results = await Promise.all(filePromises)
    fileList.value = results
      .filter(res => res && res.data && res.data.code === 1)
      .map(res => res.data.data)
    
    if (fileList.value.length === 0) {
      ElMessage.warning('未能获取到任何文件信息')
    }
  } catch (error) {
    console.error('获取文件列表失败:', error)
    ElMessage.error('获取文件列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 预览文件
const previewFile = async (file) => {
  try {
    previewVisible.value = true
    previewContent.value = '加载中...'
    
    const res = await readFileContent(file.uid, agentId)
    if (res.data && res.data.code === 1) {
      // 将换行符转换为 HTML 换行
      previewContent.value = res.data.data.replace(/\n/g, '<br>')
      // 记录操作历史
      await addOperationHistory({
        agentId: agentId,
        operationType: '预览文件',
        operationObject: file.name || file.uid,
        result: '成功',
        remark: `预览任务 ${taskId} 的结果文件`
      })
    } else {
      previewContent.value = res.data?.message || '读取文件失败'
      ElMessage.error(res.data?.message || '读取文件失败')
      // 记录失败操作
      await addOperationHistory({
        agentId: agentId,
        operationType: '预览文件',
        operationObject: file.name || file.uid,
        result: '失败',
        remark: res.data?.message || '读取文件失败'
      })
    }
  } catch (error) {
    console.error('预览文件失败:', error)
    previewContent.value = '预览文件失败，请稍后重试'
    ElMessage.error('预览文件失败，请稍后重试')
    // 记录失败操作
    await addOperationHistory({
      agentId: agentId,
      operationType: '预览文件',
      operationObject: file.name || file.uid,
      result: '失败',
      remark: error.message || '预览文件失败'
    })
  }
}

// 证据对比
const compareEvidence = async (file) => {
  await compareFile(file, 'evidence', '证据对比')
}

// 判决对比
const compareVerdict = async (file) => {
  await compareFile(file, 'verdict', '判决对比')
}

// 对比文件
const compareFile = async (file, type, title) => {
  try {
    compareVisible.value = true
    compareTitle.value = title
    compareType.value = type
    inputContent.value = '加载中...'
    resultContent.value = '加载中...'
    
    const res = await compareContent(taskId, file.uid, agentId, type)
    if (res.data && res.data.code === 1) {
      const compareData = res.data.data
      // 将换行符转换为 HTML 换行
      inputContent.value = (compareData.inputContent || '').replace(/\n/g, '<br>')
      resultContent.value = (compareData.resultContent || '').replace(/\n/g, '<br>')
      
      if (!inputContent.value || inputContent.value === '') {
        inputContent.value = '<span style="color: #999;">未提取到内容</span>'
      }
      if (!resultContent.value || resultContent.value === '') {
        resultContent.value = '<span style="color: #999;">未提取到内容</span>'
      }
      // 记录操作历史
      await addOperationHistory({
        agentId: agentId,
        operationType: type === 'evidence' ? '证据对比' : '判决对比',
        operationObject: file.name || file.uid,
        result: '成功',
        remark: `任务 ${taskId} 的${type === 'evidence' ? '证据' : '判决'}对比`
      })
    } else {
      ElMessage.error(res.data?.message || '对比失败')
      compareVisible.value = false
      // 记录失败操作
      await addOperationHistory({
        agentId: agentId,
        operationType: type === 'evidence' ? '证据对比' : '判决对比',
        operationObject: file.name || file.uid,
        result: '失败',
        remark: res.data?.message || '对比失败'
      })
    }
  } catch (error) {
    console.error('对比文件失败:', error)
    ElMessage.error('对比文件失败，请稍后重试')
    compareVisible.value = false
    // 记录失败操作
    await addOperationHistory({
      agentId: agentId,
      operationType: type === 'evidence' ? '证据对比' : '判决对比',
      operationObject: file.name || file.uid,
      result: '失败',
      remark: error.message || '对比文件失败'
    })
  }
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

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

// 组件挂载时获取文件列表
onMounted(() => {
  getFileList()
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

.preview-content {
  max-height: 500px;
  overflow-y: auto;
  padding: 10px;
  background-color: #f5f5f5;
  border-radius: 4px;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: 'Courier New', monospace;
  line-height: 1.6;
}

.compare-container {
  display: flex;
  gap: 20px;
  height: 600px;
}

.compare-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
}

.panel-header {
  background-color: #f5f7fa;
  padding: 12px 16px;
  border-bottom: 1px solid #e4e7ed;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.panel-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background-color: #fff;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: 'Courier New', monospace;
  line-height: 1.8;
  font-size: 14px;
}
</style>
