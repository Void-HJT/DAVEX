import request from '@/utils/request'

export const getTokenCache = () => {
    let res = request.post('/auth/getCache')
    return res
}