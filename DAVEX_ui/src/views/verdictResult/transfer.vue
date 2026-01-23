<template>
  <el-container>
    <el-header class="custom-header">
      <div class="icon-text">
        <el-icon><Document /></el-icon>
        <span>文件传输结果列表</span>
      </div>
    </el-header>
    <el-main>
      <el-table :data="filterResultData">
        <el-table-column label="上传时间" width="300">
          <template #default="scope">
            <div style="display: flex; align-items: center">
              <el-icon><timer /></el-icon>
              <span style="margin-left: 10px">{{ formatDate(scope.row.uploadDate) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="文件名" width="300">
          <template #default="scope">
            <el-popover effect="light" trigger="hover" placement="top" width="auto">
              <template #default>
                <div>文件名: {{ scope.row.name }}</div>
                <div>文件类型: {{ scope.row.type }}</div>
                <div>文件标签: {{ scope.row.tag }}</div>
                <div>文件大小: {{ scope.row.size }}</div>
                <div>文件描述: {{ scope.row.description }}</div>
                <div>代理: {{ scope.row.agentId }}</div>
              </template>
              <template #reference>
                <el-tag>{{ scope.row.name }}</el-tag>
              </template>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column label="过期时间" width="300">
          <template #default="scope">
            <div style="display: flex; align-items: center">
              <el-icon><timer /></el-icon>
              <span style="margin-left: 10px">{{ formatDate(scope.row.expiredTime) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="scope">
            <el-button class="small-default-button" @click="readFileMethod(scope.row.uid)">
              <el-icon><View /></el-icon> 预览文件
            </el-button>
            <el-button class="small-default-button" @click="fetchFileMethod(scope.row.uid)">
              <el-icon><Download /></el-icon> 获取文件
            </el-button>
            <el-button class="small-delete-button" @click="deleteFileMethod(scope.row.uid)">
              <el-icon><Delete /></el-icon> 删除文件
            </el-button>
          </template>
        </el-table-column>
        <el-table-column align="right">
          <template #header>
            <el-input v-model="search" size="small" placeholder="搜索" />
          </template>
        </el-table-column>
      </el-table>
    </el-main>
  </el-container>

  <el-dialog v-model="fetchSuccessVisible" title="文件获取结果" width="30%">
    <span>{{ fetchSuccessMessage }}</span>
    <template #footer>
      <div class="dialog-footer">
        <el-button class="close-button" @click="fetchSuccessVisible = false">确定</el-button>
      </div>
    </template>
  </el-dialog>
  <el-dialog v-model="fetchFailedVisible" title="文件获取结果" width="30%">
    <span>{{ fetchFailedMessage }}</span>
    <template #footer>
      <div class="dialog-footer">
        <el-button class="close-button" @click="fetchFailedVisible = false">返回</el-button>
      </div>
    </template>
  </el-dialog>
  <el-dialog v-model="deleteSuccessVisible" title="文件删除结果" width="30%">
    <span>{{ deleteSuccessMessage }}</span>
    <template #footer>
      <div class="dialog-footer">
        <el-button class="close-button" @click="deleteSuccessVisible = false">确定</el-button>
      </div>
    </template>
  </el-dialog>
  <el-dialog v-model="deleteFailedVisible" title="文件删除结果" width="30%">
    <span>{{ deleteFailedMessage }}</span>
    <template #footer>
      <div class="dialog-footer">
        <el-button class="close-button" @click="deleteFailedVisible = false">返回</el-button>
      </div>
    </template>
  </el-dialog>
  <el-dialog v-model="readSuccessVisible" title="文件预览结果" width="70%">
    <span v-html="readSuccessMessage"></span>
    <template #footer>
      <div class="dialog-footer">
        <el-button class="close-button" @click="readSuccessVisible = false">确定</el-button>
      </div>
    </template>
  </el-dialog>
  <el-dialog v-model="readFailedVisible" title="文件预览结果" width="30%">
    <span>{{ readFailedMessage }}</span>
    <template #footer>
      <div class="dialog-footer">
        <el-button class="close-button" @click="readFailedVisible = false">返回</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import {onMounted, ref, computed} from "vue";
import {getResult, fetchFile, deleteFile, readFile} from "../../api/direct.js"
import {Delete, Document, Download} from "@element-plus/icons-vue";
import { addOperationHistory } from '../../api/operationHistory.js'

onMounted(() => {
  getResultDataMethod()
})

// 对话框是否可见
const fetchSuccessVisible = ref(false)
const fetchFailedVisible = ref(false)
const fetchSuccessMessage = ref('');
const fetchFailedMessage = ref('');
const deleteSuccessVisible = ref(false)
const deleteFailedVisible = ref(false)
const deleteSuccessMessage = ref('');
const deleteFailedMessage = ref('');
const readSuccessVisible = ref(false)
const readFailedVisible = ref(false)
const readSuccessMessage = ref('');
const readFailedMessage = ref('');
const search = ref('')
const filterResultData = computed(() =>
    resultData.value.filter((data) => {
      const searchValue = search.value?.toLowerCase(); // 转为小写避免大小写问题
      return (
          !searchValue || // 如果没有输入搜索词，保留所有数据
          data.name.toLowerCase().includes(searchValue) || // 检查 name 字段
          (data.tag && data.tag.toLowerCase().includes(searchValue)) // 检查 tag 字段
      );
    })
)

const applicationId = "DAVEX-C1-A1"
const resultData = ref([])
const getResultBody = ref({
  applicationId: applicationId
})
const fetchFileBody = ref({
  outputId: '',
  applicationId: applicationId
})
const deleteFileBody = ref({
  outputId: '',
  applicationId: applicationId
})
const readFileBody = ref({
  outputId: '',
  applicationId: applicationId
})

const getResultDataMethod = async () => {
  try {
    const res = await getResult(getResultBody.value)
    resultData.value = res.data.data
  }
  catch (error) {
    console.error('Failed to get result:', error)
  }
}

const fetchFileMethod = async (outputId) => {
  try {
    fetchFileBody.value.outputId = outputId
    const res = await fetchFile(fetchFileBody.value)
    if (res.data.code == 1) {
      fetchSuccessMessage.value = res.data.message;
      fetchSuccessVisible.value = true
      // 记录操作历史
      await addOperationHistory({
        operationType: '获取文件',
        operationObject: outputId,
        result: '成功',
        remark: '从传输结果中获取文件'
      })
    }
    else {
      fetchFailedMessage.value = res.data.message;
      fetchFailedVisible.value = true
      // 记录失败操作
      await addOperationHistory({
        operationType: '获取文件',
        operationObject: outputId,
        result: '失败',
        remark: res.data.message || '获取文件失败'
      })
    }
  }
  catch (error) {
    console.error('Failed to fetch file:', error)
    await addOperationHistory({
      operationType: '获取文件',
      operationObject: outputId,
      result: '失败',
      remark: error.message || '获取文件失败'
    })
  }
}

const deleteFileMethod = async (outputId) => {
  try {
    deleteFileBody.value.outputId = outputId
    const res = await deleteFile(deleteFileBody.value)
    if (res.data.code == 1) {
      deleteSuccessMessage.value = res.data.message;
      deleteSuccessVisible.value = true
      await getResultDataMethod()
      // 记录操作历史
      await addOperationHistory({
        operationType: '删除文件',
        operationObject: outputId,
        result: '成功',
        remark: '从传输结果中删除文件'
      })
    }
    else {
      deleteFailedMessage.value = res.data.message;
      deleteFailedVisible.value = true
      // 记录失败操作
      await addOperationHistory({
        operationType: '删除文件',
        operationObject: outputId,
        result: '失败',
        remark: res.data.message || '删除文件失败'
      })
    }
  }
  catch (error) {
    console.error('Failed to delete file:', error)
    await addOperationHistory({
      operationType: '删除文件',
      operationObject: outputId,
      result: '失败',
      remark: error.message || '删除文件失败'
    })
  }
}

const readFileMethod = async (outputId) => {
  try {
    readFileBody.value.outputId = outputId
    const res = await readFile(readFileBody.value)
    if (res.data.code == 1) {
      readSuccessMessage.value = res.data.data.replace(/\n/g, '<br>')
      readSuccessVisible.value = true
      // 记录操作历史
      await addOperationHistory({
        operationType: '预览文件',
        operationObject: outputId,
        result: '成功',
        remark: '预览传输结果文件'
      })
    }
    else {
      readFailedMessage.value = res.data.message
      readFailedVisible.value = true
      // 记录失败操作
      await addOperationHistory({
        operationType: '预览文件',
        operationObject: outputId,
        result: '失败',
        remark: res.data.message || '预览文件失败'
      })
    }
  }
  catch (error) {
    console.error('Failed to read file:', error)
    await addOperationHistory({
      operationType: '预览文件',
      operationObject: outputId,
      result: '失败',
      remark: error.message || '预览文件失败'
    })
  }
}

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
</script>

<style scoped></style>
