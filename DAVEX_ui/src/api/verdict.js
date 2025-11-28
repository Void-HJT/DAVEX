import request from '@/utils/request'

/**
 * Center端转发查询条件到Agent端（新增接口）
 * @param {Object} config - 请求配置
 * @param {String} config.agentId - 目标AgentID（必传，用于Center与Agent通信路由）
 * @param {Object} [config.filterParams] - 筛选条件参数（同query2Embeddings的params）
 * @returns {Promise} - 请求Promise对象（成功返回Agent端生成的vectorizer.pkl文件路径）
 */
export const sendQuery = ({ agentId, filterParams = {} }) => {
    // 构造URLSearchParams传递agentId（作为query参数）
    const params = new URLSearchParams()
    params.append('agentId', agentId)

    return request.post(
        `verdict/sendQuery?${params.toString()}`, // agentId通过query参数传递
        filterParams, // 筛选条件通过requestBody传递（JSON格式）
        {
            headers: {
                'Content-Type': 'application/json;charset=UTF-8'
            }
        }
    )
}