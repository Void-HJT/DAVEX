import request from '@/utils/request'

export const getDatabase = (agentId) => {
    const params = new URLSearchParams()
    params.append('agentId', agentId)
    let res = request.post('/query/database/getDatabase', params.toString())
    return res
  }

export const getDatabaseTable = (databaseId) => {
  const params = new URLSearchParams()
  params.append('databaseId', databaseId)
  let res = request.post('/query/database/getTable', params.toString())
  return res
}

export const query = (applicationId, agentId,databaseId, queryObject) => {
  const params = new URLSearchParams();
  params.append('agentId',agentId)
  params.append('applicationId', applicationId);
  params.append('databaseId', databaseId);

  // 将 queryObject 转换为 JSON 字符串
  const queryString = JSON.stringify(queryObject);

  // 发送 POST 请求
  let res = request.post(`/query/database/query2Agent?${params.toString()}`, queryString, {
    headers: {
      'Content-Type': 'application/json',
    },
  });

  return res;
};

// 查询结果管理区所有文件
export const getQueryResult = ({ applicationId }) => {
  const params = new URLSearchParams()
  params.append('applicationId', applicationId)
  let res = request.post(
      'queryFile/queryQuery',
      params.toString()
  )
  return res
}

// 查询结果管理区部分文件
export const getQueryResultByIds = ({ applicationId, outputIds }) => {
  const params = new URLSearchParams()
  params.append('applicationId', applicationId)
  params.append('outputIds', outputIds)
  let res = request.post(
      'queryFile/queryQueryByIds',
      params.toString()
  )
  return res
}

// 从结果管理区获取文件
export const fetchQuery = ({ outputId, applicationId }) => {
  const params = new URLSearchParams()
  params.append('outputId', outputId)
  params.append('applicationId', applicationId)
  let res = request.post(
      'queryFile/fetchQuery',
      params.toString()
  )
  return res
}

// 从结果管理区删除文件
export const deleteQuery = ({ outputId, applicationId }) => {
  const params = new URLSearchParams()
  params.append('outputId', outputId)
  params.append('applicationId', applicationId)
  let res = request.post(
      'queryFile/deleteQuery',
      params.toString()
  )
  return res
}

// 从结果管理区预览文件
export const readQuery = ({ outputId, applicationId }) => {
  const params = new URLSearchParams()
  params.append('outputId', outputId)
  params.append('applicationId', applicationId)
  let res = request.post(
      'queryFile/readQuery',
      params.toString()
  )
  return res
}

// 新增数据库
export const addDatabase = (outsideDatabase) => {
  return request.post(
      '/query/database/addDatabase',
      outsideDatabase
  )
}

// 新增数据库
// export const addDatabase = ({ outsideDatabase }) => {
//   const formData = new FormData()
//   const outsideDatabaseJson = JSON.stringify(outsideDatabase)
//   const outsideDatabaseBlob = new Blob([outsideDatabaseJson], { type: 'application/json' })
//   formData.append('outsideDatabase', outsideDatabaseBlob)
//   let res = request.post(
//       'query/database/addDatabase',
//       formData,  // 传递 FormData 对象
//       {
//         headers: {
//           'Content-Type': 'multipart/form-data',  // 设置请求头
//         }
//       }
//   )
//   return res
// }