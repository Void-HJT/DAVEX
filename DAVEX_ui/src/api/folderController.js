import request from '@/utils/request'

export const getDirectory = ({ rootId }) => {
  const params = new URLSearchParams()
  params.append('rootId', rootId)
  let res = request.post(
    '/directory/fileFolder/getDirectory',
    params.toString(),
  )
  return res
}

export const updateFile = ({
  uid,
  agentId,
  folderId,
  name,
  createDate,
  lastUpdata,
  tag,
  size,
  description,
  expireTime,
  hash,
  example,
  type,

}) => {
  let res = request.post('/directory/fileFolder/updateFile', {
    uid,
  agentId,
  folderId,
  name,
  createDate,
  lastUpdata,
  tag,
  size,
  description,
  expireTime,
  hash,
  example,
  type,
  })

  return res
}

export const getDirectoryByGroup = ({ rootId, agentId, groupId }) => {
  const params = new URLSearchParams()
  params.append('rootId', rootId)
  params.append('agentId', agentId)
  params.append('groupId', groupId)
  let res = request.post(
    '/directory/fileFolder/getDirectoryByGroup',
    params.toString(),
  )
  return res
}

export const getDirectoryByApplication = ({
  rootId,
  agentId,
  applicationId,
}) => {
  const params = new URLSearchParams()
  params.append('rootId', rootId)
  params.append('agentId', agentId)
  params.append('applicationId', applicationId)
  let res = request.post(
    '/directory/fileFolder/getDirectoryByApplication',
    params.toString(),
  )
  return res
}

export const deleteFolder = ({ agentId, folderId }) => {
  const params = new URLSearchParams()
  params.append('agentId', agentId)
  params.append('folderId', folderId)
  let res = request.post(
    '/directory/fileFolder/deleteFolder',
    params.toString(),
  )
  return res
}

export const deleteFile = ({ fileId, agentId, folderId }) => {
  const params = new URLSearchParams()
  params.append('fileId', fileId)
  params.append('agentId', agentId)
  params.append('folderId', folderId)
  let res = request.post('/directory/fileFolder/deleteFile', params.toString())
  return res
}

export const setFolderInvisible = ({ agentId, groupId, folderId }) => {
  const params = new URLSearchParams()
  params.append('agentId', agentId)
  params.append('groupId', groupId)
  params.append('folderId', folderId)
  let res = request.post(
    '/directory/fileFolder/setFolderInvisible',
    params.toString(),
  )
  return res
}

export const setFolderVisible = ({ agentId, groupId, folderId }) => {
  const params = new URLSearchParams()
  params.append('agentId', agentId)
  params.append('groupId', groupId)
  params.append('folderId', folderId)
  let res = request.post(
    '/directory/fileFolder/setFolderVisible',
    params.toString(),
  )
  return res
}

export const createFolder = ({ name, agentId, parentId }) => {
  const params = new URLSearchParams()
  params.append('name', name)
  params.append('agentId', agentId)
  params.append('parentId', parentId)
  let res = request.post(
    '/directory/fileFolder/createFolder',
    params.toString(),
  )
  return res
}

export const setFolderName = ({ agentId, folderId, name }) => {
  const params = new URLSearchParams()

  params.append('agentId', agentId)
  params.append('folderId', folderId)
  params.append('name', name)
  let res = request.post(
    '/directory/fileFolder/setFolderName',
    params.toString(),
  )
  return res
}

export const getFile = ({ fileId, agentId, }) => {
  const params = new URLSearchParams()
  params.append('fileId', fileId)
  params.append('agentId', agentId)
  let res = request.post('/directory/fileFolder/getFile', params.toString())
  return res
}

export const getFileInfo = ({ uid, agentId, folderId }) => {
  const params = new URLSearchParams()
  params.append('uid', uid)
  params.append('agentId', agentId)
  params.append('folderId', folderId)
  let res = request.post('/directory/fileFolder/getFileInfo', params.toString())
  return res
}

export const setFileRule = ({
  fileId,
  agentId,
  folderId,
  groupId,
  allowedMethod,
}) => {
  const params = new URLSearchParams()
  params.append('fileId', fileId)
  params.append('agentId', agentId)
  params.append('folderId', folderId)
  params.append('groupId', groupId)
  params.append('allowedMethod', allowedMethod)
  let res = request.post('/directory/fileFolder/setFileRule', params.toString())
  return res
}
export const getGroupInfoByGroupId = ({
  groupId,
  agentId,
  centerId,
}) => {
  const params = new URLSearchParams()
  params.append('groupId', groupId)
  params.append('agentId', agentId)
  params.append('centerId', centerId)
  let res = request.post(
    '/directory/group/getGroup',
    params.toString(),
  )
  return res
}
export const getCenterInfoByCenterId = ({
  centerId,
}) => {
  const params = new URLSearchParams()
  params.append('centerId', centerId)
  let res = request.post(
    '/directory/group/getCenter',
    params.toString(),
  )
  return res
}


export const getAgentInfoByAgentId = ({
  agentId,
}) => {
  const params = new URLSearchParams()
  params.append('agentId', agentId)
  let res = request.post(
    '/directory/group/getAgent',
    params.toString(),
  )
  return res
}

export const deleteFileRule = ({
  fileId,
  agentId,
  folderId,
  groupId,
  allowedMethod,
}) => {
  const params = new URLSearchParams()
  params.append('fileId', fileId)
  params.append('agentId', agentId)
  params.append('folderId', folderId)
  params.append('groupId', groupId)
  params.append('allowedMethod', allowedMethod)
  let res = request.post(
    '/directory/fileFolder/deleteFileRule',
    params.toString(),
  )
  return res
}

export const getRootByAgent = ( agentId ) => {
  const params = new URLSearchParams()
  params.append('agentId', agentId)
  let res = request.post(
      '/directory/fileFolder/getRootByAgent',
      params.toString(),
  )
  return res
}

export const uploadFile = ({ agentId, folderId, file }) => {
  // 使用 FormData 来处理文件上传
  const formData = new FormData()
  formData.append('file', file)
  formData.append('agentId', agentId)
  formData.append('folderId', folderId)
  let res = request.post(
      '/directory/fileFolder/uploadFile',
      formData,  // 传递 FormData 对象
      {
        headers: {
          'Content-Type': 'multipart/form-data',  // 设置请求头
        }
      }
  )
  return res
}
