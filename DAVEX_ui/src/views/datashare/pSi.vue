<template>
  <div>
    <span>PSI</span>
    <el-upload
      ref="upload"
      :auto-upload="false"
      action=http://10.176.34.171:9999/MpcTasks/create_with_input
      @change="handleChange"
      width="100%"
    >
      <el-button type="primary">选择文件</el-button>
    </el-upload>
    <el-button @click="submitCreatePsiMethod" style="width: 100%">
      上传到当前文件夹
    </el-button>
    <el-form :model="creatPsiTaskBody" label-width="20%" style="max-width: 80%">
      <el-form-item label="新文件名">
        <el-input v-model="creatPsiTaskBody.runtimeParameters.PK"></el-input>
      </el-form-item>
    </el-form>
    <el-form :model="creatPsiTaskBody" label-width="20%" style="max-width: 80%">
      <el-form-item label="新文件名">
        <el-input v-model="creatPsiTaskBody.agentID2fileID[5]"></el-input>
      </el-form-item>
    </el-form>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed } from 'vue'
import type { UploadInstance, UploadProps, UploadRawFile } from 'element-plus'
const creatPsiTaskBody = ref({
  agentID2fileID: {
    5: [1, 21],
  },
  applicationId: 0,
  centerId: 1,
  compileParameters: {
    P0_Data: 3,
    P1_Data: 10,
  },
  host: '10.176.34.171',
  mpcId: 'PSI_GARNET',
  n: 2,
  part: 0,
  port: 6000,
  runtimeParameters: {
    PK: '案号',
    protocol: 'semi2k-party',
  },
  status: 'INIT',
  taskType: 'GARNET_PSI',
})

const uploadUrl = 'http:// '
const handleChange = () => {
  console.log('upload:', upload.value?.uploadFiles)
}
const upload = ref<UploadInstance | null>(null)

const submitCreatePsiMethod = async () => {
  await upload.value!.submit()
}
</script>

<style scoped></style>
