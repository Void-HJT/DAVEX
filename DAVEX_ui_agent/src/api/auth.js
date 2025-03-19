import request from '@/utils/request'

export const getTokenCache = () => {
    let res = request.post('/auth/getAllMeta')
    return res
}

export const isTokenExpired = (centerId) => {
    const params = new URLSearchParams()
    params.append('centerId', centerId)
    let res = request.post(
        '/auth/checkJwt',
        params.toString()
    )
    return res
}

export const updateToken = (centerId) => {
    const params = new URLSearchParams()
    params.append('centerId', centerId)
    let res = request.post(
        '/auth/refreshJwt',
        params.toString()
    )
    return res
}

export const addToken = (centerId,agentId,password) => {

    const params = new URLSearchParams()
    params.append('centerId', centerId)
    params.append('agentId', agentId)
    params.append('password', password)
    let res = request.post(
        '/auth/getJwt',
        params.toString()
    )
    return res
}

export const deleteToken = (centerId) => {
    const params = new URLSearchParams()
    params.append('centerId', centerId)
    let res = request.post(
        '/auth/revokeJwt',
        params.toString()
    )
    return res
}

export const getUser = () => {
    let res = request.post('/auth/getUser')
    return res
}

export const deleteUser = (username) => {
    const params = new URLSearchParams()
    params.append('username', username)
    let res = request.post(
        '/auth/deleteUser',
        params.toString()
    )
    return res
}

export const addUser = (username,password) => {
    const params = new URLSearchParams()
    params.append('username', username)
    params.append('password', password)
    let res = request.post(
        '/auth/addUser',
        params.toString()
    )
    return res
}



