<template>
  <div>
    <div>
      <el-header class="custom-header">
        <div class="icon-text">
          <el-icon><Folder /></el-icon>
          <span>MPC文件列表</span>
        </div>
      </el-header>
      <el-table
          :data="mpcFileList"
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
        <el-table-column label="操作" min-width="100" align="center">
          <template #default="scope">
            <el-button
                class="small-default-button"
                @click="showParameters(scope.row, 'runtime')"
            >
              查看运行参数
            </el-button>
            <el-button
                class="small-default-button"
                @click="showParameters(scope.row, 'compile')"
            >
              查看编译参数
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-header class="custom-header">
      <div class="icon-text">
        <el-icon><Folder /></el-icon>
        <span>上传MPC文件</span>
      </div>
    </el-header>
    <div class="form-container" style="margin-top: 10px">
      <el-form
          :model="mpcTaskInfo"
          label-width="auto"
          style="max-width: 100%"
          class="styled-form"
      >
        <el-form-item label="MPC文件名">
          <el-input v-model="mpcTaskInfo.name" />
        </el-form-item>
        <el-form-item>
          <el-button class="default-button" @click="addcompileRarameter">增加编译参数</el-button>

          <el-button class="default-button" @click="addruntimeRarameter">增加运行参数</el-button>
        </el-form-item>
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
            <el-button class="default-button" style="margin-right: 10px;">选择MPC文件</el-button>
          </template>
          <el-button class="start-button" @click="submitUpload">
            上传MPC文件
          </el-button>
          <template #tip>
            <div class="el-upload__tip text-red">
              限制1个文件，新文件将覆盖旧文件
            </div>
          </template>
        </el-upload>
      </el-form>
    </div>

    <div class="form-container">
      <el-form label-width="auto" style="max-width: 100%" class="styled-form">
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
              <el-select
                  v-model="scope.row.limitType"
                  placeholder="设置参数内容"
              >
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
              <el-button class="default-button" @click="handleConfig(scope.row, 0)">
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
                <el-option label="选项参数" value="FLAG" />
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
                  placeholder="输入选项参数"
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
          <el-table-column label="操作" min-width="100" align="center">
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
      </el-form>
    </div>

    <div class="form-container">
      <el-form label-width="auto" style="max-width: 100%" class="styled-form">
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
              <el-select
                  v-model="scope.row.limitType"
                  placeholder="设置参数内容"
              >
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
              <el-button class="default-button" @click="handleConfig(scope.row, 1)">
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
                <el-option label="选项参数" value="FLAG" />
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
                  placeholder="输入选项参数"
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
          <el-table-column label="操作" min-width="100" align="center">
            <template #default="scope">
              <el-button
                  type="danger"
                  @click="removeRuntimeParameter(scope.$index)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-form>
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
  <!-- 增加一个对话框，显示提示信息 -->
  <el-dialog v-model="infoDialogVisible" title="提示信息" width="30%">
    <span>{{ infoDialogText }}</span>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="infoDialogVisible = false">关闭</el-button>
      </span>
    </template>
  </el-dialog>

  <el-dialog v-model="mpcSuccessVisible" title="上传完成" width="30%">
    <span>{{ mpcSuccessMessage }}</span>
    <template #footer>
      <div class="dialog-footer">
        <el-button class="close-button" @click="mpcSuccessVisible = false">返回</el-button>
      </div>
    </template>
  </el-dialog>
  <el-dialog v-model="mpcFailedVisible" title="上传失败" width="30%">
    <span>{{ mpcFailedMessage }}</span>
    <template #footer>
      <div class="dialog-footer">
        <el-button class="close-button" @click="mpcFailedVisible = false">返回</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { getMpcList, uploadMpc } from '../../api/mpC.js'
import {genFileId, UploadInstance, UploadProps, UploadRawFile} from "element-plus";
import { addOperationHistory } from '../../api/operationHistory.js'

// 页面加载时只查询 MPC 列表，参数由用户按需添加。
onMounted(() => {
  getMpcListMethod()
})

const mpcSuccessVisible = ref(false)
const mpcFailedVisible = ref(false)
const mpcSuccessMessage = ref('')
const mpcFailedMessage = ref('')

const upload = ref<UploadInstance>()
const handleExceed: UploadProps['onExceed'] = (files) => {
  upload.value!.clearFiles()
  const file = files[0] as UploadRawFile
  file.uid = genFileId()
  upload.value!.handleStart(file)
}

const handleFileChange: UploadProps['onChange'] = (file) => {
  uploadBody.value.file = file.raw

  if (!mpcTaskInfo.value.name.trim()) {
    mpcTaskInfo.value.name = file.name.replace(/\.[^.]+$/, '')
  }
}

const submitUpload = () => {
  uploadMethod()
  console.log(uploadBody.value)
}

const mpcTaskInfo = ref({
  centerId: "DAVEX-C1",
  name: '',
  compileParameters: [],
  runtimeParameters: [],
})
const uploadBody = ref({
  file: null as File | null,
  mpc: mpcTaskInfo.value
})
const uploadMethod = async () => {
  try {
     // 上传前检查必要字段，避免生成名称为空的数据库记录。
    if (!uploadBody.value.file) {
      mpcFailedMessage.value = '请先选择 MPC 文件'
      mpcFailedVisible.value = true
      return
    }

    const name = mpcTaskInfo.value.name.trim()
    if (!name) {
      mpcFailedMessage.value = 'MPC 文件名不能为空'
      mpcFailedVisible.value = true
      return
    }

    // 不发送页面中尚未配置类型的空参数，避免后端枚举解析失败。
    const mpc = {
      ...mpcTaskInfo.value,
      name,
      compileParameters: mpcTaskInfo.value.compileParameters.filter(
        (parameter) => parameter.parameterType,
      ),
      runtimeParameters: mpcTaskInfo.value.runtimeParameters.filter(
        (parameter) => parameter.parameterType,
      ),
    }
    const res = await uploadMpc({
      file: uploadBody.value.file,
      mpc,
    })
    console.log(res.data)
    if (res.data.body.code == 1) {
      mpcSuccessMessage.value = `MPC文件上传成功`
      mpcSuccessVisible.value = true
      getMpcListMethod()
      // 记录操作历史
      await addOperationHistory({
        operationType: '上传MPC文件',
        operationObject: mpcTaskInfo.value.name,
        result: '成功',
        remark: `上传MPC程序 ${mpcTaskInfo.value.name}`
      })
    }
    else {
      // 后端业务消息位于响应的 body 中。
      mpcFailedMessage.value = res.data.body?.message || 'MPC 文件上传失败'
      mpcFailedVisible.value = true
      // 记录失败操作
      await addOperationHistory({
        operationType: '上传MPC文件',
        operationObject: mpcTaskInfo.value.name,
        result: '失败',
        remark: res.data.message || '上传失败'
      })
    }
  }
  catch (error) {
    console.error('Failed to create MPC task:', error)
    // 记录失败操作
    await addOperationHistory({
      operationType: '上传MPC文件',
      operationObject: mpcTaskInfo.value.name,
      result: '失败',
      remark: error.message || '上传MPC文件失败'
    })
  }
}

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

const removeRuntimeParameter = (index) => {
  mpcTaskInfo.value.runtimeParameters.splice(index, 1)
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
  let posIndex1 = 0
  mpcTaskInfo.value.compileParameters.forEach((param) => {
    if (param.parameterType === 'POS') {
      param.posORflag = posIndex1++
    }
  })
  let posIndex2 = 0
  mpcTaskInfo.value.runtimeParameters.forEach((param) => {
    if (param.parameterType === 'POS') {
      param.posORflag = posIndex2++
    }
  })
}

//参数限制
const selectedRow = ref(null)
const dialogVisible = ref(false)
const configType = ref()

const handleConfig = (row, type) => {
  configType.value = type
  selectedRow.value = { ...createParameter() }
  Object.assign(selectedRow.value, row) // 保留已有的属性值
  // selectedRow.value = JSON.parse(JSON.stringify(createParameter()))
  // Object.assign(selectedRow.value, JSON.parse(JSON.stringify(row)))
  dialogVisible.value = true
}
// 保存配置时，确保selectedRow.limit具有正确的结构
const saveConfig = () => {
  if (configType.value == 0) {
    if (!selectedRow.value || !mpcTaskInfo.value.compileParameters) return

    const index = mpcTaskInfo.value.compileParameters.findIndex(
        (p) => p.name === selectedRow.value.name,
    ) // 假设每行数据有一个唯一的id
    console.log(index)
    if (index !== -1) {
      mpcTaskInfo.value.compileParameters[index] = { ...selectedRow.value }
    }
    dialogVisible.value = false
  }
  else {
    if (!selectedRow.value || !mpcTaskInfo.value.runtimeParameters) return

    const index = mpcTaskInfo.value.runtimeParameters.findIndex(
        (p) => p.name === selectedRow.value.name,
    ) // 假设每行数据有一个唯一的id
    console.log(index)
    if (index !== -1) {
      mpcTaskInfo.value.runtimeParameters[index] = { ...selectedRow.value }
    }
    dialogVisible.value = false
  }
}

const addEnumValue = () => {
  if (!selectedRow.value.limit.values) {
    selectedRow.value.limit.values = []
  }
  selectedRow.value.limit.values.push('')
}

//查看MPC列表
const mpcFileList = ref([])
const getMpcListMethod = async () => {
  try {
    const res = await getMpcList()
    mpcFileList.value = res.data.body.data
    console.log(mpcFileList.value)
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
//查看MPC列表
// 信息提示框
const infoDialogVisible = ref(false)
const infoDialogText = ref('')
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
