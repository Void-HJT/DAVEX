<template>
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
                link
                type="primary"
                @click="
                  deleteFolderMethod(
                    scope.row.uid,
                    scope.row.agentId,
                    scope.row.type,
                    scope.row.parentId,
                  )
                "
                size="small"
              >
                删除
              </el-button>
              <el-button
                link
                type="primary"
                @click="
                  scope.row.type === 'folder'
                    ? openFolderRenameBlock(
                        scope.row.uid,
                        scope.row.agentId,
                        scope.row.name,
                      )
                    : openFileRenameBlockMethod(
                        scope.row.uid,
                        scope.row.agentId,
                        scope.row.name,
                      )
                "
                size="small"
              >
                重命名
              </el-button>
              <el-button
                v-if="scope.row.type === 'file'"
                link
                type="primary"
                @click="
                  getFileInfoMethod(
                    scope.row.uid,
                    scope.row.agentId,
                    scope.row.parentId,
                    scope.row.type,
                  )
                "
                size="small"
              >
                权限管理
              </el-button>
              <el-button
                v-if="scope.row.type === 'folder'"
                link
                type="primary"
                @click="
                  (folderVisibleDialogVisible = true),
                    (folderVisibleBody.folderId = scope.row.uid)
                "
                size="small"
              >
                可见性管理
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-popover
          placement="top-start"
          title="此处输入新文件夹名"
          :width="400"
          trigger="click"
        >
          <template #reference>
            <div>
              <div style="height: 20px"></div>
              <el-button class="el-button mt-4" style="width: 100%">
                在当前目录下新建文件夹
              </el-button>
              <div style="height: 10px"></div>
            </div>
          </template>
          <el-form
            :model="createFolderBody"
            label-width="80px"
            label-position="left"
          >
            <el-form-item label="文件夹名">
              <el-input v-model="createFolderBody.name"></el-input>
            </el-form-item>
            <el-button
              @click="createFolderMethod"
              calss="el-button mt-4"
              style="width: 100%"
            >
              增加组别
            </el-button>
          </el-form>
        </el-popover>
        <el-upload
          ref="upload"
          :auto-upload="false"
          :action="uploadUrl"
          @change="handleChange"
          width="100%"
        >
          <el-button style="width: 100%">选择文件</el-button>
        </el-upload>
        <el-button @click="submitUpload" style="width: 100%">
          上传到当前文件夹
        </el-button>
      </div>
      <el-dialog v-model="fileInfoVisible" title="文件详细信息" width="60%">
        <!-- <el-table :data="fileInfoData" style="width: 100%">
          <el-table-column label="Uid" prop="uid" width="80"></el-table-column>
          <el-table-column
            label="所属代理"
            prop="agentId"
            width="80"
          ></el-table-column>
          <el-table-column
            label="名称"
            prop="name"
            width="180"
          ></el-table-column>
          <el-table-column
            label="大小"
            prop="size"
            width="180"
          ></el-table-column>
          <el-table-column
            label="描述"
            prop="description"
            width="180"
          ></el-table-column>
          <el-table-column
            label="哈希"
            prop="hash"
            width="180"
          ></el-table-column>
        </el-table> -->
        <span
          style="
            display: block;
            text-align: center;
            font-size: 16px;
            line-height: 3;
          "
        >
          分组与权限信息
        </span>
        <el-table :data="userGroupData" style="width: 100%">
          <el-table-column
            label="组Id"
            prop="uid"
            width="180"
          ></el-table-column>
          <el-table-column
            label="所属中心"
            prop="centerId"
            width="180"
          ></el-table-column>
          <el-table-column
            label="名称"
            prop="name"
            width="180"
          ></el-table-column>
          <el-table-column
            label="权限"
            prop="rule"
            width="180"
          ></el-table-column>
        </el-table>
        <span
          style="
            display: block;
            text-align: center;
            font-size: 16px;
            line-height: 3;
          "
        >
          该文件拥有的权限
        </span>
        <el-table :data="fileRuleListData" style="width: 100%">
          <el-table-column label="Uid" prop="uid" width="80"></el-table-column>
          <el-table-column
            label="所属代理"
            prop="agentId"
            width="80"
          ></el-table-column>
          <el-table-column
            label="组别"
            prop="groupId"
            width="180"
          ></el-table-column>
          <el-table-column
            label="拥有权限"
            prop="allowedMethod"
            width="180"
          ></el-table-column>
        </el-table>
        <span
          style="
            display: block;
            text-align: center;
            font-size: 16px;
            line-height: 3;
          "
        >
          更新文件权限
        </span>
        <el-form
          :model="setFileRuleBody"
          label-width="80px"
          style="max-width: 600px"
        >
          <el-form-item label="组别">
            <el-input v-model="setFileRuleBody.groupId"></el-input>
          </el-form-item>
          <el-form-item label="权限">
            <el-input v-model="setFileRuleBody.allowedMethod"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button @click="setFileRuleMethod">赋予文件权限</el-button>
            <el-button @click="deleteFileRuleMethod">移除文件权限</el-button>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button type="primary" @click="closeFileInfo">确认</el-button>
          </span>
        </template>
      </el-dialog>
      <el-dialog
        v-model="folderVisibleDialogVisible"
        title="可见性管理"
        width="500"
        :before-close="folderVisibleDialogClose"
      >
        <el-form-item label="选择分组">
          <el-select
            v-model="folderVisibleBody.groupId"
            placeholder="请选择需要管理的分组"
          >
            <el-option
              v-for="item in userGroupData"
              :key="item.uid"
              :label="item.name"
              :value="item.uid"
            />
          </el-select>
        </el-form-item>
        <template #footer>
          <div class="dialog-footer">
            <el-button type="primary" @click="setFolderVisibleMethod">
              设置该组可见该文件夹
            </el-button>
            <el-button type="primary" @click="setFolderInvisibleMethod">
              设置该组不可见该文件夹
            </el-button>
          </div>
        </template>
      </el-dialog>

      <el-dialog v-model="folderRenameVisible" title="文件夹重命名" width="30%">
        <el-form
          :model="folderRenameBody"
          label-width="20%"
          style="max-width: 80%"
        >
          <el-form-item label="新文件夹名">
            <el-input v-model="folderRenameBody.name"></el-input>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button type="primary" @click="closeFolderRenameBlock">
              确认
            </el-button>
          </span>
        </template>
      </el-dialog>
      <el-dialog v-model="fileRenameVisible" title="文件重命名" width="30%">
        <el-form :model="fileInfoBody" label-width="20%" style="max-width: 80%">
          <el-form-item label="新文件名">
            <el-input v-model="updateFileBody.name"></el-input>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button type="primary" @click="closeFileRenameBlock">
              确认
            </el-button>
          </span>
        </template>
      </el-dialog>
    </el-main>
  </el-container>
  <el-form
    :model="getDirectoryByGroupBody"
    label-width="80px"
    style="max-width: 600px"
  >
    <el-form-item label="组Id">
      <el-input v-model="getDirectoryByGroupBody.groupId"></el-input>
    </el-form-item>
    <el-form-item>
      <el-button @click="getDirectoryByGroupMethod">
        按照分组获取文件列表
      </el-button>
    </el-form-item>
  </el-form>
  <el-table
    :data="directoryByGroupData"
    style="width: 100%"
    @row-click="showFileInfo"
    @row-dblclick="nextFileGroup"
  >
    <el-table-column label="Uid" prop="uid" width="80"></el-table-column>
    <el-table-column
      label="所属代理"
      prop="agentId"
      width="80"
    ></el-table-column>
    <el-table-column label="名称" prop="name" width="180"></el-table-column>
    <el-table-column label="类型" prop="type" width="180"></el-table-column>
    <el-table-column
      label="创建时间"
      prop="createDate"
      width="380"
      :formatter="formatDate"
    ></el-table-column>
    <el-table-column
      label="更新时间"
      prop="lastUpdate"
      width="380"
      :formatter="formatDate"
    ></el-table-column>
  </el-table>
  <el-form
    :model="getDirectoryByApplicationBody"
    label-width="80px"
    style="max-width: 600px"
  >
    <el-form-item label="用户Id">
      <el-input
        v-model="getDirectoryByApplicationBody.applicationId"
      ></el-input>
    </el-form-item>
    <el-form-item>
      <el-button @click="getDirectoryByApplicationMethod">
        按照用户获取文件列表
      </el-button>
    </el-form-item>
  </el-form>
  <el-table
    :data="directoryByApplicationData"
    style="width: 100%"
    @row-click="showFileInfo"
    @row-dblclick="nextFileGroup"
  >
    <el-table-column label="Uid" prop="uid" width="80"></el-table-column>
    <el-table-column
      label="所属代理"
      prop="agentId"
      width="80"
    ></el-table-column>
    <el-table-column label="名称" prop="name" width="180"></el-table-column>
    <el-table-column label="类型" prop="type" width="180"></el-table-column>
    <el-table-column
      label="创建时间"
      prop="createDate"
      width="380"
      :formatter="formatDate"
    ></el-table-column>
    <el-table-column
      label="更新时间"
      prop="lastUpdate"
      width="380"
      :formatter="formatDate"
    ></el-table-column>
  </el-table>
</template>

<script lang="ts" setup>
import { ref, computed } from 'vue'
import {
  getDirectory,
  getDirectoryByGroup,
  deleteFolder,
  deleteFile,
  createFolder,
  uploadFile,
  setFolderName,
  setFolderInvisible,
  setFolderVisible,
  getFileInfo,
  setFileRule,
  deleteFileRule,
  getDirectoryByApplication,
  updateFile,
  getFile,
} from '../../api/folderController.js'
import { getGroup, getRuleByGroup } from '../../api/testDve.js'
import { genFileId } from 'element-plus'
import type { UploadInstance, UploadProps, UploadRawFile } from 'element-plus'
import { nextTick, onMounted } from 'vue'

onMounted(() => {
  getDirectoryMethod()
  getGroups()
})

// 对话框是否可见
const fileRenameVisible = ref(false)
const folderVisibleDialogVisible = ref(false)

// 对话框关闭时的回调
const folderVisibleDialogClose = () => {
  folderVisibleDialogVisible.value = false
  folderVisibleBody.value.groupId = ''
}

const directoryData = ref([])
const currentDirectoryData = ref([])
const directoryByGroupData = ref([])
const fileInfoData = ref([])
const fileInfoVisible = ref(false)
const folderRenameVisible = ref(false)
const fileRuleListData = ref([])
const userGroupData = ref([])
const directoryByApplicationData = ref([])
const updateFileBody = ref({
  uid: '',
  agentId: '',
  folderId: '',
  name: '1',
  createDate: '',
  lastUpdate: '',
  tag: '',
  size: '',
  description: '',
  expiredTime: '',
  hash: '',
  example: '',
  type: '',
})

const uploadUrl = computed(() => {
  const url = `http://10.176.37.50:8080/directory/fileFolder/uploadFile?agentId=${uploadFileBody.value.agentId}&folderId=${uploadFileBody.value.folderId}`
  console.log('url:', url)
  return url
})

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

const handleChange = () => {
  console.log('upload:', upload.value?.uploadFiles)
}

const closeFileInfo = () => {
  fileInfoVisible.value = false
}

const getFileInfoBody = ref({
  uid: '',
  agentId: '',
  folderId: '',
})

const deleteFolderBody = ref({
  agentId: '',
  folderId: '',
})

const deleteFileBody = ref({
  fileId: '',
  agentId: '',
  folderId: '',
})

const createFolderBody = ref({
  name: '',
  agentId: '5',
  parentId: '',
})

const folderRenameBody = ref({
  agentId: '5',
  folderId: '',
  name: '',
})

const fileInfoBody = ref({
  fileId: '',
  agentId: '',
})

const uploadFileBody = ref({
  agentId: '',
  folderId: '',
  file: '',
})

const getGroupBody = ref({
  agentId: 5,
  centerId: 1,
})

const getRuleByGroupBody = ref({
  agentId: '5',
  groupId: '1',
})

const setFileRuleBody = ref({
  fileId: '',
  agentId: '',
  folderId: '',
  groupId: '',
  allowedMethod: '',
})

const upload = ref<UploadInstance | null>(null)

const getGroups = async () => {
  try {
    const res = await getGroup(getGroupBody.value)

    //遍历res.data.data，对于每一个group，使用getRuleByGroup求得rule的值
    for (let i = 0; i < res.data.data.length; i++) {
      getRuleByGroupBody.value.agentId = '5'
      getRuleByGroupBody.value.groupId = res.data.data[i].uid
      const rule = await getRuleByGroup(getRuleByGroupBody.value)
      //为res新增一个维度rule，其值为loading 的string
      res.data.data[i].rule = ''
      //若rule不为空，则遍历rule，将每个rule中的uid添加到res[i].rule中
      if (rule.data.data != null) {
        for (let j = 0; j < rule.data.data.length; j++) {
          console.log(rule.data.data[j])
          res.data.data[i].rule += rule.data.data[j].allowedMethod + ' '
        }
      }
    }

    userGroupData.value = res.data.data
  } catch (error) {
    console.error('Failed to get group list:', error)
  }
}

const setFileRuleMethod = async () => {
  try {
    await setFileRule(setFileRuleBody.value)
    const res = await getFileInfo(getFileInfoBody.value)
    fileRuleListData.value = res.data.data.ruleList
  } catch (error) {
    console.error('Failed to set file rule:', error)
  }
}

const deleteFileRuleMethod = async () => {
  try {
    await deleteFileRule(setFileRuleBody.value)
    const res = await getFileInfo(getFileInfoBody.value)
    fileRuleListData.value = res.data.data.ruleList
  } catch (error) {
    console.error('Failed to set file rule:', error)
  }
}

const getFileInfoMethod = async (uid, agentId, parentId, type) => {
  try {
    if (type === 'file') {
      setFileRuleBody.value.fileId = uid
      console.log('setFileRuleBody:', setFileRuleBody.value)
      setFileRuleBody.value.agentId = agentId
      setFileRuleBody.value.folderId = parentId
      getFileInfoBody.value.uid = uid
      getFileInfoBody.value.agentId = agentId
      getFileInfoBody.value.folderId = parentId
      console.log('getFileInfoBody:', getFileInfoBody.value)
      const res = await getFileInfo(getFileInfoBody.value)
      fileInfoData.value = [res.data.data]
      fileRuleListData.value = res.data.data.ruleList
      fileInfoVisible.value = true
      getGroups()
    }
  } catch (error) {
    console.error('Failed to get file info:', error)
  }
}

const submitUpload = async () => {
  uploadFileBody.value.agentId = '5'
  uploadFileBody.value.folderId = currentParentId.value
  upload.value!.submit()
  await nextTick()
  getDirectoryMethod()
}

const setFolderInvisibleMethod = async () => {
  try {
    await setFolderInvisible(folderVisibleBody.value)
    getDirectoryByGroupMethod()
  } catch (error) {
    console.error('Failed to set folder invisible:', error)
  }
}

const setFolderVisibleMethod = async () => {
  try {
    await setFolderVisible(folderVisibleBody.value)
    getDirectoryByGroupMethod()
  } catch (error) {
    console.error('Failed to set folder visible:', error)
  }
}

const createFolderMethod = async () => {
  try {
    createFolderBody.value.agentId = '5'
    createFolderBody.value.parentId = currentParentId.value
    await createFolder(createFolderBody.value)
    findCurrentFolder()
  } catch (error) {
    console.error('Failed to create folder:', error)
  }
}

const deleteFolderMethod = async (uid, agentId, type, parentId) => {
  try {
    //如果是文件夹
    if (type === 'folder') {
      deleteFolderBody.value.agentId = agentId
      deleteFolderBody.value.folderId = uid
      await deleteFolder(deleteFolderBody.value)
    } else {
      deleteFileBody.value.agentId = agentId
      deleteFileBody.value.fileId = uid
      deleteFileBody.value.folderId = parentId
      await deleteFile(deleteFileBody.value)
    }
    findCurrentFolder()
  } catch (error) {
    console.error('Failed to delete folder:', error)
  }
}

const showFileInfo = (row) => {
  //如果type是file
  if (row.type === 'file') {
    fileInfoVisible.value = true
    fileInfoData.value = [row]
  }
}

const folderRoute = ref([])
function addFolderRoute(row) {
  folderRoute.value.push(row.uid, row.name)
}
function deleteFolderRoute() {
  folderRoute.value.pop()
  folderRoute.value.pop()
}
function getLastFolderRoute() {
  return folderRoute.value[folderRoute.value.length - 1]
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

const handleCellDoubleClick = async (row) => {
  if (row.type === 'folder') {
    addFolderRoute(row)
    console.log('folderRoute:', folderRoute.value)
    currentParentId.value = row.uid
    directoryData.value = row.children
  }
}

const nextFileGroup = async (row) => {
  console.log('row:', row)
  if (row.type === 'folder') {
    directoryByGroupData.value = row.children
  }
}

const getDirectoryBody = ref({
  rootId: '1',
})

const getDirectoryByGroupBody = ref({
  rootId: '1',
  agentId: '5',
  groupId: '',
})

const getDirectoryByApplicationBody = ref({
  rootId: '1',
  agentId: '5',
  applicationId: '',
})

const folderVisibleBody = ref({
  agentId: '5',
  groupId: '',
  folderId: '',
})

const currentParentId = ref('1')

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

const getDirectoryByGroupMethod = async () => {
  try {
    const res = await getDirectoryByGroup(getDirectoryByGroupBody.value)
    const res1 = res.data.data.children
    directoryByGroupData.value = res1
  } catch (error) {
    console.error('Failed to get group list:', error)
  }
}

const getDirectoryByApplicationMethod = async () => {
  try {
    const res = await getDirectoryByApplication(
      getDirectoryByApplicationBody.value,
    )
    const res1 = res.data.data.children
    directoryByApplicationData.value = res1
  } catch (error) {
    console.error('Failed to get group list:', error)
  }
}

const openFolderRenameBlock = (Uid, agentId, name) => {
  folderRenameVisible.value = true
  folderRenameBody.value.folderId = Uid
  folderRenameBody.value.agentId = agentId
  folderRenameBody.value.name = name
}

const closeFolderRenameBlock = async () => {
  await setFolderName(folderRenameBody.value)
  folderRenameVisible.value = false
  getDirectoryMethod()
}

const openFileRenameBlockMethod = async (Uid, agentId, name) => {
  fileRenameVisible.value = true
  console.log('name:', name)
  fileInfoBody.value.agentId = agentId
  fileInfoBody.value.fileId = Uid
  const res = await getFile(fileInfoBody.value)
  console.log('res:', res.data.body.data)
  updateFileBody.value = res.data.body.data
}

const closeFileRenameBlock = async () => {
  await updateFile(updateFileBody.value)
  fileRenameVisible.value = false
  getDirectoryMethod()
}

const returnFrontDirectory = async () => {
  deleteFolderRoute()
  console.log('folderRoute:', folderRoute.value)
  findCurrentFolder()
}
</script>

<style></style>
