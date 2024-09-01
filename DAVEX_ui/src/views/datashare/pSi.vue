<template>
  <div>
    <span>PSI</span>
    <el-upload ref="photoRef" :auto-upload="false" :http-request="upload">
      <template #trigger>
        <el-button type="primary">选择文件</el-button>
      </template>

      <el-button class="ml-3" type="success" @click="submitUpload">
        上传文件
      </el-button>
    </el-upload>
    <el-form :model="creatPsiTaskBody" label-width="20%" style="max-width: 80%">
      <el-form-item label="新文件名">
        <el-input v-model="creatPsiTaskBody.runtimeParameters.PK"></el-input>
      </el-form-item>
    </el-form>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed } from 'vue'
import { createPsiTask } from '../../api/pSi.js'
import type { UploadInstance, UploadProps, UploadRawFile } from 'element-plus'
//导入axios
import axios from 'axios'

const creatPsiTaskBody = ref({
  partInfo: [
    {
      agentID: 5,
      part: 1,
      fileID: 21,
    },
  ],
  applicationId: 0, //后台配置
  centerId: 1, //后台配置
  compileParameters: {
    P0_Data: 3,
    P1_Data: 10,
  }, //后续自动读取
  host: '10.176.34.171', //后台配置
  mpcId: 'PSI_GARNET', //后台配置
  n: 2, //目前只需要2方
  part: 0, //发起方默认为第0方
  port: 6000, //后台配置 无需用户在前端选择端口
  runtimeParameters: {
    PK: '案号', //需要手动输入
    protocol: 'semi2k-party', //目前只支持一个协议
  },
  status: 'INIT', //默认INIT
  taskType: 'GARNET_PSI', //后台配置
})

let photoRef = ref()

function upload(params) {
  let formData = new FormData()
  formData.append('file', params.file)
  // 使用 Blob 指定 mpcTask 的 MIME 类型为 application/json
  const mpcTaskJson = JSON.stringify(creatPsiTaskBody.value)
  const mpcTaskBlob = new Blob([mpcTaskJson], { type: 'application/json' })
  formData.append('mpcTask', mpcTaskBlob) // 添加 mpcTask，指定类型
  axios({
    url: 'http://10.176.34.171:9999/MpcTasks/create_with_input',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data', // 使用 multipart/form-data
      Accept: '*/*', // 接受所有响应类型
    },
  }).then((resp) => {
    console.log('success')
  })
}
function submitUpload() {
  photoRef.value.submit()
}
</script>

<style scoped></style>
