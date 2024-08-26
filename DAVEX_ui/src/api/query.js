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

export const query = (applicationId, databaseId, queryObject) => {
  const params = new URLSearchParams();
  params.append('applicationId', applicationId);
  params.append('databaseId', databaseId);

  // 将 queryObject 转换为 JSON 字符串
  const queryString = JSON.stringify(queryObject);

  // 发送 POST 请求
  let res = request.post(`/query/database/query?${params.toString()}`, queryString, {
    headers: {
      'Content-Type': 'application/json',
    },
  });

  return res;
};