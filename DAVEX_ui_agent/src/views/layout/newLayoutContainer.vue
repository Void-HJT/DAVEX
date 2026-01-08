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
      <div class="header-text">隐私计算平台 (DAVEX) - Agent 操作界面</div>
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
          <el-menu-item index="/dve/folderController">
            <el-icon><Files /></el-icon>
            <span class="centered-text">一般文件管理</span>
          </el-menu-item>
          <el-menu-item index="/dve/privateFolderController">
            <el-icon><Files /></el-icon>
            <span class="centered-text">隐私文件管理</span>
          </el-menu-item>
<!--          <el-sub-menu index="/dve">-->
<!--            <template #title>-->
<!--              <el-icon><UserFilled /></el-icon>-->
<!--              <span class="centered-text">用户与文件管理</span>-->
<!--            </template>-->
<!--            <el-menu-item index="/dve/testTrans">-->
<!--              <el-icon><User /></el-icon>-->
<!--              <span class="centered-text">用户管理</span>-->
<!--            </el-menu-item>-->
<!--            <el-menu-item index="/dve/folderController">-->
<!--              <el-icon><User /></el-icon>-->
<!--              <span class="centered-text">文件管理</span>-->
<!--            </el-menu-item>-->
<!--          </el-sub-menu>-->
<!--          <el-sub-menu index="/auth">-->
<!--            <template #title>-->
<!--              <el-icon><UserFilled /></el-icon>-->
<!--              <span class="centered-text">认证管理</span>-->
<!--            </template>-->
<!--            <el-menu-item index="/auth/tokens">-->
<!--              <el-icon><EditPen /></el-icon>-->
<!--              <span class="centered-text">Token管理</span>-->
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
  margin-bottom: 10px;
}
.header-text {
  background-color: #165ac6;
  color: white;
  font-size: 24px;
  padding: 10px;
  font-weight: bold;
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