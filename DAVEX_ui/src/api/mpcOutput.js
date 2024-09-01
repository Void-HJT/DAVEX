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
export const getMpcResultByIds = ({ applicationId, mpcOutputIds }) => {
    const params = new URLSearchParams()
    params.append('applicationId', applicationId)
    params.append('mpcOutputIds', mpcOutputIds)
    let res = request.post(
        'MpcTasksOutput/queryByIds',
        params.toString()
    )
    return res
}

// 从结果管理区获取文件
export const fetchMpc = ({ mpcOutputId, applicationId }) => {
    const params = new URLSearchParams()
    params.append('mpcOutputId', mpcOutputId)
    params.append('applicationId', applicationId)
    let res = request.post(
        'MpcTasksOutput/fetch',
        params.toString()
    )
    return res
}

// 从结果管理区删除文件
export const deleteMpc = ({ mpcOutputId, applicationId }) => {
    const params = new URLSearchParams()
    params.append('mpcOutputId', mpcOutputId)
    params.append('applicationId', applicationId)
    let res = request.post(
        'MpcTasksOutput/delete',
        params.toString()
    )
    return res
}