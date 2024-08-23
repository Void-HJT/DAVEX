import request from '@/utils/request'

export const getDatabase = () => {
    let res = request.post('/query/database/getDatabase')
    return res
  }