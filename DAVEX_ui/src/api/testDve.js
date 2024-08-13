import request from '@/utils/request'

export const getApplication = () => {
  let res = request.post('/directory/group/getApplication')
  return res
}

export const addGroup = ({ agentId, centerId, name }) => {
  const params = new URLSearchParams()
  params.append('agentId', agentId)
  params.append('centerId', centerId)
  params.append('name', name)
  let res = request.post('/directory/group/addGroup', params.toString())
  return res
}

export const setGroupRule = ({ agentId, groupId, allowMethod }) => {
  const params = new URLSearchParams()
  params.append('agentId', agentId)
  params.append('groupId', groupId)
  params.append('allowMethod', allowMethod)
  let res = request.post('/directory/group/addRule', params.toString())
  return res
}

export const deleteRule = ({ agentId, groupId, allowMethod }) => {
  const params = new URLSearchParams()
  params.append('agentId', agentId)
  params.append('groupId', groupId)
  params.append('allowMethod', allowMethod)
  let res = request.post('/directory/group/deleteRule', params.toString())
  return res
}

export const getGroup = ({ agentId, centerId }) => {
  const params = new URLSearchParams()
  params.append('agentId', agentId)
  params.append('centerId', centerId)
  let res = request.post('/directory/group/getGroup', params.toString())
  return res
}

export const getRuleByGroup = ({ agentId, groupId }) => {
  const params = new URLSearchParams()
  params.append('agentId', agentId)
  params.append('groupId', groupId)
  let res = request.post('/directory/group/getRuleByGroup', params.toString())
  return res
}

export const getGroupByApplicationId = ({
  agentId,
  centerId,
  applicationId,
}) => {
  const params = new URLSearchParams()
  params.append('agentId', agentId)
  params.append('centerId', centerId)
  params.append('applicationId', applicationId)
  let res = request.post(
    '/directory/group/getGroupByApplicationId',
    params.toString(),
  )
  return res
}

export const deleteGroup = ({ agentId, centerId, groupId }) => {
  const params = new URLSearchParams()
  params.append('agentId', agentId)
  params.append('centerId', centerId)
  params.append('groupId', groupId)
  let res = request.post('/directory/group/deleteGroup', params.toString())
  return res
}

export const addApplicationGroup = ({
  agentId,
  centerId,
  applicationId,
  groupId,
}) => {
  const params = new URLSearchParams()
  params.append('agentId', agentId)
  params.append('centerId', centerId)
  params.append('applicationId', applicationId)
  params.append('groupId', groupId)
  let res = request.post(
    '/directory/group/addApplicationGroup',
    params.toString(),
  )
  return res
}

export const deleteApplicationGroup = ({
  agentId,
  centerId,
  applicationId,
  groupId,
}) => {
  const params = new URLSearchParams()
  params.append('agentId', agentId)
  params.append('centerId', centerId)
  params.append('applicationId', applicationId)
  params.append('groupId', groupId)
  let res = request.post(
    '/directory/group/deleteApplicationGroup',
    params.toString(),
  )
  return res
}
