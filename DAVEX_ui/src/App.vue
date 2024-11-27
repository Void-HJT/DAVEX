<template>
  <router-view></router-view>
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue'
import { useNotificationStore } from './stores/index'

const applicationId = 'DAVEX-C1-A1'
const notificationStore = useNotificationStore()

onMounted(() => {
  notificationStore.startPolling(applicationId)
})

onUnmounted(() => {
  notificationStore.stopPolling()
})
</script>

<style>
body {
  font-family: "Microsoft YaHei", Arial, sans-serif;
  font-size: 16px;
}

.el-select {
  font-size: 16px;
  border-radius: 3px;
  height: 34px; /* 边框高度 */
  background-color: #f9f9f9;
  width: 240px;
  margin-bottom: 10px;
}
/* 提示文字颜色 */
.el-select .el-input__inner::placeholder {
  color: #bac9df;
}
/* 输入框样式 */
.el-select .el-input__inner {
  height: 34px;
  line-height: 34px;
  //padding: 5px 10px;
  border-radius: 3px;
}
/* 悬停选项背景颜色和文字颜色 */
.el-select-dropdown .el-select-dropdown__item:hover {
  background-color: #ebf4fe; /* 悬停背景颜色 */
  color: #4084f0; /* 悬停文字颜色 */
}
/* 激活选中项的样式 */
.el-select-dropdown .el-select-dropdown__item.selected {
  background-color: #ebf4fe;
  color: #4084f0;
}

.custom-header {
  background: linear-gradient(to right, #005bd8, #65bfff); /* 渐变背景 */
  color: white;
  height: 40px !important;
  display: flex;
  align-items: center;
  padding-left: 20px;
  font-size: 18px;
}

.el-icon {
  margin-right: 5px; /* 图标与文字的间距 */
}

.icon-text {
  display: flex;
  align-items: center;
}

.el-button {
  border-radius: 3px !important;
}
.default-button {
  background-color: #1b9cff !important;
  color: white !important; /* 按钮文字颜色 */
  border: none !important;
  min-width: 110px;
  height: 34px !important;
}
.default-button:hover {
  background-color: rgba(255, 255, 255, 0.2); /* 鼠标移入叠加颜色 */
  box-shadow: inset 0 0 0 1000px rgba(255, 255, 255, 0.2); /* 添加透明叠加 */
}
.close-button {
  background-color: #c4dcf4 !important;
  color: #5c79b0 !important; /* 按钮文字颜色 */
  border: none !important;
  min-width: 110px;
  height: 34px !important;
}
.close-button:hover {
  background-color: rgba(255, 255, 255, 0.2); /* 鼠标移入叠加颜色 */
  box-shadow: inset 0 0 0 1000px rgba(255, 255, 255, 0.2); /* 添加透明叠加 */
}
.next-button {
  background-color: #1dc5b3 !important;
  color: white !important; /* 按钮文字颜色 */
  border: none !important;
  min-width: 110px;
  height: 34px !important;
}
.next-button:hover {
  background-color: rgba(255, 255, 255, 0.2); /* 鼠标移入叠加颜色 */
  box-shadow: inset 0 0 0 1000px rgba(255, 255, 255, 0.2); /* 添加透明叠加 */
}
.start-button {
  background-color: #ebf4fe !important;
  color: #5c79b0 !important; /* 按钮文字颜色 */
  border: 1px solid #a9c4df !important;
  min-width: 110px;
  height: 34px !important;
}
.start-button:hover {
  background-color: #1b9cff !important; /* 鼠标移入叠加颜色 */
  color: white !important;
}
.small-default-button {
  padding: 5px 6px !important; /* 上下5px，左右6px */
  align-items: center !important;
  justify-content: center !important;
  color: #4084f0 !important; /* 默认文字和图标颜色 */
  background-color: transparent !important;
  border: none !important;
  height: 24px !important;
}
.small-default-button:hover {
  background-color: #ebf4fe !important;
  border: 1px solid #a9c4df !important;
}
.small-delete-button {
  padding: 5px 6px !important; /* 上下5px，左右6px */
  align-items: center !important;
  justify-content: center !important;
  color: #f86359 !important; /* 默认文字和图标颜色 */
  background-color: transparent !important;
  border: none !important;
  height: 24px !important;
}
.small-delete-button:hover {
  background-color: #ffeceb !important;
  border: 1px solid #f86359 !important;
}
.small-default-button-disabled {
  padding: 5px 6px !important; /* 上下5px，左右6px */
  align-items: center !important;
  justify-content: center !important;
  color: #cad0d7 !important; /* 默认文字和图标颜色 */
  background-color: transparent !important;
  border: none !important;
  height: 24px !important;
}
.small-default-button-disabled:hover {
  color: white !important;
  background-color: #cad0d7 !important;
  border: none !important;
}
</style>
