<template>
  <el-table :data="resultData" style="width: 100%">
    <el-table-column label="UploadDate" width="300">
      <template #default="scope">
        <div style="display: flex; align-items: center">
          <el-icon><timer /></el-icon>
          <span style="margin-left: 10px">{{ formatDate(scope.row.uploadDate) }}</span>
        </div>
      </template>
    </el-table-column>
    <el-table-column label="Name" width="300">
      <template #default="scope">
        <el-popover effect="light" trigger="hover" placement="top" width="auto">
          <template #default>
            <div>name: {{ scope.row.name }}</div>
          </template>
          <template #reference>
            <el-tag>{{ scope.row.name }}</el-tag>
          </template>
        </el-popover>
      </template>
    </el-table-column>
    <el-table-column label="ExpiredDate" width="300">
      <template #default="scope">
        <div style="display: flex; align-items: center">
          <el-icon><timer /></el-icon>
          <span style="margin-left: 10px">{{ formatDate(scope.row.expiredTime) }}</span>
        </div>
      </template>
    </el-table-column>
    <el-table-column label="Operations">
      <template #default="scope">
        <el-button size="small" @click="fetchMpcMethod(scope.row.uid)">
          Fetch
        </el-button>
        <el-button
            size="small"
            type="danger"
            @click="deleteMpcMethod(scope.row.uid)"
        >
          Delete
        </el-button>
      </template>
    </el-table-column>
  </el-table>
  <el-dialog v-model="fetchSuccessVisible" title="文件获取结果" width="30%">
    <span>{{ fetchSuccessMessage }}</span>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="fetchSuccessVisible = false">确定</el-button>
      </div>
    </template>
  </el-dialog>
  <el-dialog v-model="fetchFailedVisible" title="文件获取结果" width="30%">
    <span>{{ fetchFailedMessage }}</span>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="fetchFailedVisible = false">返回</el-button>
      </div>
    </template>
  </el-dialog>
  <el-dialog v-model="deleteSuccessVisible" title="文件删除结果" width="30%">
    <span>{{ deleteSuccessMessage }}</span>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="deleteSuccessVisible = false">确定</el-button>
      </div>
    </template>
  </el-dialog>
  <el-dialog v-model="deleteFailedVisible" title="文件删除结果" width="30%">
    <span>{{ deleteFailedMessage }}</span>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="deleteFailedVisible = false">返回</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import {onMounted, ref} from "vue";
import {getMpcResult, fetchMpc, deleteMpc} from "../../api/mpcOutput.js"

onMounted(() => {
  getMpcResultMethod()
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

const resultData = ref([])
const getResultBody = ref({
  applicationId: '1'
})
const fetchMpcBody = ref({
  outputId: '',
  applicationId: '1'
})
const deleteMpcBody = ref({
  outputId: '',
  applicationId: '1'
})

const getMpcResultMethod = async () => {
  try {
    const res = await getMpcResult(getResultBody.value)
    resultData.value = res.data.data
  }
  catch (error) {
    console.error('Failed to get result:', error)
  }
}

const fetchMpcMethod = async (outputId) => {
  try {
    fetchMpcBody.value.outputId = outputId
    const res = await fetchMpc(fetchMpcBody.value)
    if (res.data.code == 1) {
      fetchSuccessMessage.value = res.data.message;
      fetchSuccessVisible.value = true
    }
    else {
      fetchFailedMessage.value = res.data.message;
      fetchFailedVisible.value = true
    }
  }
  catch (error) {
    console.error('Failed to fetch file:', error)
  }
}

const deleteMpcMethod = async (outputId) => {
  try {
    deleteMpcBody.value.outputId = outputId
    const res = await deleteMpc(deleteMpcBody.value)
    if (res.data.code == 1) {
      deleteSuccessMessage.value = res.data.message;
      deleteSuccessVisible.value = true
      await getMpcResultMethod()
    }
    else {
      deleteFailedMessage.value = res.data.message;
      deleteFailedVisible.value = true
    }
  }
  catch (error) {
    console.error('Failed to delete file:', error)
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
