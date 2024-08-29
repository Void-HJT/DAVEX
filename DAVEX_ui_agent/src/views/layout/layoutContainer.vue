<script setup>
import {
  Management,
  UserFilled,
  User,
  EditPen,
  SwitchButton,
  CaretBottom,
} from '@element-plus/icons-vue' //crop
import avatar from '@/assets/default.png'
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
    <el-aside width="270px">
      <div class="el-aside__logo"></div>
      <el-menu
        active-text-color="#409eff"
        background-color="#304156"
        :default-active="$route.path"
        text-color="#fff"
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
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <div class="header-text">DAVEX_agent操作界面</div>
        <!-- <el-dropdown placement="bottom-end" @command = "handleCommand">
          <span class="el-dropdown__box">
            <el-avatar :src="avatar" />
            <el-icon><CaretBottom /></el-icon>
          </span>
          <template #dropdown >
            <el-dropdown-menu >
              <el-dropdown-item command="logout" :icon="SwitchButton"
                >退出登录</el-dropdown-item
              >
            </el-dropdown-menu>
          </template>
        </el-dropdown> -->
      </el-header>
      <el-main>
        <router-view></router-view>
      </el-main>
      <el-footer>DAVEX</el-footer>
    </el-container>
  </el-container>
</template>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;

  .el-aside {
    background-color: #304156;
    &__logo {
      height: 150px;
      background: url('@/assets/logo2.png') no-repeat center / 120px auto;
    }
    .el-menu {
      justify-content: center;
      // border-right:;
    }
  }
  .el-header {
    background-color: #fff;
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 150px;
    .el-dropdown__box {
      display: flex;
      align-items: center;
      .el-icon {
        color: #999;
        margin-left: 10px;
      }

      &:active,
      &:focus {
        outline: none;
      }
    }
  }
  .el-footer {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    color: #666;
  }
}
.centered-container {
  display: flex;
  justify-content: center; /* 水平居中 */
  // align-items: center; /* 垂直居中 */
  // height: 100vh; /* 可以根据需要设置容器的高度 */
}
.centered-text {
  text-align: center; /* 文本水平居中 */
  // display: inline-block; /* 使文字垂直居中生效 */
  font-size: 25px; /* 调整字体大小 */
  background-color: #304156; /* 可选：添加背景颜色以突出显示文字位置 */
  //设置鼠标悬浮时的颜色
  // &:hover {
  //   background-color: #263445;
  // }
  //设置字体为黑体
  font-family: 'SimHei', sans-serif;
}
.header-text {
  width: 100%;
  text-align: center; /* 文本水平居中 */
  // display: inline-block; /* 使文字垂直居中生效 */
  font-size: 50px; /* 调整字体大小 */
  background-color: #fff; /* 可选：添加背景颜色以突出显示文字位置 */
  font-family: 'SimSun', sans-serif; /* 设置为微软雅黑字体 */
}
.custom-menu-item {
  height: 200; /* 设置高度为 40px */
}
</style>
