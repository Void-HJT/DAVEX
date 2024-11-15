<template>
  <el-container>
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
              <!-- 根据 scope.row.type 的值来决定显示哪个图标 -->
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
                  v-if="scope.row.type === 'file' && scope.row.name.endsWith('.csv')"
                  link
                  type="primary"
                  @click="
                  getTableHeaderMethod(
                    scope.row.agentId,
                    scope.row.uid,
                    scope.row.parentId,
                    scope.row.name
                  )
                "
                  size="small"
              >
                查看表头信息
              </el-button>
              <el-button
                  v-if="scope.row.type === 'file' && scope.row.name.endsWith('.txt')"
                  link
                  type="primary"
                  @click="
                  getTXTExampleMethod(
                    scope.row.agentId,
                    scope.row.uid,
                    scope.row.parentId,
                    scope.row.name
                  )
                "
                  size="small"
              >
                查看文件示例
              </el-button>
<!--              <el-button-->
<!--                  v-if="scope.row.type === 'file' && scope.row.name.endsWith('.csv') && !isAccessible(scope.row.ruleList)"-->
<!--                  link-->
<!--                  type="danger"-->
<!--                  size="small"-->
<!--                  disabled-->
<!--              >-->
<!--                无权比对-->
<!--              </el-button>-->
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-main>
  </el-container>

  <el-container v-if="getTableHeaderBody.fileId">
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
          文件表头信息
        </p>
      </div>
    </el-header>
    <el-main>
      <el-descriptions
          class="margin-top"
          :title="'当前选择文件：' + fileName"
          :column="3"
          :size="'default'"
          border
      >
        <el-descriptions-item
            v-for="(header, index) in tableHeaders"
            :key="index"
        >
          <template #label>
            <div class="cell-item">
              {{ header.name }}
            </div>
          </template>
          {{ header.example }}
        </el-descriptions-item>
      </el-descriptions>

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
          <el-button type="primary" style="margin-right: 10px;">上传csv文件</el-button>
        </template>
        <el-button class="ml-3" type="success" @click="submitUpload" style="margin-right: 10px;">
          比对
        </el-button>
        <template #tip>
          <div class="el-upload__tip text-red">
            limit 1 file, new file will cover the old file
          </div>
        </template>
      </el-upload>

      <el-button type="primary" @click="selectAttributesVisible = true">输入数据进行比对</el-button>
    </el-main>
  </el-container>

  <el-dialog v-model="selectAttributesVisible" title="选择属性" width="30%">
    <el-checkbox-group v-model="selectedAttributes" style="display: flex; flex-wrap: wrap;">
      <el-checkbox
          v-for="(header, index) in tableHeaders"
          :label="header.name"
          :key="index"
          style="margin-bottom: 10px;"
      >
        {{ header.name }}
      </el-checkbox>
    </el-checkbox-group>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="selectAttributesVisible = false" style="margin-right: 10px;">返回</el-button>
        <el-button type="primary" @click="confirmAttributes">
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>

  <el-dialog v-model="inputDataVisible" title="输入数据" width="30%">
    <div v-for="(rowData, rowIndex) in inputData"
         :key="rowIndex"
         class="input-row data-row"
         style="margin-bottom: 10px; padding: 10px; border: 2px solid #dcdfe6; border-radius: 4px;">
      <div v-for="(attribute, index) in selectedAttributes" :key="index" class="input-item" style="margin-bottom: 10px">
        <el-form-item :label="attribute">
          <el-input v-model="inputData[rowIndex][index]" placeholder="请输入数据"></el-input>
        </el-form-item>
      </div>
      <el-button
          type="danger"
          size="small"
          @click="removeDataRow(rowIndex)"
          v-if="inputData.length > 1"
      >
        删除数据
      </el-button>
    </div>
    <el-button type="primary" size="small" @click="addDataRow" style="margin-bottom: 10px;">
      添加数据
    </el-button>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="resetAttributes" style="margin-right: 10px;">重新选择属性</el-button>
        <el-button type="primary" @click="compareData">
          比对
        </el-button>
      </div>
    </template>
  </el-dialog>

  <el-dialog v-model="compareSuccessVisible" title="比对完成" width="30%">
    <span>{{ compareSuccessMessage }}</span>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="compareSuccessVisible = false" style="margin-right: 10px;">返回</el-button>
        <router-link to="/result/comPare">
          <el-button type="primary">
            查看结果管理区
          </el-button>
        </router-link>
      </div>
    </template>
  </el-dialog>
  <el-dialog v-model="compareFailedVisible" title="比对失败" width="30%">
    <span>{{ compareFailedMessage }}</span>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="compareFailedVisible = false">返回</el-button>
      </div>
    </template>
  </el-dialog>

  <el-container v-if="getTXTExampleBody.fileId">
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
          文本文件示例
        </p>
      </div>
    </el-header>
    <el-main>
      <el-descriptions
          class="margin-top"
          :title="'当前选择文件：' + fileName"
          :column="1"
          :size="'default'"
          border
      >
        <el-descriptions-item
            label="数据行示例"
            label-width="50px"
        style="display: flex; justify-content: center; align-items: center;"
        >
          {{ txtExample.join(' ') }}
        </el-descriptions-item>
      </el-descriptions>

      <el-upload
          ref="upload"
          class="upload-demo"
          action="/"
          :limit="1"
          :on-exceed="handleExceed"
          :auto-upload="false"
          :on-change="handleFileChangeTXT"
          style="margin-top: 20px; margin-bottom: 20px;"
      >
        <template #trigger>
          <el-button type="primary" style="margin-right: 10px;">上传txt文件</el-button>
        </template>
        <el-button class="ml-3" type="success" @click="submitUploadTXT" style="margin-right: 10px;">
          比对
        </el-button>
        <template #tip>
          <div class="el-upload__tip text-red">
            limit 1 file, new file will cover the old file
          </div>
        </template>
      </el-upload>

      <el-button type="primary" @click="inputTextDataVisible = true">输入数据进行比对</el-button>
    </el-main>
  </el-container>

  <el-dialog v-model="inputTextDataVisible" title="输入数据" width="30%">
    <div v-for="(rowData, rowIndex) in inputTextData"
         :key="rowIndex"
         class="input-row data-row"
         style="margin-bottom: 10px; padding: 10px; border: 2px solid #dcdfe6; border-radius: 4px;">
      <el-form-item :label="'第 ' + rowData.rowNum + ' 行'">
        <el-input v-model="rowData.value" placeholder="请输入数据"></el-input>
      </el-form-item>
      <el-button
          type="danger"
          size="small"
          @click="removeTextDataRow(rowIndex)"
          v-if="inputTextData.length > 1"
      >
        删除数据
      </el-button>
    </div>
    <el-button type="primary" size="small" @click="addTextDataRow" style="margin-bottom: 10px;">
      添加数据
    </el-button>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="compareTXTData">
          比对
        </el-button>
      </div>
    </template>
  </el-dialog>

</template>

<script lang="ts" setup>
import {getAgent} from '../../api/testDve.js'
import {getDirectory, getRootByAgent} from '../../api/folderController.js'
import {getTableHeader, compareFromCsv, compare, getTXTExample, compareFromTXT, compareTXT} from "../../api/comparison.js";
import {onMounted, ref} from "vue";
import {genFileId, UploadInstance, UploadProps, UploadRawFile} from "element-plus";

onMounted(() => {
  getAgentMethod()
  // getDirectoryMethod()
})

// 对话框是否可见
const compareSuccessVisible = ref(false)
const compareFailedVisible = ref(false)
const compareSuccessMessage = ref('');
const compareFailedMessage = ref('');
const selectAttributesVisible = ref(false)
const inputDataVisible = ref(false)
const inputTextDataVisible = ref(false)

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
const getTXTExampleBody = ref({
  agentId: '',
  fileId: '',
  folderId: ''
})
const inputData = ref([[]])
const inputTextData = ref([{ rowNum: 1, value: '' }])
const tableHeaders = ref([])
const txtExample = ref([])
const selectedAttributes = ref([])
const compareFromCsvBody = ref({
  applicationId: applicationId,
  agentId: '',
  fileId: '',
  folderId: '',
  file: null as File | null
})
const compareFromTXTBody = ref({
  applicationId: applicationId,
  agentId: '',
  fileId: '',
  folderId: '',
  file: null as File | null
})
const compareBody = ref({
  applicationId: applicationId,
  agentId: '',
  fileId: '',
  folderId: '',
  attributes: [],
  valuesList: [[]]
})
const compareTXTBody = ref({
  applicationId: applicationId,
  agentId: '',
  fileId: '',
  folderId: '',
  valuesList: [[]]
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
function confirmAttributes() {
  selectAttributesVisible.value = false
  inputDataVisible.value = true
}
function resetAttributes() {
  inputDataVisible.value = false
  selectAttributesVisible.value = true
  selectedAttributes.value = []
  inputData.value = [[]]
}
function addDataRow() {
  inputData.value.push(new Array(selectedAttributes.value.length).fill(''));
}
function removeDataRow(rowIndex) {
  if (inputData.value.length > 1) {
    inputData.value.splice(rowIndex, 1);
  }
}

// 定义添加新行的函数
function addTextDataRow() {
  inputTextData.value.push({
    rowNum: inputTextData.value.length + 1, // 行号从 1 开始，每次加 1
    value: '' // 初始为空字符串
  });
}

// 定义删除指定行的函数
function removeTextDataRow(index) {
  inputTextData.value.splice(index, 1);
  // 删除后更新每行的行号
  inputTextData.value.forEach((item, i) => {
    item.rowNum = i + 1;
  });
}

const compareFromCsvMethod = async () => {
  try {
    const res = await compareFromCsv(compareFromCsvBody.value)
    console.log(res.data)
    if (res.data.code == 1) {
      compareSuccessMessage.value = `比对完成，比对结果文件已存至结果管理区`
      compareSuccessVisible.value = true
    }
    else {
      compareFailedMessage.value = res.data.message
      compareFailedVisible.value = true
    }
  }
  catch (error) {
    console.error('Failed to compare:', error)
  }
}

const compareFromTXTMethod = async () => {
  try {
    const res = await compareFromTXT(compareFromTXTBody.value)
    console.log(res.data)
    if (res.data.code == 1) {
      compareSuccessMessage.value = `比对完成，比对结果文件已存至结果管理区`
      compareSuccessVisible.value = true
    }
    else {
      compareFailedMessage.value = res.data.message
      compareFailedVisible.value = true
    }
  }
  catch (error) {
    console.error('Failed to compare:', error)
  }
}

const compareMethod = async () => {
  try {
    compareBody.value.attributes = selectedAttributes.value
    compareBody.value.valuesList = inputData.value
    console.log(compareBody.value)
    const res = await compare(compareBody.value)
    console.log(res.data)
    if (res.data.code == 1) {
      const booleanArray = res.data.data
      const resultList = booleanArray.map((result, index) => `${index + 1}. ${result ? 'True' : 'False'}`).join('; ')
      compareSuccessMessage.value = `比对结果依次为: ${resultList}`
      compareSuccessVisible.value = true
    }
    else {
      compareFailedMessage.value = res.data.message
      compareFailedVisible.value = true
    }
  }
  catch (error) {
    console.error('Failed to compare:', error)
  }
}

const compareTXTMethod = async () => {
  try {
    compareTXTBody.value.valuesList = inputTextData.value.map(row => row.value.split(' '));
    console.log(compareTXTBody.value)
    const res = await compareTXT(compareTXTBody.value)
    console.log(res.data)
    if (res.data.code == 1) {
      const booleanArray = res.data.data
      const resultList = booleanArray.map((result, index) => `${index + 1}. ${result ? 'True' : 'False'}`).join('; ')
      compareSuccessMessage.value = `比对结果依次为: ${resultList}`
      compareSuccessVisible.value = true
    }
    else {
      compareFailedMessage.value = res.data.message
      compareFailedVisible.value = true
    }
  }
  catch (error) {
    console.error('Failed to compare:', error)
  }
}

const getTableHeaderMethod = async (agentId, fileId, folderId, name) => {
  try {
    getTXTExampleBody.value.agentId = ''
    getTXTExampleBody.value.fileId = ''
    getTXTExampleBody.value.folderId = ''
    compareFromTXTBody.value.agentId = ''
    compareFromTXTBody.value.fileId = ''
    compareFromTXTBody.value.folderId = ''

    getTableHeaderBody.value.agentId = agentId
    getTableHeaderBody.value.fileId = fileId
    getTableHeaderBody.value.folderId = folderId
    compareFromCsvBody.value.agentId = agentId
    compareFromCsvBody.value.fileId = fileId
    compareFromCsvBody.value.folderId = folderId
    compareBody.value.agentId = agentId
    compareBody.value.fileId = fileId
    compareBody.value.folderId = folderId
    fileName.value = name
    const res = await getTableHeader(getTableHeaderBody.value)
    console.log(res.data.data.name)
    console.log(res.data.data.example)
    tableHeaders.value = res.data.data.name.map((name, index) => ({
      name: name,
      example: res.data.data.example[index]
    }))
  }
  catch (error) {
    console.error('Failed to get table header:', error)
  }
}

const getTXTExampleMethod = async (agentId, fileId, folderId, name) => {
  try {
    getTableHeaderBody.value.agentId = ''
    getTableHeaderBody.value.fileId = ''
    getTableHeaderBody.value.folderId = ''
    compareFromCsvBody.value.agentId = ''
    compareFromCsvBody.value.fileId = ''
    compareFromCsvBody.value.folderId = ''

    getTXTExampleBody.value.agentId = agentId
    getTXTExampleBody.value.fileId = fileId
    getTXTExampleBody.value.folderId = folderId
    compareFromTXTBody.value.agentId = agentId
    compareFromTXTBody.value.fileId = fileId
    compareFromTXTBody.value.folderId = folderId
    compareTXTBody.value.agentId = agentId
    compareTXTBody.value.fileId = fileId
    compareTXTBody.value.folderId = folderId
    fileName.value = name
    const res = await getTXTExample(getTXTExampleBody.value)
    console.log(res.data.data)
    txtExample.value = res.data.data
  }
  catch (error) {
    console.error('Failed to get text file content:', error)
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
  compareFromCsvBody.value.file = file.raw
}

const handleFileChangeTXT: UploadProps['onChange'] = (file, fileList) => {
  compareFromTXTBody.value.file = file.raw
}

const submitUpload = () => {
  compareFromCsvMethod()
}

const submitUploadTXT = () => {
  compareFromTXTMethod()
}

const compareData = () => {
  compareMethod()
  inputDataVisible.value = false
}

const compareTXTData = () => {
  compareTXTMethod()
  inputTextDataVisible.value = false
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
</script>

<style scoped></style>
