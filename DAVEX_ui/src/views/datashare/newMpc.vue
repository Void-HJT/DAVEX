<template>
  <el-container>
    <span style="display: block; margin-bottom: 10px;">参与方数量</span>
    <el-select v-model="partyNumber" placeholder="Select" @change="handleSelectPartyNumber">
      <el-option
          v-for="item in partyNumbers"
          :key="item.value"
          :label="item.label"
          :value="item.value"
      />
    </el-select>
    <span style="display: block; margin-bottom: 10px;">代理</span>
    <el-select v-model="agentId" placeholder="选择代理" @change="handleSelectAgent">
      <el-option
          v-for="item in agents"
          :key="item.value"
          :label="item.label"
          :value="item.value"
      />
    </el-select>
    <el-header class="custom-header">
      <div class="icon-text">
        <el-icon><Folder /></el-icon>
        <span>文件列表</span>
      </div>
    </el-header>
    <el-main>
      <div>
        <el-button class="default-button" @click="getDirectoryMethod">返回根目录</el-button>
        <el-button class="default-button" @click="returnFrontDirectory">返回上一级目录</el-button>
      </div>
      <div>
        <el-table
            :data="directoryData"
            @row-dblclick="handleCellDoubleClick"
            max-height="400"
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
              width="200"
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
              width="200"
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
                  class="small-default-button"
                  v-if="scope.row.type === 'file' && scope.row.fileType?.includes('mpc')"
                  @click="
                    chooseFileMethod(
                      scope.row.agentId,
                      scope.row.uid,
                      scope.row.name,
                      0
                    )
                  "
              >
                <el-icon><Connection /></el-icon> 选择第一方文件
              </el-button>
              <el-button
                  :class="[
                    'small-default-button',
                    { 'small-default-button-disabled': partyNumber === 2 }
                  ]"
                  v-if="scope.row.type === 'file' && scope.row.fileType?.includes('mpc')"
                  :disabled="partyNumber === 2"
                  @click="
                    chooseFileMethod(
                      scope.row.agentId,
                      scope.row.uid,
                      scope.row.name,
                      1
                    )
                  "
              >
                <el-icon><Connection /></el-icon> 选择第二方文件
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-main>
  </el-container>

  <el-container>
    <el-header class="custom-header">
      <div class="icon-text">
        <el-icon><Tickets /></el-icon>
        <span>选择MPC文件</span>
      </div>
    </el-header>
    <el-main>
      <div>
        <el-table
            :data="mpcList"
            max-height="400"
        >
          <el-table-column fixed label="" width="50" align="center">
            <template #default="scope">
              <el-icon>
                <template>
                  <el-icon><Files /></el-icon>
                </template>
              </el-icon>
            </template>
          </el-table-column>
          <el-table-column
              label="MPC文件ID"
              prop="uid"
              width="200"
              align="center"
          ></el-table-column>
          <el-table-column
              label="名称"
              prop="name"
              width="180"
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
                  class="small-default-button"
                  @click="showParameters(scope.row, 'compile')"
              >
                <el-icon><Tickets /></el-icon> 查看编译参数
              </el-button>
              <el-button
                  class="small-default-button"
                  @click="showParameters(scope.row, 'runtime')"
              >
                <el-icon><Tickets /></el-icon> 查看运行参数
              </el-button>
              <el-button
                  class="small-default-button"
                  @click="chooseMpcMethod(scope.row)"
              >
                <el-icon><Tickets /></el-icon> 选择该MPC文件
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-main>
  </el-container>

  <el-container>
    <el-header class="custom-header">
      <div class="icon-text">
        <el-icon><Tickets /></el-icon>
        <span>上传输入数据</span>
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
      <!--      <div class="form-container">-->
      <!--        <el-form-->
      <!--            :model="createMpcTaskBody"-->
      <!--            style="max-width: 60%"-->
      <!--            class="styled-form"-->
      <!--        >-->
      <!--          <el-form-item label="主键">-->
      <!--            <el-input v-model="createMpcTaskBody.runtimeParameters.PK"></el-input>-->
      <!--          </el-form-item>-->
      <!--        </el-form>-->
      <!--      </div>-->
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
          <el-button class="default-button" style="margin-right: 10px;">上传输入文件</el-button>
        </template>
        <el-button class="default-button" @click="editCompileDialog">
          配置编译参数
        </el-button>
        <el-button class="default-button" @click="editRuntimeDialog">
          配置运行参数
        </el-button>
        <div style="margin-top: 10px">
          <el-button class="start-button" @click="submitUpload">
            创建MPC任务
          </el-button>
        </div>
        <template #tip>
          <div class="el-upload__tip text-red">
            限制1个文件，新文件将覆盖旧文件
          </div>
        </template>
      </el-upload>
    </el-main>
  </el-container>

  <!--  <div>-->
  <!--    <div class="form-container">-->
  <!--      <el-form-->
  <!--        :model="createMpcTaskBody"-->
  <!--        style="max-width: 60%"-->
  <!--        class="styled-form"-->
  <!--      >-->
  <!--        <el-form-item label="主键">-->
  <!--          <el-input v-model="createMpcTaskBody.runtimeParameters.PK"></el-input>-->
  <!--        </el-form-item>-->
  <!--        <el-form-item label="AgentID">-->
  <!--          <el-input v-model="createMpcTaskBody.partInfo[0].agentID"></el-input>-->
  <!--        </el-form-item>-->
  <!--        <el-form-item label="文件ID">-->
  <!--          <el-input v-model="createMpcTaskBody.partInfo[0].fileID"></el-input>-->
  <!--        </el-form-item>-->
  <!--        <el-form-item label="选择协议">-->
  <!--          <el-input-->
  <!--            v-model="createMpcTaskBody.runtimeParameters.protocol"-->
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
  <el-dialog v-model="mpcSuccessVisible" title="创建完成" width="30%">
    <span>{{ mpcSuccessMessage }}</span>
    <template #footer>
      <div class="dialog-footer">
        <el-button class="close-button" @click="mpcSuccessVisible = false" style="margin-right: 10px;">返回</el-button>
        <router-link to="/result/mPc">
          <el-button class="default-button">
            查看结果管理区
          </el-button>
        </router-link>
      </div>
    </template>
  </el-dialog>
  <el-dialog v-model="mpcFailedVisible" title="创建失败" width="30%">
    <span>{{ mpcFailedMessage }}</span>
    <template #footer>
      <div class="dialog-footer">
        <el-button class="close-button" @click="mpcFailedVisible = false">返回</el-button>
      </div>
    </template>
  </el-dialog>

  <el-dialog
      :title="parameterDialogTitle"
      v-model="parameterDialogVisible"
      width="60%"
  >
    <el-table :data="currentParameters">
      <el-table-column
          prop="name"
          label="参数名称"
          align="center"
      ></el-table-column>
      <el-table-column
          prop="parameterType"
          label="参数类型"
          align="center"
      ></el-table-column>
      <el-table-column
          prop="limitType"
          label="参数限制"
          align="center"
      ></el-table-column>
      <el-table-column label="限制条件" align="center">
        <template #default="scope">
          <span>
            {{ formatLimit(scope.row.limit) }}
          </span>
        </template>
      </el-table-column>
    </el-table>
    <template #footer>
      <el-button @click="parameterDialogVisible = false">关闭</el-button>
    </template>
  </el-dialog>

  <!-- 配置编译参数 -->
  <el-dialog
      title="配置编译参数"
      v-model="editCompileVisible"
      width="60%"
  >
    <el-table :data="currentCompileParameters">
      <el-table-column
          prop="name"
          label="参数名称"
          align="center"
      ></el-table-column>
      <el-table-column
          prop="limitType"
          label="参数类型"
          align="center"
      ></el-table-column>
      <el-table-column
          prop="description"
          label="参数描述"
          align="center"
      ></el-table-column>
      <el-table-column prop="limit" label="限制条件" align="center">
        <template #default="scope">
          <span>{{ formatLimit(scope.row.limit) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="设置值" align="center">
        <template #default="scope">
          <el-input v-model="scope.row.value"></el-input>
        </template>
      </el-table-column>
    </el-table>
    <template #footer>
      <el-button class="default-button" @click="saveCompileParameters">
        保存
      </el-button>
      <el-button class="close-button" @click="editCompileVisible = false">取消</el-button>
    </template>
  </el-dialog>

  <!-- 配置运行参数 -->
  <el-dialog
      title="配置运行参数"
      v-model="editRuntimeVisible"
      width="60%"
  >
    <el-table :data="currentRuntimeParameters">
      <el-table-column
          prop="name"
          label="参数名称"
          align="center"
      ></el-table-column>
      <el-table-column
          prop="limitType"
          label="参数类型"
          align="center"
      ></el-table-column>
      <el-table-column prop="limit" label="限制条件" align="center">
        <template #default="scope">
          <span>{{ formatLimit(scope.row.limit) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="设置值" align="center">
        <template #default="scope">
          <el-input v-model="scope.row.value"></el-input>
        </template>
      </el-table-column>
    </el-table>
    <template #footer>
      <el-button class="default-button" @click="saveRuntimeParameters">
        保存
      </el-button>
      <el-button class="close-button" @click="editRuntimeVisible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import {getAgent} from '../../api/testDve.js'
import {getDirectory, getRootByAgent} from '../../api/folderController.js'
import {ref, computed, onMounted} from 'vue'
import { createMpcTask, getMpcList } from '../../api/mpC.js'
import {genFileId, UploadInstance, UploadProps, UploadRawFile} from 'element-plus'
import {Connection, Tickets} from "@element-plus/icons-vue";

onMounted(() => {
  getAgentMethod()
  getMpcListMethod()
})

const mpcSuccessVisible = ref(false)
const mpcFailedVisible = ref(false)
const mpcSuccessMessage = ref('')
const mpcFailedMessage = ref('')

const descriptionsTitle = computed(() => {
  const firstFileText = fileName.value ? `第一方文件：${fileName.value}` : ''
  const secondFileText = secondFileName.value ? `第二方文件：${secondFileName.value}` : ''
  const mpcFileText = mpcFileName.value ? `MPC文件：${mpcFileName.value}` : ''

  return [firstFileText, secondFileText, mpcFileText].filter(Boolean).join(' \n')
})
const upload = ref<UploadInstance>()
const agents = ref([])
const agentId = ref('')
const partyNumbers = [
  {
    value: 2,
    label: '2',
  },
  {
    value: 3,
    label: '3',
  }
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
const createMpcTaskBody = ref({
  partInfo: [
    {
      agentID: "", //需要填写
      part: 1, //默认
      fileID: "", //需要填写
    },
    // {
    //   agentID: "", //需要填写
    //   part: 2, //默认
    //   fileID: "", //需要填写
    // }
  ],
  applicationId: "DAVEX-C1-A1", //后台配置
  centerId: "DAVEX-C1", //后台配置
  compileParameters: {
  },
  host: '10.176.37.50', //后台配置
  mpcId: 'decision-tree', //后台配置
  mpcName: "决策树训练",
  n: 2, //目前只需要2方
  part: 0, //发起方默认为第0方
  port: 6000, //后台配置 无需用户在前端选择端口
  runtimeParameters: {
    // protocol: 'replicated-ring-party',
  },
  status: 'INIT', //默认INIT
  taskType: 'GARNET_MPC', //后台配置
  uid: null
})

const createBody = ref({
  file: null as File | null,
  mpcTask: createMpcTaskBody.value
})

const createMethod = async () => {
  try {
    console.log(createBody.value)
    const res = await createMpcTask(createBody.value)
    console.log(res.data)
    if (res.data.body.code == 1) {
      mpcSuccessMessage.value = `MPC任务创建完成，执行完成后将通过消息中心提示`
      mpcSuccessVisible.value = true
    }
    else {
      mpcFailedMessage.value = res.data.message
      mpcFailedVisible.value = true
    }
  }
  catch (error) {
    console.error('Failed to create MPC task:', error)
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
    createMpcTaskBody.value.partInfo[partIndex].agentID = agentId
    createMpcTaskBody.value.partInfo[partIndex].fileID = fileId
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
  console.log(createMpcTaskBody.value)
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
  createMpcTaskBody.value.n = partyNumber.value
  // 根据选择的参与方数量动态生成 partInfo
  createMpcTaskBody.value.partInfo = Array.from({ length: partyNumber.value - 1 }, (_, index) => ({
    agentID: "",
    part: index + 1,
    fileID: ""
  }))
}

const parameterDialogVisible = ref(false)
const currentParameters = ref([])
const parameterDialogTitle = ref('')
const formatLimit = (limit) => {
  return Object.entries(limit)
      .map(([key, value]) => `${key}: ${value}`)
      .join(' ')
}
const showParameters = (row, type) => {
  if (type === 'runtime') {
    currentParameters.value = row.runtimeParameters
    parameterDialogTitle.value = '查看运行参数'
  } else if (type === 'compile') {
    currentParameters.value = row.compileParameters
    parameterDialogTitle.value = '查看编译参数'
  }
  parameterDialogVisible.value = true
}

const mpcList = ref([])
const getMpcListMethod = async () => {
  try {
    const res = await getMpcList()
    mpcList.value = res.data.body.data
    console.log(mpcList.value)
  } catch (error) {
    console.error('Failed to get mpc list:', error)
  }
}

const mpcFileName = ref('')
const chooseMpcMethod = async (row) => {
  try {
    // 遍历传入的 parameters 并将 defaultValue 保存到 createMpcTaskBody 中
    row.compileParameters.forEach(param => {
      if (param.limit && param.limit.defaultValue !== undefined) {
        createMpcTaskBody.value.compileParameters[param.name] = param.limit.defaultValue
      }
    })
    row.runtimeParameters.forEach(param => {
      if (param.limit && param.limit.defaultValue !== undefined) {
        createMpcTaskBody.value.runtimeParameters[param.name] = param.limit.defaultValue
      }
    })
    createMpcTaskBody.value.mpcId = row.uid
    createMpcTaskBody.value.mpcName = row.name
    console.log(createMpcTaskBody.value.compileParameters)
    console.log(createMpcTaskBody.value.runtimeParameters)
    mpcFileName.value = row.name
  }
  catch (error) {
    console.error('Failed to choose mpc file:', error)
  }
}

const editCompileVisible = ref(false)
const editRuntimeVisible = ref(false)
const currentCompileParameters = ref([])
const currentRuntimeParameters = ref([])
const editCompileDialog = () => {
  const mpc = mpcList.value.find(
      (t) => t.uid === createMpcTaskBody.value.mpcId,
  )
  if (mpc) {
    currentCompileParameters.value = mpc.compileParameters.map((param) => ({
      ...param,
      // value: param.limit.defaultValue || '',
      value: param.limit?.defaultValue ?? '',
    }))
    editCompileVisible.value = true
  }
}
const editRuntimeDialog = () => {
  const task = mpcList.value.find(
      (t) => t.uid === createMpcTaskBody.value.mpcId,
  )
  if (task) {
    currentRuntimeParameters.value = task.runtimeParameters.map((param) => ({
      ...param,
      // value: param.limit.defaultValue || '',
      value: param.limit?.defaultValue ?? '',
    }))
  }
  editRuntimeVisible.value = true
}

const saveCompileParameters = () => {
  currentCompileParameters.value.forEach((param) => {
    // 检查参数值是否为数字，如果是则转换为数字
    if (!isNaN(param.value) && param.limitType === 'NUM') {
      createMpcTaskBody.value.compileParameters[param.name] = Number(param.value)
    } else {
      // 对于其他类型（如字符串），直接存储
      createMpcTaskBody.value.compileParameters[param.name] = param.value
    }
  })
  editCompileVisible.value = false
}

const saveRuntimeParameters = () => {
  currentRuntimeParameters.value.forEach((param) => {
    createMpcTaskBody.value.runtimeParameters[param.name] = param.value
  })
  editRuntimeVisible.value = false
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
