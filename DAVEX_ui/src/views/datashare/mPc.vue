<template>
  <div>
    <el-upload ref="photoRef" :auto-upload="false" :http-request="upload">
      <template #trigger>
        <el-button type="primary">选择文件</el-button>
      </template>

      <el-button class="ml-3" type="success" @click="submitUpload">
        上传文件
      </el-button>
    </el-upload>
    <el-form :model="mpcTaskInfo" label-width="auto" style="max-width: 600px">
      <el-form-item label="mpc任务名">
        <el-input v-model="mpcTaskInfo.name" />
      </el-form-item>
      <el-button @click="console.log(mpcTaskInfo)">测试编译参数</el-button>
      <el-button @click="addcompileRarameter">增加编译参数</el-button>
      <el-button @click="addruntimeRarameter">增加运行参数</el-button>
    </el-form>
  </div>
  <div>
    <el-table
      :data="mpcTaskInfo.compileParameters"
      style="width: 100%; margin-top: 20px"
      stripe
      border
      height="300px"
      max-height="300px"
    >
      <el-table-column
        type="index"
        label="序号"
        width="100"
        align="center"
      ></el-table-column>
      <!-- 列：编译参数名 -->
      <el-table-column
        prop="name"
        label="编译参数名"
        width="150"
        align="center"
      >
        <template #default="scope">
          <el-input v-model="scope.row.name" />
        </template>
      </el-table-column>
      <el-table-column
        prop="limitType"
        label="参数限制"
        width="150"
        align="center"
      >
        <template #default="scope">
          <el-select v-model="scope.row.limitType" placeholder="设置参数内容">
            <el-option label="NUM" value="NUM" />
            <el-option label="STRING" value="STRING" />
            <el-option label="ENUM" value="ENUM" />
          </el-select>
        </template>
      </el-table-column>

      <el-table-column
        prop="limitType"
        label="参数限制"
        width="150"
        align="center"
      >
        <template #default="scope">
          <el-button type="primary" @click="handleConfig(scope.row)">
            配置参数限制
          </el-button>
        </template>
      </el-table-column>
      <!-- 列：参数类型 -->

      <!-- 列：禁止为空 -->
      <el-table-column
        prop="required"
        label="禁止为空"
        width="100"
        align="center"
      >
        <template #default="scope">
          <el-switch v-model="scope.row.required" />
        </template>
      </el-table-column>

      <!-- 列：参数类型 -->
      <el-table-column
        prop="parameterType"
        label="参数类型"
        width="150"
        align="center"
      >
        <template #default="scope">
          <el-select
            v-model="scope.row.parameterType"
            placeholder="设置参数类型"
            @change="() => handleParameterTypeChange(scope.row)"
          >
            <el-option label="位置参数" value="POS" />
            <el-option label="FLAG参数" value="FLAG" />
          </el-select>
        </template>
      </el-table-column>

      <!-- 列：FLAG参数 -->
      <el-table-column
        prop="posORflag"
        label="参数详情"
        width="200"
        align="center"
      >
        <template #default="scope">
          <span v-if="scope.row.parameterType === 'POS'">
            当前是第 {{ scope.row.posORflag }} 位
          </span>
          <el-input
            v-else
            v-model="scope.row.posORflag"
            placeholder="输入FLAG参数"
          />
        </template>
      </el-table-column>

      <!-- 列：参数描述 -->
      <el-table-column
        prop="description"
        label="参数描述"
        width="300"
        align="center"
      >
        <template #default="scope">
          <el-input v-model="scope.row.description" type="textarea" />
        </template>
      </el-table-column>

      <!-- 操作列：删除按钮 -->
      <el-table-column label="操作" mid-width="100" align="center">
        <template #default="scope">
          <el-button
            type="danger"
            @click="removeCompileParameter(scope.$index)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-table
      :data="mpcTaskInfo.runtimeParameters"
      style="width: 100%; margin-top: 20px"
      stripe
      border
      height="300px"
      max-height="300px"
    >
      <el-table-column
        type="index"
        label="序号"
        width="100"
        align="center"
      ></el-table-column>
      <!-- 列：编译参数名 -->
      <el-table-column
        prop="name"
        label="运行参数名"
        width="150"
        align="center"
      >
        <template #default="scope">
          <el-input v-model="scope.row.name" />
        </template>
      </el-table-column>
      <el-table-column
        prop="limitType"
        label="参数限制"
        width="150"
        align="center"
      >
        <template #default="scope">
          <el-select v-model="scope.row.limitType" placeholder="设置参数内容">
            <el-option label="NUM" value="NUM" />
            <el-option label="STRING" value="STRING" />
            <el-option label="ENUM" value="ENUM" />
          </el-select>
        </template>
      </el-table-column>

      <el-table-column
        prop="limitType"
        label="参数限制"
        width="150"
        align="center"
      >
        <template #default="scope">
          <el-button type="primary" @click="handleConfig(scope.row)">
            配置参数限制
          </el-button>
        </template>
      </el-table-column>
      <!-- 列：参数类型 -->

      <!-- 列：禁止为空 -->
      <el-table-column
        prop="required"
        label="禁止为空"
        width="100"
        align="center"
      >
        <template #default="scope">
          <el-switch v-model="scope.row.required" />
        </template>
      </el-table-column>

      <!-- 列：参数类型 -->
      <el-table-column
        prop="parameterType"
        label="参数类型"
        width="150"
        align="center"
      >
        <template #default="scope">
          <el-select
            v-model="scope.row.parameterType"
            placeholder="设置参数类型"
            @change="() => handleParameterTypeChange(scope.row)"
          >
            <el-option label="位置参数" value="POS" />
            <el-option label="FLAG参数" value="FLAG" />
          </el-select>
        </template>
      </el-table-column>

      <!-- 列：FLAG参数 -->
      <el-table-column
        prop="posORflag"
        label="参数详情"
        width="200"
        align="center"
      >
        <template #default="scope">
          <span v-if="scope.row.parameterType === 'POS'">
            当前是第 {{ scope.row.posORflag }} 位
          </span>
          <el-input
            v-else
            v-model="scope.row.posORflag"
            placeholder="输入FLAG参数"
          />
        </template>
      </el-table-column>

      <!-- 列：参数描述 -->
      <el-table-column
        prop="description"
        label="参数描述"
        width="300"
        align="center"
      >
        <template #default="scope">
          <el-input v-model="scope.row.description" type="textarea" />
        </template>
      </el-table-column>

      <!-- 操作列：删除按钮 -->
      <el-table-column label="操作" mid-width="100" align="center">
        <template #default="scope">
          <el-button
            type="danger"
            @click="removeCompileParameter(scope.$index)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-table
      :data="mpcTaskList"
      style="width: 100%; margin-top: 20px"
      stripe
      border
      height="300px"
      max-height="300px"
    >
      <el-table-column
        type="index"
        label="序号"
        width="100"
        align="center"
      ></el-table-column>
      <!-- 列：编译参数名 -->
      <el-table-column
        prop="name"
        label="程序名称"
        width="150"
        align="center"
      ></el-table-column>
      <!-- 操作列：删除按钮 -->
      <el-table-column label="操作" mid-width="100" align="center">
        <template #default="scope">
          <el-button
            type="primary"
            @click="showParameters(scope.row, 'runtime')"
          >
            查看运行参数
          </el-button>
          <el-button
            type="success"
            @click="showParameters(scope.row, 'compile')"
          >
            查看编译参数
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="form-container">
      <el-form
        :model="creatMpcTaskBody"
        style="max-width: 600px"
        class="styled-form"
      >
        <el-form-item label="AgentID">
          <el-input v-model="creatMpcTaskBody.partInfo[0].agentID"></el-input>
        </el-form-item>
        <el-form-item label="文件ID">
          <el-input v-model="creatMpcTaskBody.partInfo[0].fileID"></el-input>
        </el-form-item>
        <el-form-item label="选择任务">
          <el-select v-model="creatMpcTaskBody.mpcId" placeholder="请选择任务">
            <el-option
              v-for="task in mpcTaskList"
              :key="task.uid"
              :label="task.name"
              :value="task.uid"
            ></el-option>
          </el-select>
        </el-form-item>
        <!-- 配置编译参数按钮 -->
        <el-form-item>
          <el-button type="primary" @click="showCompileDialog">
            配置编译参数
          </el-button>
        </el-form-item>
        <!-- 配置运行参数按钮 -->
        <el-form-item>
          <el-button type="success" @click="showRuntimeDialog">
            配置运行参数
          </el-button>
        </el-form-item>
        <div>
          <el-upload
            ref="mpcTaskRef"
            :auto-upload="false"
            :http-request="uploadMpcTask"
          >
            <template #trigger>
              <el-button type="primary">选择文件</el-button>
            </template>

            <el-button class="ml-3" type="success" @click="submitMpcTask">
              创建隐私保护任务
            </el-button>
          </el-upload>
        </div>
      </el-form>
      <!-- 配置编译参数的弹窗 -->
      <el-dialog
        title="配置编译参数"
        v-model="compileDialogVisible"
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
          <el-button type="primary" @click="saveCompileParameters">
            保存
          </el-button>
          <el-button @click="compileDialogVisible = false">取消</el-button>
        </template>
      </el-dialog>

      <!-- 配置运行参数的弹窗 -->
      <el-dialog
        title="配置运行参数"
        v-model="runtimeDialogVisible"
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
          <el-button type="primary" @click="saveRuntimeParameters">
            保存
          </el-button>
          <el-button @click="runtimeDialogVisible = false">取消</el-button>
        </template>
      </el-dialog>
    </div>
    <el-dialog
      v-model="dialogVisible"
      title="配置参数"
      width="30%"
      :before-close="handleClose"
    >
      <div v-if="selectedRow">
        <template v-if="selectedRow.limitType === 'NUM'">
          <el-input
            v-model.number="selectedRow.limit.min"
            label="最小值"
            placeholder="请输入最小值"
          />
          <el-input
            v-model.number="selectedRow.limit.max"
            label="最大值"
            placeholder="请输入最大值"
          />
          <el-input
            v-model.number="selectedRow.limit.defaultValue"
            label="默认值"
            placeholder="请输入默认值"
          />
        </template>
        <template v-if="selectedRow.limitType === 'STRING'">
          <el-input
            v-model="selectedRow.limit.defaultValue"
            label="默认值"
            placeholder="请输入默认值"
          />
        </template>
        <template v-if="selectedRow.limitType === 'ENUM'">
          <div v-for="(value, index) in selectedRow.limit.values" :key="index">
            <el-input
              v-model="selectedRow.limit.values[index]"
              placeholder="请输入枚举值"
            />
          </div>
          <el-input
            v-model="selectedRow.limit.defaultValue"
            label="默认枚举值"
            placeholder="请输入默认枚举值"
          />
          <el-button type="primary" @click="addEnumValue">添加枚举值</el-button>
        </template>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveConfig">保存</el-button>
        </span>
      </template>
      <!-- ... dialog footer ... -->
    </el-dialog>
  </div>

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
      <!-- 其他列可以根据需要添加 -->
    </el-table>
    <template #footer>
      <el-button @click="parameterDialogVisible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted } from 'vue'
import { reactive } from 'vue'
import { getMpcList } from '../../api/mpC.js'

onMounted(() => {
  addcompileRarameter()
  getMpcListMethod()
})

function createParameter() {
  return {
    name: '',
    limit: {},
    required: true,
    limitType: '',
    posORflag: '',
    description: '',
    parameterType: '',
  }
}

const compileParameter = reactive({
  name: '',
  limit: {},
  required: true,
  auto: false,
  limitType: '',
  posORflag: 0,
  description: '',
  parameterType: '',
})

const runtimeParameter = reactive({
  name: '',
  limit: {},
  required: true,
  auto: false,
  limitType: '',
  posORflag: 0,
  description: '',
  parameterType: '',
})

const mpcTaskInfo = ref({
  centerId: 1,
  name: '',
  compileParameters: [],
  runtimeParameters: [],
})

const addcompileRarameter = () => {
  mpcTaskInfo.value.compileParameters.push(createParameter())
  console.log(mpcTaskInfo.value)
}
const addruntimeRarameter = () => {
  mpcTaskInfo.value.runtimeParameters.push(createParameter())
}

const removeCompileParameter = (index) => {
  mpcTaskInfo.value.compileParameters.splice(index, 1)
  updatePosParameters() // 删除后更新位置参数
}

const handleParameterTypeChange = (row) => {
  if (row.parameterType === 'POS') {
    updatePosParameters() // 当选择位置参数时，更新位置参数的值
  } else if (row.parameterType === 'FLAG') {
    updatePosParameters()
    row.posORflag = '' // 允许手动输入 FLAG 参数
  }
}

// 更新所有位置参数的自增值
const updatePosParameters = () => {
  let posIndex = 0
  mpcTaskInfo.value.compileParameters.forEach((param) => {
    if (param.parameterType === 'POS') {
      param.posORflag = posIndex++
    }
  })
}

//参数限制
const selectedRow = ref(null)
const dialogVisible = ref(false)

const handleConfig = (row) => {
  selectedRow.value = { ...createParameter() }
  Object.assign(selectedRow.value, row) // 保留已有的属性值
  dialogVisible.value = true
}
// 保存配置时，确保selectedRow.limit具有正确的结构
const saveConfig = () => {
  if (!selectedRow.value || !mpcTaskInfo.value.compileParameters) return

  const index = mpcTaskInfo.value.compileParameters.findIndex(
    (p) => p.id === selectedRow.value.id,
  ) // 假设每行数据有一个唯一的id
  if (index !== -1) {
    mpcTaskInfo.value.compileParameters[index] = { ...selectedRow.value }
  }
  dialogVisible.value = false
}

const addEnumValue = () => {
  if (!selectedRow.value.limit.values) {
    selectedRow.value.limit.values = []
  }
  selectedRow.value.limit.values.push('')
}
//参数限制
//上传文件
import axios from 'axios'
let photoRef = ref()

function upload(params) {
  let formData = new FormData()
  formData.append('file', params.file)
  // 使用 Blob 指定 mpcTask 的 MIME 类型为 application/json
  const mpcTaskJson = JSON.stringify(mpcTaskInfo.value)
  const mpcTaskBlob = new Blob([mpcTaskJson], { type: 'application/json' })
  formData.append('mpc', mpcTaskBlob) // 添加 mpcTask，指定类型
  axios({
    url: 'http://10.176.34.171:9999/Mpc/create',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data', // 使用 multipart/form-data
      Accept: '*/*', // 接受所有响应类型
    },
  }).then((resp) => {
    console.log('success')
  })
  // createPsiTask(formData.value)
}
function submitUpload() {
  photoRef.value.submit()
}
//上传文件
//创建隐私计算任务相关代码
const creatMpcTaskBody = ref({
  partInfo: [
    {
      agentID: 5, //需要填写
      part: 1, //默认
      fileID: 25, //需要填写
    },
  ],
  applicationId: 6, //后台配置
  centerId: 1, //后台配置
  compileParameters: {
    party_number: 2,
    feature_number: 5,
    ents_tree_h: 4,
    ents_n_threads: 4,
    sample_number_from_party_0: 50,
    sample_number_from_party_1: 49,
  },
  host: '10.176.34.171', //后台配置
  mpcId: '3fbd4c4945ff5260dd5a7513a4e982f3', //后台配置
  mpcName: 'name',
  n: 2, //目前只需要2方
  part: 0, //发起方默认为第0方
  port: 6000, //后台配置 无需用户在前端选择端口
  runtimeParameters: {
    protocol: 'semi2k-with-conversion-party', //目前只支持一个协议 但是后续可能会有多个协议
  },
  status: 'INIT', //默认INIT
  taskType: 'GARNET_MPC', //后台配置
})

let mpcTaskRef = ref()

function uploadMpcTask(params) {
  let formData = new FormData()
  formData.append('file', params.file)
  // 使用 Blob 指定 mpcTask 的 MIME 类型为 application/json
  const mpcTaskJson = JSON.stringify(creatMpcTaskBody.value)
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
function submitMpcTask() {
  mpcTaskRef.value.submit()
  console.log(creatMpcTaskBody.value)
}
//创建隐私计算任务相关代码
//查看MPC列表
const mpcTaskList = ref([])
const getMpcListMethod = async () => {
  try {
    const res = await getMpcList()
    mpcTaskList.value = res.data.body.data
    console.log(mpcTaskList.value)
  } catch (error) {
    console.error('Failed to get mpc list:', error)
  }
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
const parameterDialogVisible = ref(false)
const currentParameters = ref([])
const parameterDialogTitle = ref('')
const formatLimit = (limit) => {
  return Object.entries(limit)
    .map(([key, value]) => `${key}: ${value}`)
    .join(' ')
}
const compileDialogVisible = ref(false)
const runtimeDialogVisible = ref(false)
const currentCompileParameters = ref([])
const currentRuntimeParameters = ref([])
const showCompileDialog = () => {
  const task = mpcTaskList.value.find(
    (t) => t.uid === creatMpcTaskBody.value.mpcId,
  )
  if (task) {
    currentCompileParameters.value = task.compileParameters.map((param) => ({
      ...param,
      value: param.limit.defaultValue || '',
    }))
    compileDialogVisible.value = true
  }
}

const showRuntimeDialog = () => {
  const task = mpcTaskList.value.find(
    (t) => t.uid === creatMpcTaskBody.value.mpcId,
  )
  if (task) {
    currentRuntimeParameters.value = task.runtimeParameters.map((param) => ({
      ...param,
      value: param.limit.defaultValue || '',
    }))
  }
  runtimeDialogVisible.value = true
}
const saveCompileParameters = () => {
  currentCompileParameters.value.forEach((param) => {
    // 检查参数值是否为数字，如果是则转换为数字
    if (!isNaN(param.value) && param.limitType === 'NUM') {
      creatMpcTaskBody.value.compileParameters[param.name] = Number(param.value)
    } else {
      // 对于其他类型（如字符串），直接存储
      creatMpcTaskBody.value.compileParameters[param.name] = param.value
    }
  })
  // currentCompileParameters.value.forEach((param) => {
  //   creatMpcTaskBody.value.compileParameters[param.name] = param.value
  // })

  compileDialogVisible.value = false
}

const saveRuntimeParameters = () => {
  currentRuntimeParameters.value.forEach((param) => {
    creatMpcTaskBody.value.runtimeParameters[param.name] = param.value
  })
  runtimeDialogVisible.value = false
}
//查看MPC列表
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
