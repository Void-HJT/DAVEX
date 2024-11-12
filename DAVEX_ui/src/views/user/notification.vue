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
          消息列表
        </p>
      </div>
    </el-header>
    <el-main>
      <el-checkbox v-model="onlyUnread" @change="filterNotifications">仅显示未读消息</el-checkbox>
      <el-table :data="notifications" style="width: 100%">
        <el-table-column label="时间" width="300">
          <template #default="scope">
            <div style="display: flex; align-items: center">
              <el-icon><timer /></el-icon>
              <span style="margin-left: 10px">{{ formatDate(scope.row.time) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="标题" width="300">
          <template #default="scope">
            <el-popover effect="light" trigger="hover" placement="top" width="auto">
              <template #default>
                <div>内容: {{ scope.row.content }}</div>
              </template>
              <template #reference>
                <el-tag>{{ scope.row.title }}</el-tag>
              </template>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="300">
          <template #default="scope">
            <div style="display: flex; align-items: center">
              <el-icon v-if="scope.row.hasRead === true"><MuteNotification /></el-icon>
              <el-icon v-if="scope.row.hasRead === false"><Bell /></el-icon>
              <span style="margin-left: 10px">{{ scope.row.hasRead ? '已读' : '未读' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="scope">
            <el-button size="small" @click="readMethod(scope.row.uid, scope.row.content);readNotificationVisible=true">
              查看
            </el-button>
            <el-button
                size="small"
                type="info"
                @click="handleIgnore(scope.row.uid, scope.row.content)"
            >
              忽略
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <el-pagination
          background
          layout="prev, pager, next"
          :current-page="computedPage"
          :page-size="pageSize"
          :total="totalNotifications"
          @current-change="handlePageChange"
          style="margin-top: 20px;"
      />
    </el-main>
  </el-container>

  <el-dialog v-model="readNotificationVisible" title="消息内容" width="30%">
    <span>{{ notificationContent }}</span>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="readNotificationVisible=false;getNotificationMethod(1)">确定</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import {onMounted, ref, computed} from "vue";
import {queryUnread, getNotification, read, getUnreadNotification} from "../../api/notification.js"
import {MuteNotification} from "@element-plus/icons-vue";

onMounted(() => {
  getNotificationMethod(currentPage.value)
})

// 对话框是否可见
const readNotificationVisible = ref(false)
const notificationContent = ref('')

const pageSize = ref(20)
const currentPage = ref(1)
const unreadCurrentPage = ref(1)

const onlyUnread = ref(false)

const applicationId = "DAVEX-C1-A1"
const notifications = ref([])
const totalNotifications = ref(null)
const getNotificationBody = ref({
  applicationId: applicationId,
  page: null,
  size: 20
})
const readBody = ref({
  notificationId: ''
})

const computedPage = computed(() => {
  return onlyUnread.value ? unreadCurrentPage.value : currentPage.value
})

const getNotificationMethod = async (page) => {
  try {
    getNotificationBody.value.page = page
    console.log(getNotificationBody.value)
    const res = await getNotification(getNotificationBody.value);
    console.log(res)
    notifications.value = res.data.records
    totalNotifications.value = res.data.total
  }
  catch (error) {
    console.error('Failed to get notifications:', error)
  }
}

const getUnreadNotificationMethod = async (page) => {
  try {
    getNotificationBody.value.page = page
    console.log(getNotificationBody.value)
    const res = await getUnreadNotification(getNotificationBody.value);
    console.log(res)
    notifications.value = res.data.records
    totalNotifications.value = res.data.total
  }
  catch (error) {
    console.error('Failed to get notifications:', error)
  }
}

const handlePageChange = (page) => {
  if (onlyUnread.value) {
    unreadCurrentPage.value = page
    getUnreadNotificationMethod(page)
  }
  else {
    currentPage.value = page
    getNotificationMethod(page)
  }
}

const handleIgnore = (notificationId, content) => {
  readMethod(notificationId, content).then(() => {
    if (onlyUnread.value) {
      getUnreadNotificationMethod(unreadCurrentPage.value);
    } else {
      getNotificationMethod(currentPage.value);
    }
  });
}

const readMethod = async (notificationId, content) => {
  try {
    notificationContent.value = content
    readBody.value.notificationId = notificationId
    const res = await read(readBody.value)
    console.log(res)
  }
  catch (error) {
    console.error('Failed to read:', error)
  }
}

const filterNotifications = () => {
  if (onlyUnread.value) {
    getUnreadNotificationMethod(unreadCurrentPage)
  } else {
    getNotificationMethod(currentPage)
  }
}

const formatDate = (cellValue) => {
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
</script>

<style scoped></style>
