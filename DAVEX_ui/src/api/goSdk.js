import request from '@/utils/request'

// ==================== DID 相关 ====================

/** DID 生成（本地生成，不上链） */
export const didGenerate = () => {
  return request.post('/api/v1/control/did/generate')
}

/** DID 注册上链 */
export const didRegister = (didDocument) => {
  return request.post('/api/v1/control/did/register', { didDocument })
}

/** DID 查询（链上） */
export const didQuery = (did) => {
  return request.get('/api/v1/control/did/query', { params: { did } })
}

/** DID 列表（center 本地库） */
export const listDids = () => {
  return request.get('/api/v1/control/data/dids')
}

// ==================== VC（凭证）相关 ====================

/** 颁发 VC */
export const vcIssue = (data) => {
  return request.post('/api/v1/control/vc/issue', data)
}

/** 验证 VC */
export const vcVerify = (vc) => {
  return request.post('/api/v1/control/vc/verify', { vc })
}

/** VC 列表（center 本地库，按 holderDid / issuerDid 可选过滤） */
export const listVcs = (holderDid, issuerDid) => {
  const params = {}
  if (holderDid) params.holderDid = holderDid
  if (issuerDid) params.issuerDid = issuerDid
  return request.get('/api/v1/control/data/vcs', { params })
}

// ==================== VP（可验证展示）相关 ====================

/** 生成 VP */
export const vpGenerate = (data) => {
  return request.post('/api/v1/control/vp/generate', data)
}

/** 验证 VP */
export const vpVerify = (data) => {
  return request.post('/api/v1/control/vp/verify', data)
}

// ==================== 隐私环签名相关 ====================

/** 创建凭证群组 */
export const privacyGroupCreate = (data) => {
  return request.post('/api/v1/control/privacy/group/create', data)
}

/** 添加群组成员公钥 */
export const privacyGroupMember = (data) => {
  return request.post('/api/v1/control/privacy/group/member', data)
}

/** 查询群组信息 */
export const privacyGroupQuery = (groupId) => {
  return request.get('/api/v1/control/privacy/group', { params: { groupId } })
}

/** 群组列表（center 本地库） */
export const listGroups = (groupId) => {
  const params = {}
  if (groupId) params.groupId = groupId
  return request.get('/api/v1/control/data/groups', { params })
}

/** 生成隐私 VP（环签名） */
export const privacyVpGenerate = (data) => {
  return request.post('/api/v1/control/privacy/vp/generate', data)
}

/** 链上验证隐私 VP */
export const privacyVpVerify = (data) => {
  return request.post('/api/v1/control/privacy/vp/verify', data)
}

/** 隐私 VP 列表（center 本地库） */
export const listPrivacyVps = (groupId, holderKeyImage) => {
  const params = {}
  if (groupId) params.groupId = groupId
  if (holderKeyImage) params.holderKeyImage = holderKeyImage
  return request.get('/api/v1/control/data/privacy-vps', { params })
}
