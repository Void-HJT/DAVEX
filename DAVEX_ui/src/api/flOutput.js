import request from '@/utils/request'

// 查询结果管理区所有文件
export const getFlResult = ({ applicationId }) => {
    const params = new URLSearchParams()
    params.append('applicationId', applicationId)
    let res = request.post(
        'flFile/queryFl',
        params.toString()
    )
    return res
}

// 查询结果管理区部分文件
export const getFlResultByIds = ({ applicationId, outputIds }) => {
    const params = new URLSearchParams()
    params.append('applicationId', applicationId)
    params.append('outputIds', outputIds)
    let res = request.post(
        'flFile/queryFlByIds',
        params.toString()
    )
    return res
}

// 从结果管理区获取文件
export const fetchFl = ({ outputId, applicationId }) => {
    const params = new URLSearchParams()
    params.append('outputId', outputId)
    params.append('applicationId', applicationId)
    let res = request.post(
        'flFile/fetchFl',
        params.toString()
    )
    return res
}

// 从结果管理区删除文件
export const deleteFl = ({ outputId, applicationId }) => {
    const params = new URLSearchParams()
    params.append('outputId', outputId)
    params.append('applicationId', applicationId)
    let res = request.post(
        'flFile/deleteFl',
        params.toString()
    )
    return res
}

// 从结果管理区预览文件
export const readFl = ({ outputId, applicationId }) => {
    const params = new URLSearchParams()
    params.append('outputId', outputId)
    params.append('applicationId', applicationId)
    let res = request.post(
        'flFile/readFl',
        params.toString()
    )
    return res
}