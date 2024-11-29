import request from '@/utils/request'

export const getTokenCache = () => {
    let res = request.post('/auth/getCache')
    return res
}

export const isTokenExpired = (authId) => {
    const params = new URLSearchParams()
    params.append('authId', authId)
    let res = request.post(
        '/auth/isTokenExpired',
        params.toString()
    )
    return res
}

export const updateToken = (targetId) => {
    const params = new URLSearchParams()
    params.append('targetId', targetId)
    let res = request.post(
        '/auth/updateToken',
        params.toString()
    )
    return res
}

export const addToken = (username,password,targetId) => {
    const params = new URLSearchParams()
    params.append('username', username)
    params.append('password', password)
    params.append('authId', targetId)
    let res = request.post(
        '/auth/getToken',
        params.toString()
    )
    return res
}

export const deleteToken = (authId) => {
    const params = new URLSearchParams()
    params.append('authId', authId)
    let res = request.post(
        '/auth/logout',
        params.toString()
    )
    return res
}

