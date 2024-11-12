import { defineStore } from 'pinia'
import { ElNotification } from 'element-plus'
import { queryUnread } from '../../api/notification.js'
import router from '../../router'

export const useNotificationStore = defineStore('notification', {
    state: () => ({
        intervalId: null,
    }),
    actions: {
        startPolling(applicationId) {
            if (this.intervalId) return // 防止重复启动
            this.intervalId = setInterval(async () => {
                try {
                    const res = await queryUnread({ applicationId })
                    if (res.data === true) {
                        ElNotification({
                            title: '提示',
                            message: `<span>您有新的未读消息 <button class="el-notification__btn">查看</button></span>`,
                            type: 'info',
                            duration: 5000,
                            dangerouslyUseHTMLString: true,
                            onClose: () => {
                                // 当通知关闭时可以执行一些额外操作
                            }
                        })
                        // 添加点击事件监听器
                        setTimeout(() => {
                            const btn = document.querySelector('.el-notification__btn')
                            if (btn) {
                                btn.addEventListener('click', () => {
                                    router.push('/user/notification')
                                })
                            }
                        }, 0)
                    }
                } catch (error) {
                    console.error('轮询未读消息失败:', error)
                }
            }, 10000)
        },
        stopPolling() {
            clearInterval(this.intervalId)
            this.intervalId = null
        }
    }
})
