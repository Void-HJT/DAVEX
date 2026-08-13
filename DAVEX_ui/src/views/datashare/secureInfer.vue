<template>
  <el-container>
    <span style="display: block; margin-bottom: 10px;">代理</span>
    <el-select v-model="agentId" placeholder="Select" @change="handleSelectAgent">
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
              width="180"
              header-align="center"
              align="center"
          >
            <template v-slot="scope">
              <el-button
                  class="small-default-button"
                  v-if="scope.row.type === 'file'"
                  @click="
                  chooseModelMethod(
                    scope.row.agentId,
                    scope.row.uid,
                    scope.row.name
                  )
                "
              >
                <el-icon><Connection /></el-icon> 选择该模型文件
              </el-button>
              <!-- <el-button
                  v-if="scope.row.type === 'file' && scope.row.name.endsWith('.csv') && !isAccessible(scope.row.ruleList)"
                  link
                  type="danger"
                  size="small"
                  disabled
              >
                无权比对
              </el-button> -->
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
      <!-- MPC 文件列表铺满内容区，操作按钮保持横向排列。 -->
      <div class="mpc-table-wrapper">
        <el-table
          :data="mpcList"
          max-height="400"
          style="width: 100%"
          stripe
        >
          <el-table-column
            label="MPC文件ID"
            prop="uid"
            width="280"
            align="center"
          ></el-table-column>

          <el-table-column
            label="名称"
            prop="name"
            width="260"
            align="center"
          ></el-table-column>

          <el-table-column
            label="操作"
            min-width="480"
            header-align="center"
            align="center"
          >
            <template #default="scope">
              <div class="mpc-operation-actions">
                <el-button
                  class="small-default-button"
                  @click="showParameters(scope.row, 'compile')"
                >
                  <el-icon><Tickets /></el-icon>
                  查看编译参数
                </el-button>

                <el-button
                  class="small-default-button"
                  @click="showParameters(scope.row, 'runtime')"
                >
                  <el-icon><Tickets /></el-icon>
                  查看运行参数
                </el-button>

                <el-button
                  class="small-default-button"
                  @click="chooseMpcMethod(scope.row)"
                >
                  <el-icon><Tickets /></el-icon>
                  选择该MPC文件
                </el-button>
              </div>
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
          <el-button class="default-button" style="margin-right: 10px;">上传txt文件</el-button>
        </template>
        <el-button class="default-button" @click="editCompileDialog">
          配置编译参数
        </el-button>
        <el-button class="default-button" @click="editRuntimeDialog">
          配置运行参数
        </el-button>
        <div style="margin-top: 10px">
          <el-button class="start-button" @click="submitUpload">
            创建安全推理任务
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
  
  <el-dialog v-model="flSuccessVisible" title="创建完成" width="30%">
    <span>{{ flSuccessMessage }}</span>
    <template #footer>
      <div class="dialog-footer">
        <el-button class="close-button" @click="flSuccessVisible = false" style="margin-right: 10px;">返回</el-button>
        <router-link to="/result/comPare">
          <el-button class="default-button">
            查看结果管理区
          </el-button>
        </router-link>
      </div>
    </template>
  </el-dialog>
  <el-dialog v-model="flFailedVisible" title="创建失败" width="30%">
    <span>{{ flFailedMessage }}</span>
    <template #footer>
      <div class="dialog-footer">
        <el-button class="close-button" @click="flFailedVisible = false">返回</el-button>
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
import {create} from "../../api/secureInfer.js";
import { getMpcList } from '../../api/mpC.js'
import {computed, onMounted, ref} from "vue";
import {genFileId, UploadInstance, UploadProps, UploadRawFile} from "element-plus";
import {Connection, Tickets} from "@element-plus/icons-vue";

onMounted(() => {
  getAgentMethod()
  getMpcListMethod()
})

// 对话框是否可见
const flSuccessVisible = ref(false)
const flFailedVisible = ref(false)
const flSuccessMessage = ref('')
const flFailedMessage = ref('')
const inputDataVisible = ref(false)

const descriptionsTitle = computed(() => {
  const firstFileText = fileName.value ? `模型文件：${fileName.value}` : ''
  const mpcFileText = mpcFileName.value ? `MPC文件：${mpcFileName.value}` : ''

  return [firstFileText, mpcFileText].filter(Boolean).join(' \n')
})

const agents = ref([])
const agentId = ref('')
const applicationId = "DAVEX-C1-A1"
const directoryData = ref([])
const currentDirectoryData = ref([])
// const getDirectoryBody = ref({
//   applicationId: applicationId,
//   agentId: '5'
// })
const getDirectoryBody = ref({
  rootId: '',
})
const folderRoute = ref([])
const getTableHeaderBody = ref({
  agentId: '',
  fileId: '',
  folderId: ''
})
const inferenceInfo = ref({
  agentId: '',
  fileId: '',
  applicationId: applicationId,
  compileParameters: {},
  runtimeParameters: {},
})
const createBody = ref({
  file: null as File | null,
  inferenceInfo: inferenceInfo
})
const fileName = ref('')
const upload = ref<UploadInstance>()

function addFolderRoute(row) {
  folderRoute.value.push(row.uid, row.name)
}
function deleteFolderRoute() {
  folderRoute.value.pop()
  folderRoute.value.pop()
}

const createMethod = async () => {
  try {
    console.log(createBody.value)
    const res = await create(createBody.value)
    console.log(res.data)
    if (res.data.body.code == 1) {
      flSuccessMessage.value = `安全推理任务创建完成，执行完成后将通过消息中心提示`
      flSuccessVisible.value = true
    }
    else {
      flFailedMessage.value = res.data.body.message
      flFailedVisible.value = true
    }
  }
  catch (error) {
    console.error('Failed to create secure inference task:', error)
  }
}

const chooseModelMethod = async (agentId, fileId, name) => {
  try {
    inferenceInfo.value.agentId = agentId
    inferenceInfo.value.fileId = fileId
    fileName.value = name
  }
  catch (error) {
    console.error('Failed to choose model file:', error)
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
}

const isAccessible = (ruleList) => {
  return ruleList.includes('comparison')
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
    // 选择 MPC 时先复制注册默认值，参数编辑弹窗可在此基础上覆盖。
    inferenceInfo.value.compileParameters = {}
    inferenceInfo.value.runtimeParameters = {}

    row.compileParameters.forEach((param) => {
      if (param.limit?.defaultValue !== undefined) {
        inferenceInfo.value.compileParameters[param.name] =
            param.limit.defaultValue
      }
    })

    row.runtimeParameters.forEach((param) => {
      if (param.limit?.defaultValue !== undefined) {
        inferenceInfo.value.runtimeParameters[param.name] =
            param.limit.defaultValue
      }
    })

    mpcFileName.value = row.name
  } catch (error) {
    console.error('Failed to choose mpc file:', error)
  }
}

const editCompileVisible = ref(false)
const editRuntimeVisible = ref(false)
const currentCompileParameters = ref([])
const currentRuntimeParameters = ref([])
const editCompileDialog = () => {
  const mpc = mpcList.value.find(
      (t) => t.name === mpcFileName.value,
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
      (t) => t.name === mpcFileName.value,
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
    // 数值编译参数保持 Number 类型，避免后端收到无法用于编译的字符串。
    if (!isNaN(param.value) && param.limitType === 'NUM') {
      inferenceInfo.value.compileParameters[param.name] =
          Number(param.value)
    } else {
      inferenceInfo.value.compileParameters[param.name] = param.value
    }
  })

  editCompileVisible.value = false
}

const saveRuntimeParameters = () => {
  currentRuntimeParameters.value.forEach((param) => {
    inferenceInfo.value.runtimeParameters[param.name] = param.value
  })

  editRuntimeVisible.value = false
}
</script>

<style scoped></style>
