<template>
  <el-container>
    <span style="display: block; margin-bottom: 8px;">选择参与方数量</span>
    <el-select v-model="partyNumber" placeholder="Select" style="width: 240px" @change="handleSelectPartyNumber">
      <el-option
          v-for="item in partyNumbers"
          :key="item.value"
          :label="item.label"
          :value="item.value"
      />
    </el-select>
    <span style="display: block; margin-bottom: 8px;">选择代理</span>
    <el-select v-model="agentId" placeholder="Select" style="width: 240px" @change="handleSelectAgent">
      <el-option
          v-for="item in agents"
          :key="item.value"
          :label="item.label"
          :value="item.value"
      />
    </el-select>
    <el-header style="height: 50px">
      <div
          style="
            background-color: antiquewhite;
            height: 40px;
            display: flex;
            justify-content: center;
            align-items: center;
          "
      >
        <p
            style="
              font-size: 20px;
              color: black;
              opacity: 100%;
              text-align: center;
            "
        >
          文件列表
        </p>
      </div>
    </el-header>
    <el-main>
      <div>
        <el-button @click="getDirectoryMethod">返回根目录</el-button>
        <el-button @click="returnFrontDirectory">返回上一级目录</el-button>
      </div>
      <div>
        <el-table
            stripe
            :data="directoryData"
            style="width: 100%"
            @row-dblclick="handleCellDoubleClick"
            max-height="300"
        >
          <el-table-column fixed label="" width="50" align="center">
            <template #default="scope">
              <el-icon>
                <template v-if="scope.row.type === 'folder'">
                  <el-icon color="#409efc"><Folder /></el-icon>
                </template>
                <template v-else-if="scope.row.type === 'file'">
                  <el-icon><Files /></el-icon>
                </template>
              </el-icon>
            </template>
          </el-table-column>
          <el-table-column
              label="文件ID"
              prop="uid"
              width="80"
              align="center"
          ></el-table-column>
          <el-table-column
              label="名称"
              prop="name"
              width="180"
              align="center"
          ></el-table-column>
          <el-table-column
              label="所属代理"
              prop="agentId"
              width="80"
              align="center"
          ></el-table-column>

          <el-table-column
              label="类型"
              prop="type"
              width="180"
              align="center"
          ></el-table-column>
          <el-table-column
              label="创建时间"
              prop="createDate"
              width="300"
              :formatter="formatDate"
              align="center"
          ></el-table-column>
          <el-table-column
              label="更新时间"
              prop="lastUpdate"
              width="300"
              :formatter="formatDate"
              align="center"
          ></el-table-column>
          <el-table-column
              fixed="right"
              label="操作"
              mid-width="300"
              header-align="center"
              align="center"
          >
            <template v-slot="scope">
              <el-button
                  v-if="scope.row.type === 'file'"
                  link
                  type="primary"
                  @click="
                    chooseFileMethod(
                      scope.row.agentId,
                      scope.row.uid,
                      scope.row.name,
                      0
                    )
                  "
                  size="small"
              >
                选择第一方文件
              </el-button>
              <el-button
                  v-if="scope.row.type === 'file'"
                  link
                  type="primary"
                  :disabled="partyNumber === 2"
                  @click="
                    chooseFileMethod(
                      scope.row.agentId,
                      scope.row.uid,
                      scope.row.name,
                      1
                    )
                  "
                  size="small"
              >
                选择第二方文件
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-main>
  </el-container>

  <el-container>
    <el-header style="height: 50px">
      <div
          style="
            background-color: antiquewhite;
            height: 40px;
            display: flex;
            justify-content: center;
            align-items: center;
          "
      >
        <p
            style="
              font-size: 20px;
              color: black;
              opacity: 100%;
              text-align: center;
            "
        >
          上传输入数据
        </p>
      </div>
    </el-header>
    <el-main>
      <el-descriptions
          class="margin-top"
          :title="descriptionsTitle"
          :column="3"
          :size="'default'"
          border
      ></el-descriptions>
      <div class="form-container">
        <el-form
            :model="createPsiTaskBody"
            style="max-width: 60%"
            class="styled-form"
        >
          <el-form-item label="主键">
            <el-input v-model="createPsiTaskBody.runtimeParameters.PK"></el-input>
          </el-form-item>
        </el-form>
      </div>
      <el-upload
          ref="upload"
          class="upload-demo"
          action="/"
          :limit="1"
          :on-exceed="handleExceed"
          :auto-upload="false"
          :on-change="handleFileChange"
          style="margin-top: 20px; margin-bottom: 20px;"
      >
        <template #trigger>
          <el-button type="primary" style="margin-right: 10px;">上传输入文件</el-button>
        </template>
        <el-button class="ml-3" type="success" @click="submitUpload" style="margin-right: 10px;">
          创建PSI任务
        </el-button>
        <template #tip>
          <div class="el-upload__tip text-red">
            limit 1 file, new file will cover the old file
          </div>
        </template>
      </el-upload>
    </el-main>
  </el-container>

<!--  <div>-->
<!--    <div class="form-container">-->
<!--      <el-form-->
<!--        :model="createPsiTaskBody"-->
<!--        style="max-width: 60%"-->
<!--        class="styled-form"-->
<!--      >-->
<!--        <el-form-item label="主键">-->
<!--          <el-input v-model="createPsiTaskBody.runtimeParameters.PK"></el-input>-->
<!--        </el-form-item>-->
<!--        <el-form-item label="AgentID">-->
<!--          <el-input v-model="createPsiTaskBody.partInfo[0].agentID"></el-input>-->
<!--        </el-form-item>-->
<!--        <el-form-item label="文件ID">-->
<!--          <el-input v-model="createPsiTaskBody.partInfo[0].fileID"></el-input>-->
<!--        </el-form-item>-->
<!--        <el-form-item label="选择协议">-->
<!--          <el-input-->
<!--            v-model="createPsiTaskBody.runtimeParameters.protocol"-->
<!--          ></el-input>-->
<!--        </el-form-item>-->
<!--        <el-upload ref="photoRef" :auto-upload="false" :http-request="upload">-->
<!--          <template #trigger>-->
<!--            <el-button type="primary">选择文件</el-button>-->
<!--          </template>-->

<!--          <el-button class="ml-3" type="success" @click="submitUpload">-->
<!--            创建任务-->
<!--          </el-button>-->
<!--        </el-upload>-->
<!--      </el-form>-->
<!--    </div>-->
<!--  </div>-->
<!--  <el-dialog v-model="infoDialogVisible" title="提示信息" width="30%">-->
<!--    <span>{{ infoDialogText }}</span>-->
<!--    <template #footer>-->
<!--      <span class="dialog-footer">-->
<!--        <el-button @click="infoDialogVisible = false">关闭</el-button>-->
<!--      </span>-->
<!--    </template>-->
<!--  </el-dialog>-->
  <el-dialog v-model="psiSuccessVisible" title="创建完成" width="30%">
    <span>{{ psiSuccessMessage }}</span>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="psiSuccessVisible = false" style="margin-right: 10px;">返回</el-button>
        <router-link to="/result/mPc">
          <el-button type="primary">
            查看结果管理区
          </el-button>
        </router-link>
      </div>
    </template>
  </el-dialog>
  <el-dialog v-model="psiFailedVisible" title="创建失败" width="30%">
    <span>{{ psiFailedMessage }}</span>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="psiFailedVisible = false">返回</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import {getAgent} from '../../api/testDve.js'
import {getDirectory, getRootByAgent} from '../../api/folderController.js'
import {ref, computed, onMounted} from 'vue'
import { createPsiTask } from '../../api/pSi.js'
import {genFileId, UploadInstance, UploadProps, UploadRawFile} from 'element-plus'
//导入axios
import axios from 'axios'

onMounted(() => {
  getAgentMethod()
  // getDirectoryMethod()
})

const psiSuccessVisible = ref(false)
const psiFailedVisible = ref(false)
const psiSuccessMessage = ref('')
const psiFailedMessage = ref('')

const descriptionsTitle = computed(() => {
  const firstFileText = fileName.value ? `第一方文件：${fileName.value}` : ''
  const secondFileText = secondFileName.value ? `第二方文件：${secondFileName.value}` : ''

  return [firstFileText, secondFileText].filter(Boolean).join(' \n')
})
const upload = ref<UploadInstance>()
const agents = ref([])
const agentId = ref('')
const partyNumbers = [
  {
    value: 2,
    label: '2',
  },
  // {
  //   value: 3,
  //   label: '3',
  // }
]
const partyNumber = ref(2)
const directoryData = ref([])
const currentDirectoryData = ref([])
const getDirectoryBody = ref({
  rootId: '',
})
const folderRoute = ref([])
const fileName = ref('')
const secondFileName = ref('')
const createPsiTaskBody = ref({
  partInfo: [
    {
      agentID: "", //需要填写
      part: 1, //默认
      fileID: "", //需要填写
    }
  ],
  applicationId: "DAVEX-C1-A1", //后台配置
  centerId: "DAVEX-C1", //后台配置
  compileParameters: {},
  host: '10.176.37.50', //后台配置
  mpcId: 'PSI_GARNET', //后台配置
  n: 2, //目前只需要2方
  part: 0, //发起方默认为第0方
  port: 6000, //后台配置 无需用户在前端选择端口
  runtimeParameters: {
    PK: '', //需要手动输入，可能可以采用读取的方式
    protocol: 'semi2k-party', //目前只支持一个协议 但是后续可能会有多个协议
  },
  status: 'INIT', //默认INIT
  taskType: 'GARNET_PSI', //后台配置
})
// const createPsiTaskBody = ref({
//   partInfo: [
//     {
//       agentID: "", //需要填写
//       part: 1, //默认
//       fileID: "", //需要填写
//     },
//     {
//       agentID: "", //需要填写
//       part: 2, //默认
//       fileID: "", //需要填写
//     }
//   ],
//   applicationId: "DAVEX-C1-AXX1", //后台配置
//   centerId: "DAVEX-C1", //后台配置
//   compileParameters: {},
//   host: '10.176.37.50', //后台配置
//   mpcId: 'correction-supervision', //后台配置
//   mpcName: "xxx",
//   n: 2, //目前只需要2方
//   part: 0, //发起方默认为第0方
//   port: 6000, //后台配置 无需用户在前端选择端口
//   runtimeParameters: {
//     protocol: 'replicated-ring-party', //目前只支持一个协议 但是后续可能会有多个协议
//   },
//   status: 'INIT', //默认INIT
//   taskType: 'GARNET_MPC', //后台配置
//   uid: null
// })

const createBody = ref({
  file: null as File | null,
  mpcTask: createPsiTaskBody.value
})

// let photoRef = ref()

// function upload(params) {
//   let formData = new FormData()
//   formData.append('file', params.file)
//   // 使用 Blob 指定 mpcTask 的 MIME 类型为 application/json
//   const mpcTaskJson = JSON.stringify(createPsiTaskBody.value)
//   const mpcTaskBlob = new Blob([mpcTaskJson], { type: 'application/json' })
//   formData.append('mpcTask', mpcTaskBlob) // 添加 mpcTask，指定类型
//   axios({
//     url: 'http://10.176.34.171:9999/MpcTasks/create_with_input',
//     method: 'post',
//     data: formData,
//     headers: {
//       'Content-Type': 'multipart/form-data', // 使用 multipart/form-data
//       Accept: '*/*', // 接受所有响应类型
//     },
//   }).then((resp) => {
//     console.log('success')
//   })
// }
// function submitUpload() {
//   photoRef.value.submit()
//   infoDialogText.value = '创建PSI任务成功'
//   infoDialogVisible.value = true
// }
// // 信息提示框
// const infoDialogVisible = ref(false)
// const infoDialogText = ref('')

const createMethod = async () => {
  try {
    console.log(createBody.value)
    const res = await createPsiTask(createBody.value)
    console.log(res.data)
    if (res.data.body.code == 1) {
      psiSuccessMessage.value = `PSI任务创建完成，执行完成后将通过消息中心提示`
      psiSuccessVisible.value = true
    }
    else {
      psiFailedMessage.value = res.data.body.message
      psiFailedVisible.value = true
    }
  }
  catch (error) {
    console.error('Failed to create PSI task:', error)
  }
}

function addFolderRoute(row) {
  folderRoute.value.push(row.uid, row.name)
}
function deleteFolderRoute() {
  folderRoute.value.pop()
  folderRoute.value.pop()
}

const chooseFileMethod = async (agentId, fileId, name, partIndex = 0) => {
  try {
    createPsiTaskBody.value.partInfo[partIndex].agentID = agentId
    createPsiTaskBody.value.partInfo[partIndex].fileID = fileId
    if (partIndex === 0) {
      fileName.value = name
    } else {
      secondFileName.value = name
    }
  }
  catch (error) {
    console.error('Failed to choose file:', error)
  }
}

const getDirectoryMethod = async () => {
  try {
    const res = await getDirectory(getDirectoryBody.value)
    const res1 = res.data.data.children
    directoryData.value = res1
    currentDirectoryData.value = '1'
    addFolderRoute(res.data.data)
  } catch (error) {
    console.error('Failed to get group list:', error)
  }
}

const findCurrentFolder = async () => {
  //沿着folderRoute找到当前文件夹
  const res1 = await getDirectory(getDirectoryBody.value)
  const res = res1.data.data.children
  currentDirectoryData.value = res
  if (folderRoute.value.length === 1) {
    directoryData.value = res
    return
  }
  for (let i = 1; i < folderRoute.value.length; i++) {
    for (let j = 0; j < currentDirectoryData.value.length; j++) {
      if (currentDirectoryData.value[j].uid === folderRoute.value[i]) {
        currentDirectoryData.value = currentDirectoryData.value[j].children
        break
      }
    }
  }
  directoryData.value = currentDirectoryData.value
}

const returnFrontDirectory = async () => {
  deleteFolderRoute()
  console.log('folderRoute:', folderRoute.value)
  findCurrentFolder()
}

const currentParentId = ref('1')
const handleCellDoubleClick = async (row) => {
  if (row.type === 'folder') {
    addFolderRoute(row)
    console.log('folderRoute:', folderRoute.value)
    currentParentId.value = row.uid
    directoryData.value = row.children
  }
}

const formatDate = (row, column, cellValue) => {
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

const handleExceed: UploadProps['onExceed'] = (files) => {
  upload.value!.clearFiles()
  const file = files[0] as UploadRawFile
  file.uid = genFileId()
  upload.value!.handleStart(file)
}

const handleFileChange: UploadProps['onChange'] = (file, fileList) => {
  createBody.value.file = file.raw
}

const submitUpload = () => {
  createMethod()
  console.log(createPsiTaskBody.value)
}

const getRootByAgentMethod = async (agentId) => {
  const res = await getRootByAgent(agentId)
  getDirectoryBody.value.rootId = res.data.data.uid
}

const getAgentMethod = async () => {
  const res = await getAgent()
  agents.value = res.data.body.data.map(item => ({
    value: item.uid,
    label: item.uid
  }))
}

const handleSelectAgent = async (value) => {
  agentId.value = value
  await getRootByAgentMethod(agentId.value)
  getDirectoryMethod()
}

const handleSelectPartyNumber = async (value) => {
  partyNumber.value = value
  createPsiTaskBody.value.n = partyNumber.value
}
</script>

<style scoped>
.form-container {
  display: flex;
  justify-content: center;
  align-items: center;
}

.styled-form {
  width: 100%;
  max-width: 600px;
  padding: 20px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  background-color: #ffffff;
  display: flex; /* 使用 Flexbox */
  flex-direction: column; /* 设置为纵向布局 */
  align-items: center; /* 居中对齐子元素 */
}
</style>
