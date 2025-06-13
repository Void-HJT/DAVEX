import request from '@/utils/request'

// 发送文件传输请求
export const getFile = ({ fileId, agentId, folderId, applicationId }) => {
    const params = new URLSearchParams()
    params.append('fileId', fileId)
    params.append('agentId', agentId)
    params.append('folderId', folderId)
    params.append('applicationId', applicationId)
    let res = request.post(
        'file/getFile',
        params.toString()
    )
    return res
}

// 查询结果管理区所有文件
export const getResult = ({ applicationId }) => {
    const params = new URLSearchParams()
    params.append('applicationId', applicationId)
    let res = request.post(
        'file/query',
        params.toString()
    )
    return res
}

// 查询结果管理区部分文件
export const getResultByIds = ({ applicationId, outputIds }) => {
    const params = new URLSearchParams()
    params.append('applicationId', applicationId)
    params.append('outputIds', outputIds)
    let res = request.post(
        'file/queryByIds',
        params.toString()
    )
    return res
}

// 从结果管理区获取文件
export const fetchFile = ({ outputId, applicationId }) => {
    const params = new URLSearchParams()
    params.append('outputId', outputId)
    params.append('applicationId', applicationId)
    let res = request.post(
        'file/fetch',
        params.toString()
    )
    return res
}

// 从结果管理区删除文件
export const deleteFile = ({ outputId, applicationId }) => {
    const params = new URLSearchParams()
    params.append('outputId', outputId)
    params.append('applicationId', applicationId)
    let res = request.post(
        'file/delete',
        params.toString()
    )
    return res
}

// 从结果管理区预览文件
export const readFile = ({ outputId, applicationId }) => {
    const params = new URLSearchParams()
    params.append('outputId', outputId)
    params.append('applicationId', applicationId)
    let res = request.post(
        'file/read',
        params.toString()
    )
    return res
}