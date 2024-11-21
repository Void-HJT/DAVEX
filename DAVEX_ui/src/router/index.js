import { createRouter, createWebHistory } from 'vue-router'
// import { useUserStore } from '@/stores'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      component: () => import('@/views/login/loginPage.vue'),
    }, //登陆页
    {
      path: '/',
      component: () => import('@/views/layout/newLayoutContainer.vue'),
      redirect: '/datashare/fileTrans',
      children: [
        {
          path: 'locate/locateCompute',
          component: () => import('@/views/locate/locateCompute.vue'),
        },
        {
          path: 'locate/locateTaskTable',
          component: () => import('@/views/locate/locateTaskTable.vue'),
        },
        {
          path: 'mpc/mpC',
          component: () => import('@/views/mpc/mpC.vue'),
        },
        {
          path: 'mpc/jionTask',
          component: () => import('@/views/mpc/jionTask.vue'),
        },
        {
          path: 'mpc/taskTable',
          component: () => import('@/views/mpc/taskTable.vue'),
        },
        {
          path: 'user/userdata',
          component: () => import('@/views/user/userData.vue'),
        },
        {
          path: 'user/mpcCode',
          component: () => import('@/views/user/mpcCode.vue'),
        },
        {
          path: 'dve/testTrans',
          component: () => import('@/views/dve/testTrans.vue'),
        },
        {
          path: 'dve/folderController',
          component: () => import('@/views/dve/folderController.vue'),
        },
        {
          path: 'datashare/comPare',
          component: () => import('@/views/datashare/comPare.vue'),
        },
        {
          path: 'datashare/fileTrans',
          component: () => import('@/views/datashare/fileTrans.vue'),
        },
        {
          path: 'datashare/mPc',
          component: () => import('@/views/datashare/newMpc.vue'),
        },
        {
          path: 'datashare/pSi',
          component: () => import('@/views/datashare/pSi.vue'),
        },
        {
          path: 'datashare/quEry',
          component: () => import('@/views/datashare/quEry.vue'),
        },
        {
          path: 'datashare/secureInfer',
          component: () => import('@/views/datashare/secureInfer.vue'),
        },
        {
          path: 'datashare/fLearning',
          component: () => import('@/views/datashare/fLearning.vue'),
        },
        {
          path: 'result/fileTrans',
          component: () => import('@/views/result/fileTrans.vue'),
        },
        {
          path: 'result/mPc',
          component: () => import('@/views/result/mPc.vue'),
        },
        {
          path: 'result/quEry',
          component: () => import('@/views/result/quEry.vue'),
        },
        {
          path: 'result/comPare',
          component: () => import('@/views/result/comPare.vue'),
        },
        {
          path: 'result/fL',
          component: () => import('@/views/result/fL.vue'),
        },
        {
          path: 'user/notification',
          component: () => import('@/views/user/notification.vue'),
        },
      ],
    }, //布局
  ],
})

//登录访问拦截
// router.beforeEach((to)=>{
//   const useStore = useUserStore()
//   if(!useStore.isLogin && to.path !== '/login'){
//     return '/login'
//   }
//   return true
// })

export default router
