import request from '@/utils/request'

export const getMpcList = () => {
  let res = request.get('/Mpc/list')
  return res
}