import request from '@/utils/request'

export const getFile = ({ fileId, agentId, folderId, applicationId }) => {
    const params = new URLSearchParams()
    params.append('fileId', fileId)
    params.append('agentId', agentId)
    params.append('folderId', folderId)
    params.append('applicationId', applicationId)
    let res = request.post(
        'file/getFile',
        params.toString()
    )
    return res
}