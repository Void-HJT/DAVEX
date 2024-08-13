import request from '@/utils/request'
//连接
export const userLinkServer = ({ ip, port }) =>
  request.post('/link/metadata/send/', {
    ip,
    port,
  })
