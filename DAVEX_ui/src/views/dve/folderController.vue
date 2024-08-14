<template>
  <el-button @click="getDirectoryMethod">获取文件列表</el-button>

  <div>
    <el-table
      :data="directoryData"
      style="width: 100%"
      v-loading="taskTableLoading"
      @row-dblclick="handleCellDoubleClick"
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
        width="300"
        :formatter="formatDate"
      ></el-table-column>
      <el-table-column
        label="更新时间"
        prop="lastUpdate"
        width="300"
        :formatter="formatDate"
      ></el-table-column>
      <el-table-column
        fixed="right"
        label="操作"
        width="300"
        header-align="center"
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
            @click="openFilenameBlock(scope.row.uid)"
            size="small"
          >
            重命名
          </el-button>
          <el-button
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
          <!-- <el-button
            link
            type="primary"
            @click="handleResult(scope.row.id, scope.row.status)"
            size="small"
            >获取结果</el-button
          >
          <el-button link type="primary" size="small" @click = "deleteTaskByID(scope.row.id)">删除</el-button> -->
        </template>
      </el-table-column>
    </el-table>
    <el-form
      :model="createFolderBody"
      label-width="80px"
      style="max-width: 600px"
    >
      <el-form-item label="文件夹Id">
        <el-input v-model="createFolderBody.name"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="createFolderMethod">
          在当前目录下创建新文件夹
        </el-button>
      </el-form-item>
    </el-form>

    <el-form
      :model="folderVisibleBody"
      label-width="80px"
      style="max-width: 600px"
    >
      <el-form-item label="文件夹Id">
        <el-input v-model="folderVisibleBody.folderId"></el-input>
      </el-form-item>
      <el-form-item label="组Id">
        <el-input v-model="folderVisibleBody.groupId"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="setFolderVisibleMethod">
          设置该组可见该文件夹
        </el-button>
        <el-button @click="setFolderInvisibleMethod">
          设置该组不可见该文件夹
        </el-button>
      </el-form-item>
    </el-form>
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
      v-loading="taskTableLoading"
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
      v-loading="taskTableLoading"
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
  </div>
  <el-dialog v-model="fileInfoVisible" title="文件详细信息" width="60%">
    <el-table
      :data="fileInfoData"
      style="width: 100%"
      v-loading="taskTableLoading"
    >
      <el-table-column label="Uid" prop="uid" width="80"></el-table-column>
      <el-table-column
        label="所属代理"
        prop="agentId"
        width="80"
      ></el-table-column>
      <el-table-column label="名称" prop="name" width="180"></el-table-column>
      <el-table-column label="大小" prop="size" width="180"></el-table-column>
      <el-table-column
        label="描述"
        prop="description"
        width="180"
      ></el-table-column>
      <el-table-column label="哈希" prop="hash" width="180"></el-table-column>
    </el-table>
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
    <el-table
      :data="userGroupData"
      style="width: 100%"
      @row-click="handleRowClick"
      v-loading="taskTableLoading"
    >
      <el-table-column label="组Id" prop="uid" width="180"></el-table-column>
      <el-table-column
        label="所属中心"
        prop="centerId"
        width="180"
      ></el-table-column>
      <el-table-column label="名称" prop="name" width="180"></el-table-column>
      <el-table-column label="权限" prop="rule" width="180"></el-table-column>
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
    <el-table
      :data="fileRuleListData"
      style="width: 100%"
      v-loading="taskTableLoading"
    >
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
  <el-dialog v-model="fileRenameVisible" title="文件重命名" width="30%">
    <el-form :model="fileRenameBody" label-width="20%" style="max-width: 80%">
      <el-form-item label="新文件名">
        <el-input v-model="fileRenameBody.name"></el-input>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button type="primary" @click="closeFileRenameBlock">确认</el-button>
      </span>
    </template>
  </el-dialog>
  <el-upload
    ref="upload"
    :auto-upload="false"
    :action="uploadUrl"
    @change="handleChange"
  >
    <el-button type="primary">从本地选择文件</el-button>
  </el-upload>
  <el-button @click="submitUpload">上传到当前文件夹</el-button>

  <!-- <el-upload
    ref="upload"
    class="upload-demo"
    action="https://run.mocky.io/v3/9d059bf9-4660-45f2-925d-ce80ad6c4d15"
    :limit="1"
    :on-exceed="handleExceed"
    :auto-upload="false"
  >
    <template #trigger>
      <el-button type="primary">select file</el-button>
    </template>
    <el-button class="ml-3" type="success" @click="submitUpload">
      upload to server
    </el-button>
    <template #tip>
      <div class="el-upload__tip text-red">
        limit 1 file, new file will cover the old file
      </div>
    </template>
  </el-upload> -->
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
} from '../../api/folderController.js'
import { getGroup, getRuleByGroup } from '../../api/testDve.js'
import { genFileId } from 'element-plus'
import type { UploadInstance, UploadProps, UploadRawFile } from 'element-plus'
import { nextTick } from 'vue'

const directoryData = ref([])
const currentDirectoryData = ref([])
const directoryByGroupData = ref([])
const fileInfoData = ref([])
const fileInfoVisible = ref(false)
const fileRenameVisible = ref(false)
const fileRuleListData = ref([])
const userGroupData = ref([])
const directoryByApplicationData = ref([])

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

const fileRenameBody = ref({
  agentId: '5',
  folderId: '',
  name: '',
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
      getRuleByGroupBody.value.agentId = 5
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
    getDirectoryMethod()
    getDirectoryByGroupMethod()
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
    getDirectoryByGroupMethod()
    getDirectoryMethod()
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
const handleCellDoubleClick = async (row) => {
  console.log('row:', row)
  if (row.type === 'folder') {
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

const openFilenameBlock = (folderId) => {
  fileRenameVisible.value = true
  console.log('folderId:', folderId)
  fileRenameBody.value.folderId = folderId
}

const closeFileRenameBlock = async () => {
  await setFolderName(fileRenameBody.value)
  fileRenameVisible.value = false
}

const handleExceed: UploadProps['onExceed'] = (files) => {
  upload.value!.clearFiles()
  const file = files[0] as UploadRawFile
  file.uid = genFileId()
  upload.value!.handleStart(file)
}
</script>

<style></style>
