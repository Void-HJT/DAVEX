<template>
  <!-- 全局限制消息数量，并合并短时间内重复出现的相同消息。 -->
  <el-config-provider :message="messageConfig">
    <router-view></router-view>
  </el-config-provider>
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue'
import { useNotificationStore } from './stores/index'
// 最多同时显示3条消息，相同内容合并，3秒后自动关闭。
const messageConfig = {
  max: 3,
  grouping: true,
  duration: 3000,
}

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
  padding: 5px 10px;
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

.el-table {
  border: 1px solid #a9c4df !important; /* 外边框 */
  border-left: none !important; /* 去掉左边框 */
  border-right: none !important; /* 去掉右边框 */
  margin-top: 10px;
}
/* 表头 */
.el-table th {
  color: #2D405E;
  font-weight: bold;
  height: 40px;
}
/* 单元格 */
.el-table td {
  border-top: 1px solid #a9c4df !important; /* 单元格顶部边框 */
  color: #4f5e7b; /* 单元格文字颜色 */
  height: 40px;
}
/* 列表行间隔颜色 */
.el-table .el-table__row:nth-child(odd) {
  background-color: #F3F6FB;
}
.el-table .el-table__row:nth-child(even) {
  background-color: #FFFFFF;
}
/* 强制应用悬停背景色 */
.el-table .el-table__body-wrapper tr:hover > td {
  background-color: #fffdec !important;
}

.el-dialog {
  border-radius: 5px !important; /* 对话框圆角 */
  overflow: hidden; /* 防止内容溢出影响圆角效果 */
}
.el-dialog .el-dialog__title {
  padding: 0 15px; /* 左右留出适当间距 */
  color: #ffffff; /* 标题文字颜色 */
  line-height: 40px; /* 居中对齐 */
  font-size: 16px;
}
.el-dialog .el-dialog__header {
  height: 40px; /* 标题高度 */
  background-color: #1b9cff; /* 标题背景色 */
  padding: 0; /* 移除默认的内边距 */
  width: 100%; /* 确保标题宽度填满 */
  box-sizing: border-box; /* 确保宽度计算包括边框 */
}
.el-dialog__headerbtn {
  top: 0 !important;
  height: 40px !important;
  width: 40px !important;
}
.el-dialog__headerbtn i {
  position: absolute; /* 确保按钮浮动在右侧 */
  right: 6px;
  top: 10px;
  color: white !important;
}
.el-dialog__headerbtn:hover {
  background-color: #f5b923 !important; /* 悬停时背景颜色 */
}

.el-input {
  border-radius: 3px;
  border: 1px solid #a9c4df;
}
.el-input__inner {
  color: #4f5e7b !important;
}
.el-input__inner::placeholder {
  color: #bac9df !important;
}
.el-input:hover {
  border-color: #879bba; /* 鼠标悬浮时改变边框颜色 */
}
.el-input.is-focus .el-input__inner {
  border-color: #4084f0;  /* 聚焦时的边框颜色 */
}

.el-pagination .el-pager .number.is-active {
  background-color: #4084f0 !important; /* 选中页码的背景颜色 */
}
.el-pagination .el-pager .number:hover, .btn:hover {
  border: 1px solid #4084f0;
  color: #4084f0;         /* 悬停时的文字颜色 */
  background-color: #ebf4fe !important; /* 悬停时的背景颜色 */
}
.el-pagination .el-pager .number.is-active:hover {
  background-color: #4084f0 !important; /* 悬停时选中页码仍保持选中样式 */
}
.el-pagination .el-pager .number, .btn-prev, .btn-next {
  width: 28px !important;
  height: 28px !important;
  border: 1px solid #a9c4df !important;
}
.el-pagination .el-pager .number:not(:last-child) {
  margin-right: 6px;  /* 设置按钮之间的间距 */
}

::-webkit-scrollbar {
  width: 8px; /* 滚动条宽度 */
  height: 8px; /* 横向滚动条高度 */
}
::-webkit-scrollbar-thumb {
  background-color: rgba(120, 161, 203, 0.5); /* 默认颜色（78a1cb），透明度50% */
  border-radius: 4px; /* 圆角 */
}
::-webkit-scrollbar-thumb:hover {
  background-color: #78a1cb; /* 鼠标悬停时不透明 */
}

.el-notification {
  border-radius: 5px !important; /* 圆角设置 */
  width: 400px !important;
  height: 200px;
}
.el-notification .el-notification__group {
  margin: 0 !important;
}
.el-notification__content {
  line-height: 1.5 !important; /* 内容行高设置为 1.5 倍 */
}
.el-notification .el-notification__title {
  padding: 0;
  color: #5c79b0; /* 标题文字颜色 */
  font-size: 14px; /* 字体大小 */
  font-weight: normal;
  margin-bottom: 25px;
}
.el-notification__closeBtn {
  top: 0 !important;
  right: 0 !important;
  height: 40px !important;
  width: 40px !important;
  color: #5c79b0 !important; /* 按钮颜色 */
}
.el-notification__closeBtn:hover {
  color: #f86359 !important; /* 悬停变为红色 */
}
.notification-content {
  display: inline-flex;
  align-items: center;
}
.button-container {
  position: absolute;
  right: 15px; /* 距离右边 10px */
  bottom: 15px; /* 距离底部 10px */
}
.notification-icon {
  color: #e6a23c;
  width: 50px;
  margin-right: 10px;
  margin-left: 30px;
}
.notification-icon svg {
  width: 100%;
  height: 100%;
  vertical-align: middle;
}
</style>
