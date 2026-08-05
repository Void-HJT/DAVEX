<template>
  <!-- 先保留原页面布局，只在顶部增加统一的隐私计算任务选择。 -->
  <el-container class="privacy-task-container">
    <el-header class="custom-header">
      <div class="icon-text">
        <span>隐私计算任务</span>
      </div>
    </el-header>
    <el-main>
      <el-form label-width="140px" class="privacy-task-form">
        <el-form-item label="任务类型" required>
          <el-radio-group v-model="taskType" @change="handleTaskTypeChange">
            <el-radio-button label="GARNET_MPC">安全多方计算</el-radio-button>
            <el-radio-button label="GARNET_PSI">隐私集合求交</el-radio-button>
            <el-radio-button label="GARNET_INFERENCE">安全推理</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="功能介绍">
          <el-alert
            :title="taskTypeDescription"
            type="info"
            :closable="false"
            show-icon
          />
        </el-form-item>

      </el-form>
    </el-main>
  </el-container>

   <!-- 将 MPC 文件选择移动到隐私计算任务之后、代理文件选择之前。 -->
  <el-container>
    <el-header class="custom-header">
      <div class="icon-text">
        <el-icon><Tickets /></el-icon>
        <span>选择MPC文件</span>
      </div>
    </el-header>

    <el-main>
      <div class="mpc-table-wrapper">
        <el-table
          :data="filteredMpcList"
          empty-text="当前功能暂无可用MPC文件"
          max-height="400"
          style="width: 100%"
          stripe
        >
          <el-table-column
          label="名称"
          prop="name"
          width="420"
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
    <div style="display: flex; gap: 10px; align-items: center;">
      <div v-if="taskType === 'GARNET_MPC'">
        <span style="display: block; margin-bottom: 5px;">参与方数量</span>
        <el-select
          v-model="partyNumber"
          placeholder="选择参与方数量"
          @change="handleSelectPartyNumber"
        >
          <el-option
            v-for="item in partyNumbers"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </div>
      <div>
        <span style="display: block; margin-bottom: 5px;">代理</span>
        <el-select v-model="agentId" placeholder="选择代理" @change="handleSelectAgent">
          <el-option
              v-for="item in agents"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          />
        </el-select>
      </div>
    </div>
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
                  v-if="scope.row.type === 'file'"
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
      <div v-if="taskType === 'GARNET_PSI'" class="psi-primary-key-form">
        <el-form label-width="80px">
          <el-form-item label="主键" required>
            <el-input
              v-model="psiPrimaryKey"
              placeholder="请输入CSV文件中的主键列名，例如 id"
              style="width: 375px"
            />
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
          <el-button class="default-button" style="margin-right: 10px;">上传输入文件</el-button>
        </template>
        <el-button class="default-button" @click="editCompileDialog">
          配置编译参数
        </el-button>
        <el-button class="default-button" @click="editRuntimeDialog">
          配置运行参数
        </el-button>
        <div style="margin-top: 10px">
          <el-button
            class="start-button"
            :loading="creatingTask"
            :disabled="creatingTask"
            @click="submitUpload"
          >
            {{ creatingTask ? '正在创建任务...' : '创建MPC任务' }}
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
import {ElMessage,genFileId, UploadInstance, UploadProps, UploadRawFile} from 'element-plus'
import {Connection, Tickets} from "@element-plus/icons-vue";

onMounted(() => {
  getAgentMethod()
  getMpcListMethod()
})

// 当前只增加统一入口，后续再按任务类型接入三个页面各自的提交逻辑。
const taskType = ref('GARNET_MPC')
const taskTypeDescriptions = {
  GARNET_MPC:
    '使用通用MPC程序，在不公开参与方原始输入的情况下完成联合计算。',
  GARNET_PSI:
    '在不公开参与方完整数据集的情况下，根据指定主键计算数据交集。',
  GARNET_INFERENCE:
    '参与方分别提供私有模型和待推理数据，在不泄露模型参数及原始数据的情况下完成安全推理。',
}
const taskTypeDescription = computed(
  () => taskTypeDescriptions[taskType.value],
)
const psiPrimaryKey = ref('')

// PSI和安全推理当前固定为两方；只有通用MPC开放参与方数量选择。
const handleTaskTypeChange = async (value) => {
 // 将页面选择同步到实际提交对象。
  createMpcTaskBody.value.taskType = value

  if (value !== 'GARNET_MPC') {
    partyNumber.value = 2
    handleSelectPartyNumber(2)
  }

  if (value !== 'GARNET_PSI') {
    psiPrimaryKey.value = ''
    delete createMpcTaskBody.value.runtimeParameters.PK
  }

  // 切换功能后清除上一个功能选择的 MPC 和参数。
  createMpcTaskBody.value.mpcId = ''
  createMpcTaskBody.value.mpcName = ''
  createMpcTaskBody.value.compileParameters = {}
  createMpcTaskBody.value.runtimeParameters = {}
  mpcFileName.value = ''
  await getMpcListMethod()
}

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
  mpcId: '', //后台配置
  mpcName: "",
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

const countInputValues = async (file: File) => {
  const content = await file.text()

  if (!content.trim()) {
    return 0
  }

  return content.trim().split(/\s+/).filter(Boolean).length
}

const validatePsiFile = async (file: File, primaryKey: string) => {
  const content = await file.text()
  const lines = content
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter(Boolean)

  if (lines.length < 2) {
    ElMessage.error('PSI输入文件必须包含表头和至少一条数据')
    return false
  }

  const headers = lines[0]
    .split(',')
    .map((header) => header.trim())

  if (!headers.includes(primaryKey)) {
    ElMessage.error(`PSI输入文件中不存在主键列：${primaryKey}`)
    return false
  }

  return true
}

const validateCreateTask = async () => {
  if (!createMpcTaskBody.value.mpcId) {
    ElMessage.warning('请先选择MPC文件')
    return false
  }

  if (!createBody.value.file) {
    ElMessage.warning('请先上传所需文件')
    return false
  }

  if (
    !createMpcTaskBody.value.partInfo ||
    createMpcTaskBody.value.partInfo.length === 0
  ) {
    ElMessage.warning('请先选择Agent参与方')
    return false
  }

  const incompletePart = createMpcTaskBody.value.partInfo.find(
    (part) => !part.agentID || !part.fileID,
  )

  if (incompletePart) {
    ElMessage.warning('请为每个Agent参与方选择输入文件')
    return false
  }

  if (taskType.value === 'GARNET_PSI') {
    const primaryKey = psiPrimaryKey.value.trim()

    if (!primaryKey) {
      ElMessage.warning('请输入隐私集合求交的主键')
      return false
    }

    const valid = await validatePsiFile(
      createBody.value.file,
      primaryKey,
    )

    if (!valid) {
      return false
    }
  }

  if (taskType.value === 'GARNET_INFERENCE') {
    const params = createMpcTaskBody.value.compileParameters
    const m = Number(params.m)
    const testSamples = Number(params.test_samples)
    const labelNumber = Number(params.label_number)
    const treeHeight = Number(params.tree_h)

    if (
      !Number.isInteger(m) ||
      m < 2 ||
      !Number.isInteger(testSamples) ||
      testSamples < 1 ||
      !Number.isInteger(labelNumber) ||
      labelNumber < 1 ||
      !Number.isInteger(treeHeight) ||
      treeHeight < 1
    ) {
      ElMessage.warning('请完整配置安全推理编译参数')
      return false
    }

    if (createMpcTaskBody.value.mpcId === 'davex-dt-inference') {
      const actualCount = await countInputValues(createBody.value.file)
      const expectedCount = m * testSamples

      if (actualCount !== expectedCount) {
        ElMessage.error(
          `安全推理输入数量不正确：需要 ${expectedCount} 个数，实际有 ${actualCount} 个`,
        )
        return false
      }
    }
  }

  return true
}

const createMethod = async () => {
  if (creatingTask.value) {
    return
  }

  createMpcTaskBody.value.taskType = taskType.value
  createMpcTaskBody.value.n =
    taskType.value === 'GARNET_MPC' ? partyNumber.value : 2

  if (taskType.value === 'GARNET_PSI') {
    createMpcTaskBody.value.runtimeParameters.PK =
      psiPrimaryKey.value.trim()
  }

  if (!(await validateCreateTask())) {
    return
  }

  creatingTask.value = true

  ElMessage.info({
    message: '任务正在创建，请稍候',
    duration: 2000,
  })

  try {
    const res = await createMpcTask({
      file: createBody.value.file,
      mpcTask: createMpcTaskBody.value,
    })

    const body = res?.data?.body

    if (body?.code === 1) {
      mpcSuccessMessage.value =
        body.message || '任务创建成功，执行完成后将通过消息中心提示'
      mpcSuccessVisible.value = true
      // ElMessage.success('任务创建成功')
    } else {
      mpcFailedMessage.value =
        body?.message || res?.data?.message || '任务创建失败'
      mpcFailedVisible.value = true
      // ElMessage.error(mpcFailedMessage.value)
    }
  } catch (error) {
    const message =
      error?.response?.data?.body?.message ||
      error?.response?.data?.message ||
      error?.response?.data?.error ||
      error?.message ||
      '任务创建请求失败'

    mpcFailedMessage.value = message
    mpcFailedVisible.value = true
    ElMessage.error(`创建失败：${message}`)

    console.error('Failed to create privacy task:', error)
  } finally {
    creatingTask.value = false
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
 if (!file.raw) {
    createBody.value.file = null
    return
  }

  if (file.raw.size === 0) {
    createBody.value.file = null
    upload.value?.clearFiles()
    ElMessage.error('输入文件不能为空')
    return
  }

  createBody.value.file = file.raw
}

const submitUpload = async () => {
  await createMethod()
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
// 即使后端意外返回全部 MPC，页面也只显示当前功能对应的数据。
const filteredMpcList = computed(() =>
  mpcList.value.filter(
    (item) => item.taskType === taskType.value,
  ),
)

const getMpcListMethod = async () => {
  // 查询前清空旧列表，避免切换功能时短暂显示错误的 MPC。
  mpcList.value = []

  try {
    // 只查询当前隐私计算功能对应的 MPC 文件。
    const res = await getMpcList(taskType.value)

    if (res.data.body?.code !== 1) {
      ElMessage.error(res.data.body?.message || '查询MPC文件失败')
      return
    }

    mpcList.value = res.data.body.data || []
  } catch (error) {
    // 向用户显示查询失败，而不是只写入控制台。
    const message =
      error.response?.data?.body?.message ||
      error.response?.data?.message ||
      error.message ||
      '查询MPC文件失败'

    ElMessage.error(message)
  }
}

const mpcFileName = ref('')
const chooseMpcMethod = async (row) => {
  try {
    createMpcTaskBody.value.compileParameters = {}
    createMpcTaskBody.value.runtimeParameters = {}

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
  if (!createMpcTaskBody.value.mpcId) {
    ElMessage.warning('请先选择MPC文件')
    return
  }

  const mpc = mpcList.value.find(
    (item) => item.uid === createMpcTaskBody.value.mpcId,
  )

  if (!mpc) {
    ElMessage.error('找不到已选择的MPC文件')
    return
  }

  // 优先显示用户已经保存的值；没有保存时才使用默认值。
  currentCompileParameters.value = mpc.compileParameters.map(
    (param) => ({
      ...param,
      value:
        createMpcTaskBody.value.compileParameters[param.name] ??
        param.limit?.defaultValue ??
        '',
    }),
  )

  editCompileVisible.value = true
}

const editRuntimeDialog = () => {
  if (!createMpcTaskBody.value.mpcId) {
    ElMessage.warning('请先选择MPC文件')
    return
  }

  const mpc = mpcList.value.find(
    (item) => item.uid === createMpcTaskBody.value.mpcId,
  )

  if (!mpc) {
    ElMessage.error('找不到已选择的MPC文件')
    return
  }

  // 优先显示已经保存到任务对象中的运行参数。
  currentRuntimeParameters.value = mpc.runtimeParameters.map(
    (param) => ({
      ...param,
      value:
        createMpcTaskBody.value.runtimeParameters[param.name] ??
        param.limit?.defaultValue ??
        '',
    }),
  )

  editRuntimeVisible.value = true
}

const saveCompileParameters = () => {
  for (const param of currentCompileParameters.value) {
    const value =
      typeof param.value === 'string'
        ? param.value.trim()
        : param.value

    if (
      param.required &&
      (value === '' || value === null || value === undefined)
    ) {
      ElMessage.warning(`编译参数“${param.name}”不能为空`)
      return
    }

    if (param.limitType === 'NUM') {
      const numberValue = Number(value)

      if (!Number.isFinite(numberValue)) {
        ElMessage.warning(`编译参数“${param.name}”必须是数字`)
        return
      }

      createMpcTaskBody.value.compileParameters[param.name] =
        numberValue
    } else {
      createMpcTaskBody.value.compileParameters[param.name] =
        value
    }
  }

  editCompileVisible.value = false
  ElMessage.success('编译参数已保存')
}

const saveRuntimeParameters = () => {
  for (const param of currentRuntimeParameters.value) {
    const value =
      typeof param.value === 'string'
        ? param.value.trim()
        : param.value

    if (
      param.required &&
      (value === '' || value === null || value === undefined)
    ) {
      ElMessage.warning(`运行参数“${param.name}”不能为空`)
      return
    }

    createMpcTaskBody.value.runtimeParameters[param.name] = value
  }

  editRuntimeVisible.value = false
  ElMessage.success('运行参数已保存')
}

const creatingTask = ref(false)
</script>

<style scoped>
.privacy-task-container {
  display: block;
  width: 100%;
}

.privacy-task-form {
  width: 100%;
  max-width: 1250px;
}

.privacy-task-form :deep(.el-alert) {
  width: 100%;
}

.psi-primary-key-form {
  margin-bottom: 10px;
}
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

/* MPC 文件表格铺满内容区，窄屏时允许横向滚动。 */
.mpc-table-wrapper {
  width: 100%;
  overflow-x: auto;
}

/* 三个 MPC 操作按钮横向居中排列。 */
.mpc-operation-actions {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  white-space: nowrap;
}

/* 使用 gap 统一控制按钮间距。 */
.mpc-operation-actions .el-button + .el-button {
  margin-left: 0;
}
</style>