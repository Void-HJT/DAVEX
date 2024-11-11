import request from '@/utils/request'

// 查询是否存在未读消息
export const queryUnread = ({ applicationId }) => {
    const params = {
        appID: applicationId
    }
    return request.get('notification/unread', { params })
}

// 获取消息列表
export const getNotification = ({ applicationId, page, size }) => {
    const params = {
        appID: applicationId,
        page: page,
        size: size
    }
    return request.get('notification/list', { params })
}

// 注明已读
export const read = ({ notificationId }) => {
    const params = {
        uid: notificationId
    }
    return request.get('notification/read', { params })
}

// 获取未读消息列表
export const getUnreadNotification = ({ applicationId, page, size }) => {
    const params = {
        appID: applicationId,
        page: page,
        size: size
    }
    return request.get('notification/listUnread', { params })
}