<script setup>

import {
  Document,
  EditPen,
  FolderOpened,
  Message,
  MessageBox,
  Promotion,
  User,
  UserFilled
} from "@element-plus/icons-vue";
</script>

<template>
  <el-container class="layout-container">
    <el-header class="header">
<!--      <div>-->
<!--        <img src="@/assets/DAVEX.png" class="logo-img" />-->
<!--      </div>-->
      <div class="header-text">跨部门类案类判平台 - Center 操作界面</div>
      <div class="user-info">您好，C1-A1</div>
    </el-header>
    <el-container>
      <el-aside style="background-color: #010927; margin-right: 10px">
        <el-menu
            active-text-color="#fff"
            active-text-background
            background-color="#010927"
            :default-active="$route.path"
            text-color="#bac9df"
            router
        >
<!--          <el-menu-item index="/user/notification">-->
<!--            <el-icon><Message /></el-icon>-->
<!--            <span class="centered-text">消息中心</span>-->
<!--          </el-menu-item>-->
          <el-menu-item index="/user/operationHistory">
            <el-icon><Message /></el-icon>
            <span class="centered-text">历史记录</span>
          </el-menu-item>
          <el-sub-menu index="/dve">
            <template #title>
              <el-icon><Menu /></el-icon>
              <span class="centered-text">文件管理</span>
            </template>
<!--            <el-menu-item index="/dve/testTrans">-->
<!--              <el-icon><User /></el-icon>-->
<!--              <span class="centered-text">用户管理</span>-->
<!--            </el-menu-item>-->
            <el-menu-item index="/dve/folderController">
              <el-icon><FolderOpened /></el-icon>
              <span class="centered-text">一般文件管理</span>
            </el-menu-item>
            <el-menu-item index="/user/mpcFile">
              <el-icon><Document /></el-icon>
              <span class="centered-text">隐私文件管理</span>
            </el-menu-item>
          </el-sub-menu>
<!--          <el-sub-menu index="/datashare">-->
<!--            <template #title>-->
<!--              <el-icon><Promotion /></el-icon>-->
<!--              <span class="centered-text">数据流通</span>-->
<!--            </template>-->
<!--            <el-menu-item index="/datashare/fileTrans">-->
<!--              <el-icon><EditPen /></el-icon>-->
<!--              <span class="centered-text">文件传输</span>-->
<!--            </el-menu-item>-->
<!--            <el-menu-item index="/datashare/quEry">-->
<!--              <el-icon><EditPen /></el-icon>-->
<!--              <span class="centered-text">查询</span>-->
<!--            </el-menu-item>-->
<!--            <el-menu-item index="/datashare/comPare">-->
<!--              <el-icon><EditPen /></el-icon>-->
<!--              <span class="centered-text">比对</span>-->
<!--            </el-menu-item>-->
<!--            <el-menu-item index="/datashare/pSi">-->
<!--              <el-icon><EditPen /></el-icon>-->
<!--              <span class="centered-text">隐私集合求交</span>-->
<!--            </el-menu-item>-->
<!--            <el-menu-item index="/datashare/mPc">-->
<!--              <el-icon><EditPen /></el-icon>-->
<!--              <span class="centered-text">自定义安全多方计算</span>-->
<!--            </el-menu-item>-->
<!--            <el-menu-item index="/datashare/secureInfer">-->
<!--              <el-icon><EditPen /></el-icon>-->
<!--              <span class="centered-text">安全推理</span>-->
<!--            </el-menu-item>-->
<!--            <el-menu-item index="/datashare/fLearning">-->
<!--              <el-icon><EditPen /></el-icon>-->
<!--              <span class="centered-text">联邦学习</span>-->
<!--            </el-menu-item>-->
<!--          </el-sub-menu>-->
<!--          <el-sub-menu index="/result">-->
<!--            <template #title>-->
<!--              <el-icon><MessageBox /></el-icon>-->
<!--              <span class="centered-text">结果管理区</span>-->
<!--            </template>-->
<!--            <el-menu-item index="/result/fileTrans">-->
<!--              <el-icon><Document /></el-icon>-->
<!--              <span class="centered-text">文件传输</span>-->
<!--            </el-menu-item>-->
<!--            <el-menu-item index="/result/quEry">-->
<!--              <el-icon><Document /></el-icon>-->
<!--              <span class="centered-text">查询</span>-->
<!--            </el-menu-item>-->
<!--            <el-menu-item index="/result/comPare">-->
<!--              <el-icon><Document /></el-icon>-->
<!--              <span class="centered-text">比对</span>-->
<!--            </el-menu-item>-->
<!--            <el-menu-item index="/result/mPc">-->
<!--              <el-icon><Document /></el-icon>-->
<!--              <span class="centered-text">安全多方计算</span>-->
<!--            </el-menu-item>-->
<!--            <el-menu-item index="/result/fL">-->
<!--              <el-icon><Document /></el-icon>-->
<!--              <span class="centered-text">联邦学习</span>-->
<!--            </el-menu-item>-->
<!--          </el-sub-menu>-->
          <el-sub-menu index="/verdict">
            <template #title>
              <el-icon><Promotion /></el-icon>
              <span class="centered-text">案件文本流通模块</span>
            </template>
            <el-menu-item index="/verdict/transfer">
              <el-icon><EditPen /></el-icon>
              <span class="centered-text">案件文本直接获取</span>
            </el-menu-item>
            <el-menu-item index="/verdict/query">
              <el-icon><EditPen /></el-icon>
              <span class="centered-text">隐私保护类案查询</span>
            </el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="/verdictResult">
            <template #title>
              <el-icon><Promotion /></el-icon>
              <span class="centered-text">案件文本流通结果</span>
            </template>
            <el-menu-item index="/verdictResult/transfer">
              <el-icon><EditPen /></el-icon>
              <span class="centered-text">案件文本直接获取</span>
            </el-menu-item>
            <el-menu-item index="/verdictResult/query">
              <el-icon><EditPen /></el-icon>
              <span class="centered-text">隐私保护类案查询</span>
            </el-menu-item>
          </el-sub-menu>
<!--          <el-sub-menu index="/auth">-->
<!--            <template #title>-->
<!--              <el-icon><UserFilled /></el-icon>-->
<!--              <span class="centered-text">认证管理</span>-->
<!--            </template>-->
<!--            <el-menu-item index="/auth/tokens">-->
<!--              <el-icon><EditPen /></el-icon>-->
<!--              <span class="centered-text">Token管理</span>-->
<!--            </el-menu-item>-->
<!--            <el-menu-item index="/auth/userManagement">-->
<!--              <el-icon><EditPen /></el-icon>-->
<!--              <span class="centered-text">认证服务用户管理</span>-->
<!--            </el-menu-item>-->
<!--          </el-sub-menu>-->
        </el-menu>
      </el-aside>
      <el-main>
        <router-view></router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;
}
.header {
  height: 80px;
  background-color: #165ac6;
  display: flex;
  align-items: center;
  justify-content: space-between; // 新增这一行，实现左右对齐
  margin-bottom: 10px;
  padding: 0 20px; // 可选：添加左右内边距，避免文字贴边
}
.header-text {
  background-color: #165ac6;
  color: white;
  font-size: 24px;
  padding: 10px;
  font-weight: bold;
}
.user-info {
  color: white; // 和标题同色，保持视觉统一
  font-size: 18px; // 字号略小于标题，区分层级
  font-weight: 500;
}
.logo-img {
  width: 120px;
  height: auto;
}
.el-menu-item.is-active {
  color: #fff;
  background-color: #4084f0;
}
</style>