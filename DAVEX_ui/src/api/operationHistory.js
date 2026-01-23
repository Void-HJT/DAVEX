import request from '@/utils/request'

/**
 * 获取所有操作历史记录
 */
export function getAllOperationHistory() {
    return request.get('operationHistory/list')
}

/**
 * 根据ID获取操作历史
 * @param {number} uid 操作历史ID
 */
export function getOperationHistoryById(uid) {
    return request.get(`operationHistory/${uid}`)
}

/**
 * 添加操作历史记录
 * @param {Object} params 操作历史参数
 * @param {string} params.agentId 代理ID
 * @param {string} params.applicationId 应用ID
 * @param {string} params.operationType 操作类型
 * @param {string} params.operationObject 操作对象
 * @param {string} params.result 操作结果
 * @param {string} params.remark 备注
 */
export function addOperationHistory(params) {
    const formParams = new URLSearchParams()
    if (params.agentId) formParams.append('agentId', params.agentId)
    if (params.applicationId) formParams.append('applicationId', params.applicationId)
    formParams.append('operationType', params.operationType)
    if (params.operationObject) formParams.append('operationObject', params.operationObject)
    if (params.result) formParams.append('result', params.result)
    if (params.remark) formParams.append('remark', params.remark)
    return request.post('operationHistory/add', formParams.toString())
}

/**
 * 根据代理ID获取操作历史
 * @param {string} agentId 代理ID
 */
export function getOperationHistoryByAgentId(agentId) {
    return request.get(`operationHistory/byAgent/${agentId}`)
}

/**
 * 根据操作类型获取操作历史
 * @param {string} operationType 操作类型
 */
export function getOperationHistoryByType(operationType) {
    return request.get(`operationHistory/byType/${operationType}`)
}

/**
 * 删除操作历史记录
 * @param {number} uid 操作历史ID
 */
export function deleteOperationHistory(uid) {
    return request.delete(`operationHistory/${uid}`)
}

/**
 * 清空所有操作历史记录
 */
export function clearAllOperationHistory() {
    return request.delete('operationHistory/clear')
}
