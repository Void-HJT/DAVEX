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
          min-width="150"
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
</template>

<script lang="ts" setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getFileInfo, readFileContent } from '../../api/verdict.js'
import { Document, Timer } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

// 加载状态
const loading = ref(false)
const previewVisible = ref(false)
const previewContent = ref('')

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
    } else {
      previewContent.value = res.data?.message || '读取文件失败'
      ElMessage.error(res.data?.message || '读取文件失败')
    }
  } catch (error) {
    console.error('预览文件失败:', error)
    previewContent.value = '预览文件失败，请稍后重试'
    ElMessage.error('预览文件失败，请稍后重试')
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
</style>
