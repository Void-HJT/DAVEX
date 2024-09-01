import request from '@/utils/request'

// 查询结果管理区所有文件
export const getMpcResult = ({ applicationId }) => {
    const params = new URLSearchParams()
    params.append('applicationId', applicationId)
    let res = request.post(
        'MpcTasksOutput/query',
        params.toString()
    )
    return res
}

// 查询结果管理区部分文件
export const getMpcResultByIds = ({ applicationId, outputIds }) => {
    const params = new URLSearchParams()
    params.append('applicationId', applicationId)
    params.append('outputIds', outputIds)
    let res = request.post(
        'MpcTasksOutput/queryByIds',
        params.toString()
    )
    return res
}

// 从结果管理区获取文件
export const fetchMpc = ({ outputId, applicationId }) => {
    const params = new URLSearchParams()
    params.append('outputId', outputId)
    params.append('applicationId', applicationId)
    let res = request.post(
        'MpcTasksOutput/fetch',
        params.toString()
    )
    return res
}

// 从结果管理区删除文件
export const deleteMpc = ({ outputId, applicationId }) => {
    const params = new URLSearchParams()
    params.append('outputId', outputId)
    params.append('applicationId', applicationId)
    let res = request.post(
        'MpcTasksOutput/delete',
        params.toString()
    )
    return res
}