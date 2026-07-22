<template>    
  <div>
  <!-- <span style="display: block; margin-bottom: 8px;">选择代理</span> -->
  <!-- 创建一个button，点击代用创建ray节点的函数 -->
  <el-header style="height: 50px">
  <el-button class="default-button" @click="activeRayMethod">创建ray节点</el-button>
  <el-button class="default-button" @click="stopRayMethod">关闭ray节点</el-button>
  <el-button class="default-button" @click="getRayStatusMethod">查看节点状态</el-button>
  <el-select v-model="agentId" placeholder="选择代理" style="width: 240px;margin-left: 15px;margin-right: 15px" @change="handleSelectAgent">
        <el-option
            v-for="item in agents"
            :key="item.value"
            :label="item.label"
            :value="item.value"
        />
      </el-select>
      <el-button class="default-button" @click="joinRayMethod">将该agent加入当前节点</el-button>
  </el-header>
  <!-- 创建一个下拉框 -->
  <el-dialog v-model="alertMessageVisible" :title="alertTitle" width="50%">
  <span>
      <pre>{{ alertMessage }}</pre>
  </span>
<template #footer>
  <div class="dialog-footer">
    <el-button class="close-button" @click="alertMessageVisible = false">返回</el-button>
    
  </div>
</template>
</el-dialog>
  
</div>
    <el-container>
      
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
          <span>上传输入数据</span>
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
            <el-button class="default-button" style="margin-right: 10px;">上传文件</el-button>
          </template>
          <el-button class="start-button" @click="submitUpload">
            创建联邦学习任务
          </el-button>
          <template #tip>
            <div class="el-upload__tip text-red">
              限制1个文件，新文件将覆盖旧文件
            </div>
          </template>
        </el-upload>
      </el-main>
    </el-container>

    <el-dialog v-model="createFLTaskVisible" :title="createFLTaskTitle" width="30%">
      <span>{{ createFLTaskMessage }}</span>
      <template #footer>
        <div class="dialog-footer">
          <el-button class="close-button" @click="createFLTaskVisible = false" style="margin-right: 10px;">返回</el-button>
          <router-link to="/result/fL">
            <el-button class="default-button">
              查看结果管理区
            </el-button>
          </router-link>
        </div>
      </template>
    </el-dialog>
    <el-dialog v-model="rayStatusDialogVisible" :title="rayStatusDialogTitle" width="30%">
      <el-table
              stripe
              :data="nodesWithUsage"
              style="width: 100%"
              max-height="300"
          >
          
            <el-table-column
                label="节点名称"
                prop="userName"
                width="120"
                align="center"
            ></el-table-column>
            <el-table-column
                label="节点ID"
                prop="nodeId"
                width="300"
                align="center"><template #default="scope">
              <el-popover effect="light" trigger="hover" placement="top" width="auto">
                <template #default>
                  <div>完整ID: {{ scope.row.nodeId }}</div>
                </template>
                <template #reference>
                  <el-tag>{{ scope.row.nodeId }}</el-tag>
                </template>
              </el-popover>
            </template></el-table-column>
            <el-table-column
                label="内存使用"
                prop="memoryUsage"
                width="180"
                align="center"
            ></el-table-column>
          </el-table>
      <template #footer>
        <div class="dialog-footer">
          <el-button class="close-button" @click="rayStatusDialogVisible = false">返回</el-button>
        </div>
      </template>
    </el-dialog>
</template>

<script lang="ts" setup>
import {activeRay,stopRay,getRayStatus,executeTask,joinRay} from '../../api/fLearning.js'
import {getAgent} from '../../api/testDve.js'
import {getDirectory, getRootByAgent} from '../../api/folderController.js'
import {onMounted, ref,reactive,computed} from "vue";
import {genFileId, UploadInstance, UploadProps, UploadRawFile} from "element-plus";
import {Connection} from "@element-plus/icons-vue";

onMounted(() => {
    getAgentMethod()
    getDirectoryMethod()
  })


// 对话框是否可见
const createFLTaskVisible = ref(false)
const createFLTaskTitle = ref('')

  const createFLTaskMessage = ref('')


  const agents = ref([])
  const agentId = ref('')
  const applicationId = "DAVEX-C1-A1"
  const directoryData = ref([])
  const currentDirectoryData = ref([])
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
  const executeTaskBody = ref({
    taskName: 'HFL',
    outputPath: 'result',
  })
  const joinRayBody = ref({
    ip:'10.176.34.173',
    port:'9876',
    name:'',
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
  
  

  // const joinRayMethod = async()=>{
  //   joinRayBody.value.name = agentId;
  //   await joinRay(joinRayBody.value);
    
  // }
  const createMethod = async () => {
    try {
      createFLTaskMessage.value = `联邦学习任务创建完成`
      createFLTaskTitle.value = `创建成功`
      createFLTaskVisible.value = true
      await executeTask(executeTaskBody.value)
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

const alertMessageVisible = ref(false)
const alertTitle = ref('')
const alertMessage = ref('')
const editAlertMeassageMethod =(title,message)=>{
  alertTitle.value = title
  alertMessage.value = message
} 

const activePort = ref({
    port: '9876'
  })
const activeRayMethod = async () => {
    try {
        const res = await activeRay(activePort.value)
        if(res.data.code == 1){
          editAlertMeassageMethod('反馈信息','创建ray节点成功')
        }else{
          editAlertMeassageMethod('警告','当前已存在ray节点,创建失败')
        }
        
        alertMessageVisible.value = true
    } catch (error) {
        createFLTaskVisible.value = true
        editAlertMeassageMethod('创建ray节点失败',error)
    }
  }
const stopRayMethod = async () => {
    try {
        const res = await stopRay()
        editAlertMeassageMethod('关闭ray节点','已关闭所有结点')
        alertMessageVisible.value = true
    } catch (error) {
        console.log(error)
    }
  }

  const state = reactive({
  activeNodes: [],
  usage: {},
});
const activeNodes = computed(() => state.activeNodes);
const usage = computed(() => state.usage);
  // 解析函数
function parseNodeStatus(message) {
  const activeNodes = [];
  const usage = {};
  // 解析 active 节点
  const activePattern = /Active:\s+((?:\d+\s+[\w\d]+(?:\s+[\w\d]+)*\s*)+)/g;
  const activeMatch = activePattern.exec(message);
  if (activeMatch) {
    const nodesString = activeMatch[1].trim();
    const nodesArray = nodesString.split('\n');
    
    // 遍历每行提取节点信息
    nodesArray.forEach(nodeLine => {
      const parts = nodeLine.trim().split(' ');
      if (parts.length === 2) {
        const nodeId = parts[1];
        activeNodes.push(nodeId);
      }
    });
  }

//  // 解析 usage 部分的用户内存数据
//  const userMemoryPattern = /(\d+(\.\d+)?|0B)\/(\d+(\.\d+)?)([a-zA-Z0-9\-]+)/g;
//   let match;
//   let skipLines = 1;

//   while ((match = userMemoryPattern.exec(message)) !== null) {
 
//     if (skipLines > 0) {
//       skipLines -= 1;
//       continue;
//     }
//     const memoryUsage = `${match[1]}/${match[3]}G`;  // 用户的内存使用格式：0/16.0
//     const userName = match[5];  // 用户名（如 alice、bob 等）
//     usage[userName] = memoryUsage;  // 将用户和内存使用量存入对象
//   }
// 解析 usage 部分的特定格式数据
const usageLines = message.split('\n');
  let isUsageSection = false;
  let skipLines = 1;

  usageLines.forEach(line => {
    line = line.trim();
    if (line.startsWith("Usage:")) {
      isUsageSection = true; // 检测到 Usage 开始部分
      return;
    }
    if (isUsageSection && line.length > 0) {
      // 匹配指定格式的行
      const match = /^(\d+(\.\d+)?|0B)\/(\d+(\.\d+)?)\s+([a-zA-Z0-9\-]+)$/.exec(line);
      if (match) {
        if (skipLines > 0) {
            skipLines -= 1;
        }else{

        const memoryUsage = `${match[1]}/${match[3]}G`; // 格式化内存使用量
        const userName = match[5]; // 提取用户名（如 CPU、DAVEX-C1 等）
        usage[userName] = memoryUsage; // 将用户和内存使用量存入对象
        }
      }
    }
  });

  // 更新 state 中的值
  state.activeNodes = activeNodes;
  state.usage = usage;
}

// 计算属性（如果需要动态响应）
const nodesWithUsage = computed(() => {
  // 创建一个新数组，组合节点信息和内存使用信息
  return state.activeNodes.map((nodeId, index) => {
    const userName = Object.keys(state.usage)[index]; // 获取对应用户的名称
    const memoryUsage = state.usage[userName] || '0/16.0'; // 默认显示 '0/16.0' 如果没有找到
    return {
      userName,
      nodeId,
      memoryUsage,
    };
  });
});

  const getRayStatusMethod = async () => {
    try {
        const res = await getRayStatus()
        if(res.data.code == 1){

          parseNodeStatus(res.data.message);
          rayStatusDialogVisible.value = true
        }else{
        editAlertMeassageMethod('查看节点状态','当前无ray节点,请先进行创建')
        alertMessageVisible.value = true
        }
        
        console.log(res)
    } catch (error) {
        console.log(error)
    }
  }

const joinRayMethod = async () => {
    try {
      joinRayBody.value.name = agentId;
        const res = await joinRay(joinRayBody.value)
        editAlertMeassageMethod('加入ray节点','已加入ray节点')
        alertMessageVisible.value = true
    } catch (error) {
        console.log(error)
    }
  }
//dialog管理区 和dialog管理有关的变量声明 以及操作在这个区域
const rayStatusDialogVisible = ref(false)
const rayStatusDialogTitle = ref('节点状态')

//dialog管理区 和dialog管理有关的变量声明 以及操作在这个区域


</script>



<style scoped>
pre {
  text-align: center; /* 预格式化文本对齐方式 */
  word-wrap: break-word; /* 保证长单词换行显示 */
}</style>