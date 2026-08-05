<template>
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
        <el-popover
          placement="top-start"
          title="此处输入新文件夹名"
          :width="400"
          trigger="click"
        >
          <template #reference>
            <el-button class="default-button">在当前目录下新建文件夹</el-button>
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
              class="default-button"
              @click="createFolderMethod"
              calss="el-button mt-4"
              style="width: 100%"
            >
              新建文件夹
            </el-button>
          </el-form>
        </el-popover>
        <el-popover
            placement="top-start"
            title="此处选择需要上传的文件"
            :width="400"
            trigger="click"
        >
          <template #reference>
            <el-button class="default-button">上传文件</el-button>
          </template>
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
            <div style="margin-top: 10px">
              <el-button class="start-button" @click="submitUpload">
                保存到当前文件夹
              </el-button>
            </div>
            <template #tip>
              <div class="el-upload__tip text-red">
                限制1个文件，新文件将覆盖旧文件
              </div>
            </template>
          </el-upload>
        </el-popover>
      </div>
      <div>
        <el-table
          :data="filteredDirectoryData"
          @row-dblclick="handleCellDoubleClick"
          max-height="800"
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
                class="small-delete-button"
                @click="
                  deleteFolderMethod(
                    scope.row.uid,
                    scope.row.agentId,
                    scope.row.type,
                    scope.row.parentId,
                  )
                "
              >
                <el-icon><Delete /></el-icon> 删除
              </el-button>
              <el-button
                class="small-default-button"
                @click="
                  scope.row.type === 'folder'
                    ? openFolderRenameBlock(
                        scope.row.uid,
                        scope.row.agentId,
                        scope.row.name,
                      )
                    : openFileRenameBlockMethod(
                        scope.row
                      )
                "
              >
                <el-icon><Edit /></el-icon> 重命名
              </el-button>
<!--              <el-button-->
<!--                  v-if="scope.row.type === 'file'"-->
<!--                  class="small-default-button"-->
<!--                  @click="-->
<!--                  openFileFuncEditBlockMethod(-->
<!--                        scope.row-->
<!--                      )-->
<!--                "-->
<!--              >-->
<!--                <el-icon><Edit /></el-icon> 功能权限-->
<!--              </el-button>-->
<!--              <el-button-->
<!--                v-if="scope.row.type === 'file'"-->
<!--                link-->
<!--                type="primary"-->
<!--                @click="-->
<!--                  getFileInfoMethod(-->
<!--                    scope.row.uid,-->
<!--                    scope.row.agentId,-->
<!--                    scope.row.parentId,-->
<!--                    scope.row.type,-->
<!--                  )-->
<!--                "-->
<!--                size="small"-->
<!--              >-->
<!--                权限管理-->
<!--              </el-button>-->
<!--              <el-button-->
<!--                v-if="scope.row.type === 'folder'"-->
<!--                link-->
<!--                type="primary"-->
<!--                @click="-->
<!--                  (folderVisibleDialogVisible = true),-->
<!--                    (folderVisibleBody.folderId = scope.row.uid)-->
<!--                "-->
<!--                size="small"-->
<!--              >-->
<!--                可见性管理-->
<!--              </el-button>-->
            </template>
          </el-table-column>
        </el-table>
      </div>
<!--      <el-dialog v-model="fileInfoVisible" title="文件详细信息" width="60%">-->
<!--        <span-->
<!--          style="-->
<!--            display: block;-->
<!--            text-align: center;-->
<!--            font-size: 16px;-->
<!--            line-height: 3;-->
<!--          "-->
<!--        >-->
<!--          该文件拥有的权限-->
<!--        </span>-->
<!--        <el-table-->
<!--          :data="fileRuleListData"-->
<!--          style="width: 100%"-->
<!--          stripe-->
<!--          max-height="300"-->
<!--        >-->
<!--          <el-table-column-->
<!--            label="序号"-->
<!--            prop="uid"-->
<!--            width="80"-->
<!--            align="center"-->
<!--          ></el-table-column>-->
<!--          <el-table-column-->
<!--            label="所属代理"-->
<!--            prop="agentId"-->
<!--            width="80"-->
<!--            align="center"-->
<!--          ></el-table-column>-->
<!--          <el-table-column-->
<!--            label="代理名"-->
<!--            prop="agentName"-->
<!--            width="80"-->
<!--            align="center"-->
<!--          ></el-table-column>-->
<!--          <el-table-column-->
<!--            label="组别"-->
<!--            prop="groupId"-->
<!--            width="100"-->
<!--            align="center"-->
<!--          ></el-table-column>-->
<!--          <el-table-column-->
<!--            label="组别名"-->
<!--            prop="groupName"-->
<!--            width="180"-->
<!--            align="center"-->
<!--          ></el-table-column>-->
<!--          <el-table-column-->
<!--            label="拥有权限"-->
<!--            prop="allowedMethod"-->
<!--            min-width="180"-->
<!--            align="center"-->
<!--          ></el-table-column>-->
<!--        </el-table>-->
<!--        <span-->
<!--          style="-->
<!--            display: block;-->
<!--            text-align: center;-->
<!--            font-size: 16px;-->
<!--            line-height: 3;-->
<!--          "-->
<!--        >-->
<!--          更新文件权限-->
<!--        </span>-->
<!--        <div class="form-container">-->
<!--          <el-form-->
<!--            :model="setFileRuleBody"-->
<!--            style="max-width: 600px"-->
<!--            class="styled-form"-->
<!--          >-->
<!--            &lt;!&ndash; 第一个选择框：选择需要管理的分组 &ndash;&gt;-->
<!--            <el-form-item label="选择分组">-->
<!--              <el-select-->
<!--                v-model="selectedGroupUid"-->
<!--                placeholder="请选择需要管理的分组"-->
<!--                @change="updateGroupId"-->
<!--              >-->
<!--                <el-option-->
<!--                  v-for="item in userGroupData"-->
<!--                  :key="item.uid"-->
<!--                  :label="item.name"-->
<!--                  :value="item.uid"-->
<!--                />-->
<!--              </el-select>-->
<!--            </el-form-item>-->
<!--            &lt;!&ndash; 第二个选择框：选择对应的权限 &ndash;&gt;-->
<!--            <el-form-item label="选择权限">-->
<!--              <el-select-->
<!--                v-model="selectedAllowedMethod"-->
<!--                placeholder="请选择对应的权限"-->
<!--                @change="updateAllowedMethod"-->
<!--              >-->
<!--                <el-option-->
<!--                  v-for="method in currentAllowedMethods"-->
<!--                  :key="method"-->
<!--                  :label="method"-->
<!--                  :value="method"-->
<!--                />-->
<!--              </el-select>-->
<!--            </el-form-item>-->
<!--  -->
<!--            <el-form-item>-->
<!--              <el-button @click="setFileRuleMethod">赋予文件权限</el-button>-->
<!--              <el-button @click="deleteFileRuleMethod">移除文件权限</el-button>-->
<!--            </el-form-item>-->
<!--          </el-form>-->
<!--        </div>-->
<!--        <template #footer>-->
<!--          <span class="dialog-footer">-->
<!--            <el-button type="primary" @click="closeFileInfo">确认</el-button>-->
<!--          </span>-->
<!--        </template>-->
<!--      </el-dialog>-->

<!--      <el-dialog-->
<!--        v-model="folderVisibleDialogVisible"-->
<!--        title="可见性管理"-->
<!--        width="500"-->
<!--        :before-close="folderVisibleDialogClose"-->
<!--      >-->
<!--        <el-form-item label="选择分组">-->
<!--          <el-select-->
<!--            v-model="folderVisibleBody.groupId"-->
<!--            placeholder="请选择需要管理的分组"-->
<!--          >-->
<!--            <el-option-->
<!--              v-for="item in userGroupData"-->
<!--              :key="item.uid"-->
<!--              :label="item.name"-->
<!--              :value="item.uid"-->
<!--            />-->
<!--          </el-select>-->
<!--        </el-form-item>-->
<!--        <template #footer>-->
<!--          <div class="dialog-footer">-->
<!--            <el-button type="primary" @click="setFolderVisibleMethod">-->
<!--              设置该组可见该文件夹-->
<!--            </el-button>-->
<!--            <el-button type="primary" @click="setFolderInvisibleMethod">-->
<!--              设置该组不可见该文件夹-->
<!--            </el-button>-->
<!--          </div>-->
<!--        </template>-->
<!--      </el-dialog>-->

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
            <el-input v-model="fileInfo.name"></el-input>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button class="default-button" @click="closeFileRenameBlock">
              确认
            </el-button>
          </span>
        </template>
      </el-dialog>

      <el-dialog v-model="fileFuncEditVisible" title="文件功能权限管理" width="30%">
        <el-form :model="fileInfoBody" label-width="20%" style="max-width: 80%">
          <el-form-item label="功能权限">
            <el-checkbox-group v-model="selectedFuncTypes">
              <el-checkbox label="trans">文件传输</el-checkbox>
              <el-checkbox label="compare">数据比对</el-checkbox>
              <el-checkbox label="mpc">安全多方计算</el-checkbox>
              <el-checkbox label="psi">隐私集合求交</el-checkbox>
              <el-checkbox label="secureinfer">安全推理</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button class="default-button" @click="closeFileFuncEditBlock">
              确认
            </el-button>
          </span>
        </template>
      </el-dialog>
    </el-main>
  </el-container>

<!--  <el-container>-->
<!--    <el-header style="height: 50px">-->
<!--      <div-->
<!--        style="-->
<!--          background-color: antiquewhite;-->
<!--          height: 40px;-->
<!--          display: flex;-->
<!--          justify-content: center;-->
<!--          align-items: center;-->
<!--        "-->
<!--      >-->
<!--        <p-->
<!--          style="-->
<!--            font-size: 20px;-->
<!--            color: black;-->
<!--            opacity: 100%;-->
<!--            text-align: center;-->
<!--          "-->
<!--        >-->
<!--          条件筛选列表-->
<!--        </p>-->
<!--      </div>-->
<!--    </el-header>-->
<!--    <el-main>-->
<!--      <el-popover-->
<!--        placement="top-start"-->
<!--        title="按分组查看文件目录"-->
<!--        :width="400"-->
<!--        trigger="click"-->
<!--      >-->
<!--        <template #reference>-->
<!--          <el-button>按分组查看文件目录</el-button>-->
<!--        </template>-->
<!--        <el-form-->
<!--          :model="getDirectoryByGroupBody"-->
<!--          label-width="80px"-->
<!--          style="max-width: 600px"-->
<!--        >-->
<!--          <el-form-item label="选择分组">-->
<!--            <el-select-->
<!--              v-model="getDirectoryByGroupBody.groupId"-->
<!--              placeholder="请选择分组"-->
<!--            >-->
<!--              <el-option-->
<!--                v-for="item in userGroupData"-->
<!--                :key="item.uid"-->
<!--                :label="item.name"-->
<!--                :value="item.uid"-->
<!--              />-->
<!--            </el-select>-->
<!--          </el-form-item>-->
<!--          <el-form-item>-->
<!--            <el-button @click="getDirectoryByGroupMethod">-->
<!--              按照分组获取文件列表-->
<!--            </el-button>-->
<!--          </el-form-item>-->
<!--        </el-form>-->
<!--      </el-popover>-->
<!--      <el-popover-->
<!--        placement="top-start"-->
<!--        title="按用户查看文件目录"-->
<!--        :width="400"-->
<!--        trigger="click"-->
<!--      >-->
<!--        <template #reference>-->
<!--          <el-button>按用户查看文件目录</el-button>-->
<!--        </template>-->
<!--        <el-form-->
<!--          :model="getDirectoryByApplicationBody"-->
<!--          label-width="80px"-->
<!--          style="max-width: 600px"-->
<!--        >-->
<!--          <el-form-item label="选择用户">-->
<!--            <el-select-->
<!--              v-model="getDirectoryByApplicationBody.applicationId"-->
<!--              placeholder="请选择用户"-->
<!--            >-->
<!--              <el-option-->
<!--                v-for="item in applicationData"-->
<!--                :key="item.uid"-->
<!--                :label="item.name"-->
<!--                :value="item.uid"-->
<!--              />-->
<!--            </el-select>-->
<!--          </el-form-item>-->
<!--          <el-form-item>-->
<!--            <el-button @click="getDirectoryByApplicationMethod">-->
<!--              按照用户获取文件列表-->
<!--            </el-button>-->
<!--          </el-form-item>-->
<!--        </el-form>-->
<!--      </el-popover>-->
<!--      <el-table-->
<!--        stripe-->
<!--        :data="directorySelectedData"-->
<!--        style="width: 100%"-->
<!--        @row-dblclick="nextFileGroup"-->
<!--        max-height="300"-->
<!--      >-->
<!--        <el-table-column fixed label="" width="50" align="center">-->
<!--          <template #default="scope">-->
<!--            <el-icon>-->
<!--              <template v-if="scope.row.type === 'folder'">-->
<!--                <el-icon color="#409efc"><Folder /></el-icon>-->
<!--              </template>-->
<!--              <template v-else-if="scope.row.type === 'file'">-->
<!--                <el-icon><Files /></el-icon>-->
<!--              </template>-->
<!--            </el-icon>-->
<!--          </template>-->
<!--        </el-table-column>-->
<!--        <el-table-column-->
<!--          label="Uid"-->
<!--          prop="uid"-->
<!--          width="80"-->
<!--          align="center"-->
<!--        ></el-table-column>-->
<!--        <el-table-column-->
<!--          label="名称"-->
<!--          prop="name"-->
<!--          width="180"-->
<!--          align="center"-->
<!--        ></el-table-column>-->
<!--        <el-table-column-->
<!--          label="所属代理"-->
<!--          prop="agentId"-->
<!--          width="80"-->
<!--        ></el-table-column>-->

<!--        <el-table-column-->
<!--          label="类型"-->
<!--          prop="type"-->
<!--          width="180"-->
<!--          align="center"-->
<!--        ></el-table-column>-->
<!--        <el-table-column-->
<!--          label="创建时间"-->
<!--          prop="createDate"-->
<!--          width="380"-->
<!--          :formatter="formatDate"-->
<!--          align="center"-->
<!--        ></el-table-column>-->
<!--        <el-table-column-->
<!--          label="更新时间"-->
<!--          prop="lastUpdate"-->
<!--          min-width="380"-->
<!--          :formatter="formatDate"-->
<!--          align="center"-->
<!--        ></el-table-column>-->
<!--      </el-table>-->
<!--    </el-main>-->
<!--  </el-container>-->
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
  getCenterInfoByCenterId,
  getGroupInfoByGroupId,
  getAgentInfoByAgentId,
  getRoot
} from '../../api/folderController.js'
import { getGroup, getRuleByGroup, getApplication } from '../../api/testDve.js'
import { genFileId } from 'element-plus'
import type { UploadInstance, UploadProps, UploadRawFile } from 'element-plus'
import { nextTick, onMounted } from 'vue'

onMounted(async () => {
  await getRootMethod()
  getDirectoryMethod()
  // getGroups()
  // getApplicationMethod()
})

// 对话框是否可见
const fileRenameVisible = ref(false)
const folderVisibleDialogVisible = ref(false)
const fileFuncEditVisible = ref(false)

// 对话框关闭时的回调
const folderVisibleDialogClose = () => {
  folderVisibleDialogVisible.value = false
  folderVisibleBody.value.groupId = ''
}

const agentId = ref('')
const rootId = ref('')
const selectedGroupUid = ref(null)
const selectedAllowedMethod = ref(null)
const directoryData = ref([])
const applicationData = ref([])
const currentDirectoryData = ref([])
const fileInfoData = ref([])
const fileInfoVisible = ref(false)
const folderRenameVisible = ref(false)
const fileRuleListData = ref([])
const userGroupData = ref([])
const directorySelectedData = ref([])
const fileInfo = ref({
  uid: '',
  agentId: '',
  folderId: '',
  name: '1',
  createDate: '',
  lastUpdate: '',
  size: '',
  description: '',
  expiredTime: '',
  hash: '',
  example: '',
  type: '',
  attribute: '',
})
const updateFileBody = ref({
  fileInfo: fileInfo
})

const selectedFuncTypes = ref([])

const getApplicationMethod = async () => {
  const res = await getApplication()
  // 确保 res.data 是一个数组
  applicationData.value = res.data.data
  // 现在可以安全地调用 includes
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
  agentId: '',
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
  file: null as File | null,
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

const getCenterInfoByCenterIdBody = ref({
  centerId: '1',
})

const getAgentInfoByAgentIdBody = ref({
  agentId: '5',
})

const getGroupInfoByGroupIdBody = ref({
  groupId: '',
  agentId: '5',
  centerId: '1',
})

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
    if (res.data.data.ruleList != null) {
      for (let j = 0; j < res.data.data.ruleList.length; j++) {
        getGroupInfoByGroupIdBody.value.groupId =
          res.data.data.ruleList[j].groupId
        getAgentInfoByAgentIdBody.value.agentId =
          res.data.data.ruleList[j].agentId
        const gName = await getGroupInfoByGroupId(
          getGroupInfoByGroupIdBody.value,
        )
        const aName = await getAgentInfoByAgentId(
          getAgentInfoByAgentIdBody.value,
        )
        res.data.data.ruleList[j].agentName = aName.data.data.name
        res.data.data.ruleList[j].groupName = gName.data.data.name
      }
    }
    fileRuleListData.value = res.data.data.ruleList
  } catch (error) {
    console.error('Failed to set file rule:', error)
  }
}

const deleteFileRuleMethod = async () => {
  try {
    await deleteFileRule(setFileRuleBody.value)
    const res = await getFileInfo(getFileInfoBody.value)
    if (res.data.data.ruleList != null) {
      for (let j = 0; j < res.data.data.ruleList.length; j++) {
        getGroupInfoByGroupIdBody.value.groupId =
          res.data.data.ruleList[j].groupId
        getAgentInfoByAgentIdBody.value.agentId =
          res.data.data.ruleList[j].agentId
        const gName = await getGroupInfoByGroupId(
          getGroupInfoByGroupIdBody.value,
        )
        const aName = await getAgentInfoByAgentId(
          getAgentInfoByAgentIdBody.value,
        )
        res.data.data.ruleList[j].agentName = aName.data.data.name
        res.data.data.ruleList[j].groupName = gName.data.data.name
      }
    }
    fileRuleListData.value = res.data.data.ruleList
  } catch (error) {
    console.error('Failed to set file rule:', error)
  }
}

const getFileInfoMethod = async (uid, agentId, parentId, type) => {
  try {
    if (type === 'file') {
      setFileRuleBody.value.fileId = uid
      setFileRuleBody.value.agentId = agentId
      setFileRuleBody.value.folderId = parentId
      getFileInfoBody.value.uid = uid
      getFileInfoBody.value.agentId = agentId
      getFileInfoBody.value.folderId = parentId
      const res = await getFileInfo(getFileInfoBody.value)
      fileInfoData.value = [res.data.data]
      // 若rule不为空，则遍历rule，将每个rule中的uid添加到res[i].rule中
      if (res.data.data.ruleList != null) {
        for (let j = 0; j < res.data.data.ruleList.length; j++) {
          getGroupInfoByGroupIdBody.value.groupId =
            res.data.data.ruleList[j].groupId
          getAgentInfoByAgentIdBody.value.agentId =
            res.data.data.ruleList[j].agentId
          const gName = await getGroupInfoByGroupId(
            getGroupInfoByGroupIdBody.value,
          )
          const aName = await getAgentInfoByAgentId(
            getAgentInfoByAgentIdBody.value,
          )
          res.data.data.ruleList[j].agentName = aName.data.data.name
          res.data.data.ruleList[j].groupName = gName.data.data.name
        }
      }
      fileRuleListData.value = res.data.data.ruleList
      fileInfoVisible.value = true
      getGroups()
    }
  } catch (error) {
    console.error('Failed to get file info:', error)
  }
}

async function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms))
}

const upload = ref<UploadInstance>()

const handleExceed: UploadProps['onExceed'] = (files) => {
  upload.value!.clearFiles()
  const file = files[0] as UploadRawFile
  file.uid = genFileId()
  upload.value!.handleStart(file)
}

const handleFileChange: UploadProps['onChange'] = (file, fileList) => {
  uploadFileBody.value.file = file.raw
}

const submitUpload = async () => {
  uploadFileBody.value.agentId = agentId.value
  uploadFileBody.value.folderId = currentParentId.value
  await uploadFileMethod()
  findCurrentFolder()
}

const uploadFileMethod = async () => {
  try {
    console.log(uploadFileBody.value)
    const res = await uploadFile(uploadFileBody.value)
    console.log(res.data)
  }
  catch (error) {
    console.error('Failed to create MPC task:', error)
  }
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
    createFolderBody.value.agentId = agentId.value
    createFolderBody.value.parentId = currentParentId.value
    const res = await createFolder(createFolderBody.value)
    console.log(res)
    findCurrentFolder()
  } catch (error) {
    console.error('Failed to create folder:', error)
  }
}

const deleteFolderMethod = async (uid, agentId, type, parentId) => {
  try {
    let res

    if (type === 'folder') {
      // 删除文件夹。
      res = await deleteFolder({
        agentId,
        folderId: uid,
      })
    } else {
      // 删除文件。
      res = await deleteFile({
        agentId,
        fileId: uid,
        folderId: parentId,
      })
    }

    // HTTP 请求成功不代表业务操作成功。
    if (res.data.code === 0) {
      ElMessage.error(
        res.data.message ||
        (type === 'folder' ? '文件夹删除失败' : '文件删除失败'),
      )
      return
    }

    ElMessage.success(
      res.data.message ||
      (type === 'folder' ? '文件夹删除成功' : '文件删除成功'),
    )

    // 只有删除成功才刷新目录。
    await findCurrentFolder()
  } catch (error) {
    console.error('Failed to delete file or folder:', error)

    ElMessage.error(
      error?.response?.data?.message ||
      error?.message ||
      (type === 'folder' ? '文件夹删除失败' : '文件删除失败'),
    )
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
    directorySelectedData.value = row.children
  }
}

const getDirectoryBody = ref({
  rootId: rootId,
})

const getDirectoryByGroupBody = ref({
  rootId: rootId,
  agentId: '5',
  groupId: '',
})

const getDirectoryByApplicationBody = ref({
  rootId: rootId,
  agentId: '5',
  applicationId: '',
})

const folderVisibleBody = ref({
  agentId: '5',
  groupId: '',
  folderId: '',
})

const currentParentId = ref('-1')

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
    directorySelectedData.value = res1
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
    directorySelectedData.value = res1
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

const openFileRenameBlockMethod = async (row) => {
  fileRenameVisible.value = true
  // console.log('name:', name)
  // fileInfoBody.value.agentId = agentId
  // fileInfoBody.value.fileId = Uid
  // const res = await getFile(fileInfoBody.value)
  // console.log('res:', res.data.body.data)
  // fileInfo.value = res.data.body.data
  fileInfo.value.uid = row.uid
  fileInfo.value.agentId = row.agentId
  fileInfo.value.folderId = row.parentId
  fileInfo.value.name = row.name
  fileInfo.value.createDate = row.createDate
  fileInfo.value.lastUpdate = row.lastUpdate
  fileInfo.value.size = row.size
  fileInfo.value.description = row.description
  fileInfo.value.expiredTime = row.expiredTime
  fileInfo.value.hash = row.hash
  fileInfo.value.example = row.example
  fileInfo.value.type = row.fileType
  fileInfo.value.attribute = row.attribute
  console.log(fileInfo.value)
}

const closeFileRenameBlock = async () => {
  const res = await updateFile(updateFileBody.value)
  console.log(res)
  fileRenameVisible.value = false
  // getDirectoryMethod()
  findCurrentFolder()
}

const openFileFuncEditBlockMethod = async (row) => {
  fileFuncEditVisible.value = true
  fileInfo.value.uid = row.uid
  fileInfo.value.agentId = row.agentId
  fileInfo.value.folderId = row.parentId
  fileInfo.value.name = row.name
  fileInfo.value.createDate = row.createDate
  fileInfo.value.lastUpdate = row.lastUpdate
  fileInfo.value.size = row.size
  fileInfo.value.description = row.description
  fileInfo.value.expiredTime = row.expiredTime
  fileInfo.value.hash = row.hash
  fileInfo.value.example = row.example
  fileInfo.value.type = row.fileType
  fileInfo.value.attribute = row.attribute
  selectedFuncTypes.value = (row.fileType?.split(',') || []).map(s => s.trim())
  console.log(selectedFuncTypes.value)
  console.log(fileInfo.value)
}

const closeFileFuncEditBlock = async () => {
  fileInfo.value.type = selectedFuncTypes.value.join(',')
  const res = await updateFile(updateFileBody.value)
  console.log(res)
  fileFuncEditVisible.value = false
  findCurrentFolder()
}

const returnFrontDirectory = async () => {
  deleteFolderRoute()
  console.log('folderRoute:', folderRoute.value)
  findCurrentFolder()
}

// 计算属性，返回当前选中组的 allowedMethod 列表
const currentAllowedMethods = computed(() => {
  const selectedGroup = userGroupData.value.find(
    (group) => group.uid === selectedGroupUid.value,
  )
  if (selectedGroup && selectedGroup.rule) {
    // 将 allowedMethod 字符串按空格分割成数组
    return selectedGroup.rule.trim().split(' ')
  }
  return []
})

// 更新 setFileRuleBody.groupId
const updateGroupId = () => {
  setFileRuleBody.value.groupId = selectedGroupUid.value
  // 清空已选择的权限
  selectedAllowedMethod.value = null
  // 更新 allowedMethod 为空
  setFileRuleBody.value.allowedMethod = ''
}

// 更新 setFileRuleBody.allowedMethod
const updateAllowedMethod = () => {
  setFileRuleBody.value.allowedMethod = selectedAllowedMethod.value
}

const getRootMethod = async () => {
  const res = await getRoot()
  rootId.value = res.data.data.uid
  currentParentId.value = res.data.data.uid
  agentId.value = res.data.data.agentId
}

// 过滤后的数据：仅显示type为default的file，folder全部显示
const filteredDirectoryData = computed(() => {
  return directoryData.value.filter(item => {
    // 文件夹(folder)全部保留，文件(file)仅保留type为default的
    if (item.type === 'folder') {
      return true;
    } else if (item.type === 'file') {
      return item.fileType === 'default';
    }
    return false;
  });
});
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
