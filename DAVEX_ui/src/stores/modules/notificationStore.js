import { defineStore } from 'pinia'
import { ElNotification } from 'element-plus'
import { queryUnread } from '../../api/notification.js'
import router from '../../router'
import {defineComponent, h} from "vue";
import {WarningFilled} from "@element-plus/icons-vue";

export default defineComponent({
    components: {WarningFilled}
})


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
                        const notification = ElNotification({
                            title: '消息提示',
                            duration: 5000,
                            position: 'bottom-right',
                            message: h('div', { class: 'notification-content' }, [
                                h('el-icon', { class: 'notification-icon', style: 'font-size: 24px;' }, [
                                    h(WarningFilled)
                                ]),
                                h('span', { }, '您有新的未读消息。'),
                                h('div', { class: 'button-container' }, [
                                    h(
                                        'button',
                                        {
                                            class: 'default-button',
                                            style: 'margin-right: 10px; border-radius: 3px',
                                            onClick: () => router.push('/user/notification'),
                                        },
                                        '查看'
                                    ),
                                    h(
                                        'button',
                                        {
                                            class: 'close-button',
                                            style: 'border-radius: 3px',
                                            onClick: () => notification.close(),
                                        },
                                        '返回'
                                    ),
                                ])
                            ])
                        })
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
