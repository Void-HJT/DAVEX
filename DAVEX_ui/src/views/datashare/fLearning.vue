<template>    
  <div>
  <!-- <span style="display: block; margin-bottom: 8px;">选择代理</span> -->
  <!-- 创建一个button，点击代用创建ray节点的函数 -->
  <el-header style="height: 50px">
  <el-button type="primary" @click="activeRayMethod">创建ray节点</el-button>
  <el-button type="primary" @click="stopRayMethod">关闭ray节点</el-button>
  <el-button type="primary" @click="getRayStatusMethod">查看节点状态</el-button>
  <el-select v-model="agentId" placeholder="选择代理" style="width: 240px;margin-left: 15px;" @change="handleSelectAgent">
        <el-option
            v-for="item in agents"
            :key="item.value"
            :label="item.label"
            :value="item.value"
        />
      </el-select>
  </el-header>
  <!-- 创建一个下拉框 -->
  <el-dialog v-model="rayStatusVisible" title="节点信息" width="50%">
  <span>
      <pre>{{ windowMessage }}</pre>
  </span>
<template #footer>
  <div class="dialog-footer">
    <el-button @click="rayStatusVisible = false">返回</el-button>
    
  </div>
</template>
</el-dialog>
  
</div>
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
                    chooseModelMethod(
                      scope.row.agentId,
                      scope.row.uid,
                      scope.row.name
                    )
                  "
                    size="small"
                >
                  选择该模型文件
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
            :title="'当前选择文件：' + fileName"
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
            <el-button type="primary" style="margin-right: 10px;">上传文件</el-button>
          </template>
          <el-button class="ml-3" type="success" @click="submitUpload" style="margin-right: 10px;">
            创建联邦学习任务
          </el-button>
          <template #tip>
            <div class="el-upload__tip text-red">
              你可以在此选择一个文件并上传
            </div>
          </template>
        </el-upload>
      </el-main>
    </el-container>

    <el-dialog v-model="alertVisible" :title="alertTitle" width="30%">
      <span>{{ alertMessage }}</span>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="alertVisible = false" style="margin-right: 10px;">返回</el-button>
          <router-link to="/result/fL">
            <el-button type="primary">
              查看结果管理区
            </el-button>
          </router-link>
        </div>
      </template>
    </el-dialog>

</template>

<script lang="ts" setup>
import {activeRay,stopRay,getRayStatus} from '../../api/fLearning.js'
import {getAgent} from '../../api/testDve.js'
import {getDirectory, getRootByAgent} from '../../api/folderController.js'
import {create} from "../../api/secureInfer.js";
import {onMounted, ref} from "vue";
import {genFileId, UploadInstance, UploadProps, UploadRawFile} from "element-plus";

onMounted(() => {
    getAgentMethod()
    getDirectoryMethod()
  })


// 对话框是否可见
const alertVisible = ref(false)
const alertTitle = ref('')

  const alertMessage = ref('')


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
      alertMessage.value = `联邦学习任务创建完成`
      alertTitle.value = `创建成功`
        alertVisible.value = true
      // console.log(createBody.value)
      // const res = await create(createBody.value)
      // console.log(res.data)
      // if (res.data.body.code == 1) {
      //   alertMessage.value = `联邦学习任务创建完成`
      //   alertVisible.value = true
      // }
      // else {

      // }
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

const rayStatusVisible = ref(false)
const windowMessage = ref('')

const activePort = ref({
    port: '9876'
  })
const activeRayMethod = async () => {
    try {
        console.log(activePort.value)
        const res = await activeRay(activePort.value)
        
    } catch (error) {
        console.log(error)
    }
  }
const stopRayMethod = async () => {
    try {
        const res = await stopRay()
        console.log(res)
    } catch (error) {
        console.log(error)
    }
  }

  const getRayStatusMethod = async () => {
    try {
        const res = await getRayStatus()
        windowMessage.value = res.data.message
        rayStatusVisible.value = true
        console.log(res)
    } catch (error) {
        console.log(error)
    }
  }

</script>



<style scoped>
pre {
  text-align: center; /* 预格式化文本对齐方式 */
  word-wrap: break-word; /* 保证长单词换行显示 */
}</style>