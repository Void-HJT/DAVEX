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
        <!-- <el-menu-item index="/locate" class="custom-menu-item">
          <el-icon><Management /></el-icon>
          <span class="centered-text">本地计算</span>
        </el-menu-item> -->
        <!-- <el-sub-menu index="/locate">
          <template #title>
            <el-icon><Management /></el-icon>
            <span class="centered-text">单机模拟</span>
          </template>
          <el-menu-item index="/locate/locateCompute">
            <el-icon><User /></el-icon>
            <span class="centered-text">创建任务</span>
          </el-menu-item>
          <el-menu-item index="/locate/locateTaskTable">
            <el-icon><EditPen /></el-icon>
            <span class="centered-text">查看任务</span>
          </el-menu-item>
        </el-sub-menu> -->
        <!-- <el-menu-item index="/mpc">
          <el-icon><Promotion /></el-icon>
          <span>多方计算</span>
        </el-menu-item> -->
        <!-- <el-sub-menu index="/mpc">
          <template #title>
            <el-icon><UserFilled /></el-icon>
            <span class="centered-text">多方计算</span>
          </template>
          <el-menu-item index="/mpc/mpC">
            <el-icon><User /></el-icon>
            <span class="centered-text">创建任务</span>
          </el-menu-item>
          <el-menu-item index="/mpc/taskTable">
            <el-icon><EditPen /></el-icon>
            <span class="centered-text">我的任务</span>
          </el-menu-item>
          <el-menu-item index="/mpc/jionTask">
            <el-icon><EditPen /></el-icon>
            <span class="centered-text">加入任务</span>
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="/user">
          <template #title>
            <el-icon><UserFilled /></el-icon>
            <span class="centered-text">个人中心</span>
          </template>
          <el-menu-item index="/user/userData">
            <el-icon><User /></el-icon>
            <span class="centered-text">服务器设置</span>
          </el-menu-item>
          <el-menu-item index="/user/mpcCode">
            <el-icon><EditPen /></el-icon>
            <span class="centered-text">编写MPC代码</span>
          </el-menu-item>
        </el-sub-menu> -->
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
        <el-sub-menu index="/datashare">
          <template #title>
            <el-icon><UserFilled /></el-icon>
            <span class="centered-text">数据共享</span>
          </template>
          <el-menu-item index="/datashare/fileTrans">
            <el-icon><EditPen /></el-icon>
            <span class="centered-text">文件传输</span>
          </el-menu-item>
          <el-menu-item index="/datashare/pSi">
            <el-icon><EditPen /></el-icon>
            <span class="centered-text">PSI</span>
          </el-menu-item>
          <el-menu-item index="/datashare/mPc">
            <el-icon><EditPen /></el-icon>
            <span class="centered-text">MPC</span>
          </el-menu-item>
          <el-menu-item index="/datashare/quEry">
            <el-icon><EditPen /></el-icon>
            <span class="centered-text">QUERY</span>
          </el-menu-item>
          <el-menu-item index="/datashare/comPare">
            <el-icon><EditPen /></el-icon>
            <span class="centered-text">比对</span>
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="/result">
          <template #title>
            <el-icon><UserFilled /></el-icon>
            <span class="centered-text">结果管理区</span>
          </template>
          <el-menu-item index="/result/fileTrans">
            <el-icon><EditPen /></el-icon>
            <span class="centered-text">文件传输</span>
          </el-menu-item>
          <el-menu-item index="/result/mPc">
            <el-icon><EditPen /></el-icon>
            <span class="centered-text">MPC/PSI</span>
          </el-menu-item>
          <el-menu-item index="/result/quEry">
            <el-icon><EditPen /></el-icon>
            <span class="centered-text">QUERY</span>
          </el-menu-item>
          <el-menu-item index="/result/comPare">
            <el-icon><EditPen /></el-icon>
            <span class="centered-text">比对</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <div class="header-text">DAVEX_center操作界面</div>
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
