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