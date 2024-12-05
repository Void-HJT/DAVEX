import request from '@/utils/request'

// 获取数据目录
export const getDirectory = ({ applicationId, agentId }) => {
    const params = new URLSearchParams()
    params.append('applicationId', applicationId)
    params.append('agentId', agentId)
    let res = request.post(
        'comparison/getDirectory',
        params.toString()
    )
    return res
}

// 获取csv文件表头信息
export const getTableHeader = ({ agentId, fileId, folderId }) => {
    const params = new URLSearchParams()
    params.append('agentId', agentId)
    params.append('fileId', fileId)
    params.append('folderId', folderId)
    let res = request.post(
        'comparison/getTableHeader',
        params.toString()
    )
    return res
}

//获取txt文件第一行的信息
export const getTXTExample = ({ agentId, fileId, folderId }) => {
    const params = new URLSearchParams()
    params.append('agentId', agentId)
    params.append('fileId', fileId)
    params.append('folderId', folderId)
    let res = request.post(
        'comparison/getTXTExample',
        params.toString()
    )
    return res
}

// 输入属性比对
export const compare = ({ applicationId, agentId, fileId, folderId, attributes, valuesList }) => {
    const formData = new FormData()

    formData.append('applicationId', applicationId)
    formData.append('agentId', agentId)
    formData.append('fileId', fileId)
    formData.append('folderId', folderId)
    formData.append('attributes', JSON.stringify(attributes))
    formData.append('valuesList', JSON.stringify(valuesList))

    let res = request.post(
        'comparison/compareFromJson',
        formData,
        {
            headers: {
                'Content-Type': 'multipart/form-data',
            }
        }
    )

    return res
}

// 一行一行输入数据比对
export const compareTXT = ({ applicationId, agentId, fileId, folderId, valuesList }) => {
    const formData = new FormData()

    formData.append('applicationId', applicationId)
    formData.append('agentId', agentId)
    formData.append('fileId', fileId)
    formData.append('folderId', folderId)
    formData.append('valuesList', JSON.stringify(valuesList))

    let res = request.post(
        'comparison/compareTXTFromJson',
        formData,
        {
            headers: {
                'Content-Type': 'multipart/form-data',
            }
        }
    )

    return res
}

// 输入文件比对
export const compareFromCsv = ({ applicationId, agentId, fileId, folderId, file }) => {
    // 使用 FormData 来处理文件上传
    const formData = new FormData()
    formData.append('applicationId', applicationId)
    formData.append('agentId', agentId)
    formData.append('fileId', fileId)
    formData.append('folderId', folderId)
    formData.append('file', file)
    let res = request.post(
        'comparison/compareFromCsv',
        formData,  // 传递 FormData 对象
        {
            headers: {
                'Content-Type': 'multipart/form-data',  // 设置请求头
            }
        }
    )
    return res
}

//输入txt文件比对
export const compareFromTXT = ({ applicationId, agentId, fileId, folderId, file }) => {
    // 使用 FormData 来处理文件上传
    const formData = new FormData()
    formData.append('applicationId', applicationId)
    formData.append('agentId', agentId)
    formData.append('fileId', fileId)
    formData.append('folderId', folderId)
    formData.append('file', file)
    let res = request.post(
        'comparison/compareFromTXT',
        formData,  // 传递 FormData 对象
        {
            headers: {
                'Content-Type': 'multipart/form-data',  // 设置请求头
            }
        }
    )
    return res
}

// 查询结果管理区所有文件
export const getComparisonResult = ({ applicationId }) => {
    const params = new URLSearchParams()
    params.append('applicationId', applicationId)
    let res = request.post(
        'comparisonFile/queryComparison',
        params.toString()
    )
    return res
}

// 查询结果管理区部分文件
export const getComparisonResultByIds = ({ applicationId, outputIds }) => {
    const params = new URLSearchParams()
    params.append('applicationId', applicationId)
    params.append('outputIds', outputIds)
    let res = request.post(
        'comparisonFile/queryComparisonByIds',
        params.toString()
    )
    return res
}

// 从结果管理区获取文件
export const fetchComparison = ({ outputId, applicationId }) => {
    const params = new URLSearchParams()
    params.append('outputId', outputId)
    params.append('applicationId', applicationId)
    let res = request.post(
        'comparisonFile/fetchComparison',
        params.toString()
    )
    return res
}

// 从结果管理区删除文件
export const deleteComparison = ({ outputId, applicationId }) => {
    const params = new URLSearchParams()
    params.append('outputId', outputId)
    params.append('applicationId', applicationId)
    let res = request.post(
        'comparisonFile/deleteComparison',
        params.toString()
    )
    return res
}

// 从结果管理区预览文件
export const readComparison = ({ outputId, applicationId }) => {
    const params = new URLSearchParams()
    params.append('outputId', outputId)
    params.append('applicationId', applicationId)
    let res = request.post(
        'comparisonFile/readComparison',
        params.toString()
    )
    return res
}