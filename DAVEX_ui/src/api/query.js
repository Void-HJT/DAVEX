import request from '@/utils/request'

export const getDatabase = () => {
    let res = request.post('/query/database/getDatabase')
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