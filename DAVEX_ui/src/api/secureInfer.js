import request from '@/utils/request'

// 创建安全推理任务
export const create = ({ file, inferenceInfo }) => {
    // 使用 FormData 来处理文件上传
    const formData = new FormData()
    formData.append('file', file)
    const inferenceInfoJson = JSON.stringify(inferenceInfo)
    const inferenceInfoBlob = new Blob([inferenceInfoJson], { type: 'application/json' })
    formData.append('inferenceInfo', inferenceInfoBlob)
    let res = request.post(
        'SecureInference/create',
        formData,  // 传递 FormData 对象
        {
            headers: {
                'Content-Type': 'multipart/form-data',  // 设置请求头
            }
        }
    )
    return res
}

// // 查询结果管理区所有文件
// export const getComparisonResult = ({ applicationId }) => {
//     const params = new URLSearchParams()
//     params.append('applicationId', applicationId)
//     let res = request.post(
//         'comparisonFile/queryComparison',
//         params.toString()
//     )
//     return res
// }

// // 查询结果管理区部分文件
// export const getComparisonResultByIds = ({ applicationId, outputIds }) => {
//     const params = new URLSearchParams()
//     params.append('applicationId', applicationId)
//     params.append('outputIds', outputIds)
//     let res = request.post(
//         'comparisonFile/queryComparisonByIds',
//         params.toString()
//     )
//     return res
// }

// // 从结果管理区获取文件
// export const fetchComparison = ({ outputId, applicationId }) => {
//     const params = new URLSearchParams()
//     params.append('outputId', outputId)
//     params.append('applicationId', applicationId)
//     let res = request.post(
//         'comparisonFile/fetchComparison',
//         params.toString()
//     )
//     return res
// }

// // 从结果管理区删除文件
// export const deleteComparison = ({ outputId, applicationId }) => {
//     const params = new URLSearchParams()
//     params.append('outputId', outputId)
//     params.append('applicationId', applicationId)
//     let res = request.post(
//         'comparisonFile/deleteComparison',
//         params.toString()
//     )
//     return res
// }