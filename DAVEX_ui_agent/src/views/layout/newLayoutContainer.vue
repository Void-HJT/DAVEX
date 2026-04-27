<script setup>
import {
  Management,
  UserFilled,
  User,
  EditPen,
  SwitchButton,
  CaretBottom,
  Files,
} from '@element-plus/icons-vue' //crop
// import avatar from '@/assets/default.png'
import { useUserStore } from '../../stores'
import { useRouter } from 'vue-router'
const router = useRouter()
const useStore = useUserStore()
const handleCommand = (key) => {
  if (key == 'logout') {
    useStore.removeToken()
    useStore.isLogin = false
    router.push('/login')
    console.log('logout')
  } else {
    console.log(key)
  }
}
</script>

<template>
  <el-container class="layout-container">
    <el-header class="header">
<!--      <div>-->
<!--        <img src="@/assets/DAVEX.png" class="logo-img" />-->
<!--      </div>-->
<!--      <div class="header-text">隐私计算平台 (DAVEX) - Agent 操作界面</div>-->
      <div class="header-text">面向法律监督的隐私计算平台（Agent）</div>
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
          <el-sub-menu index="/dve">
            <template #title>
              <el-icon><UserFilled /></el-icon>
              <span class="centered-text">用户与文件管理</span>
            </template>
            <el-menu-item index="/dve/testTrans">
              <el-icon><User /></el-icon>
              <span class="centered-text">用户管理</span>
            </el-menu-item>
            <el-menu-item index="/dve/folderController">
              <el-icon><User /></el-icon>
              <span class="centered-text">文件管理</span>
            </el-menu-item>
            <el-menu-item index="/dve/privateFolderController">
              <el-icon><Files /></el-icon>
              <span class="centered-text">隐私文件管理（用于类案类判）</span>
            </el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="/auth">
            <template #title>
              <el-icon><UserFilled /></el-icon>
              <span class="centered-text">认证管理</span>
            </template>
            <el-menu-item index="/auth/tokens">
              <el-icon><EditPen /></el-icon>
              <span class="centered-text">Token管理</span>
            </el-menu-item>
          </el-sub-menu>
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