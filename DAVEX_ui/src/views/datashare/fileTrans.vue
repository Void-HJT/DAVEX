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
                  v-if="scope.row.type === 'file'"
                  link
                  type="primary"
                  @click="
                  getFileMethod(
                    scope.row.uid,
                    scope.row.agentId,
                    scope.row.parentId
                  )
                "
                  size="small"
              >
                获取文件
              </el-button>
<!--              <el-button-->
<!--                  v-if="scope.row.type === 'file' && !isAccessible(scope.row.ruleList)"-->
<!--                  link-->
<!--                  type="danger"-->
<!--                  size="small"-->
<!--                  disabled-->
<!--              >-->
<!--                无权获取文件-->
<!--              </el-button>-->
            </template>
          </el-table-column>
        </el-table>
      </div>
      <el-dialog v-model="transSuccessVisible" title="文件传输结果" width="30%">
        <span>文件传输完成</span>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="transSuccessVisible = false" style="margin-right: 10px;">返回</el-button>
            <router-link to="/result/fileTrans">
              <el-button type="primary">
                  查看结果管理区
              </el-button>
            </router-link>
          </div>
        </template>
      </el-dialog>
      <el-dialog v-model="transFailedVisible" title="文件传输结果" width="30%">
        <span>{{ transFailedMessage }}</span>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="transFailedVisible = false">返回</el-button>
          </div>
        </template>
      </el-dialog>
    </el-main>
  </el-container>
</template>

<script lang="ts" setup>
import {getAgent} from '../../api/testDve.js'
import {getDirectory, getRootByAgent} from '../../api/folderController.js'
import {getFile} from "../../api/direct.js";
import {onMounted, ref} from "vue";

onMounted(() => {
  getAgentMethod()
  getDirectoryMethod()
})

// 对话框是否可见
const transSuccessVisible = ref(false)
const transFailedVisible = ref(false)
const transFailedMessage = ref('');

const agents = ref([])
const agentId = ref('')
const applicationId = 6
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
const getFileBody = ref({
  fileId: '',
  agentId: '',
  folderId: '',
  applicationId: applicationId
})

function addFolderRoute(row) {
  folderRoute.value.push(row.uid, row.name)
}
function deleteFolderRoute() {
  folderRoute.value.pop()
  folderRoute.value.pop()
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

const getFileMethod = async (uid, agentId, folderId) => {
  try {
    getFileBody.value.fileId = uid
    getFileBody.value.agentId = agentId
    getFileBody.value.folderId = folderId
    const res = await getFile(getFileBody.value)
    if (res.data.code == 1) {
      transSuccessVisible.value = true
    }
    else {
      transFailedMessage.value = res.data.message;
      transFailedVisible.value = true
    }
  }
  catch (error) {
    console.error('Failed to get file:', error)
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

const isAccessible = (ruleList) => {
  return ruleList.includes('direct')
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
