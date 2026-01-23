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

/**
 * Center端串联流程：先获取pkl文件，再上传输入文件生成emb文件
 * @param {Object} config - 请求配置
 * @param {String} config.agentId - 目标AgentID（必传）
 * @param {Object} [config.filterParams] - 筛选条件参数（同sendQuery）
 * @param {File} config.inputFile - 上传的输入文本文件（必传，MultipartFile类型）
 * @returns {Promise} - 请求Promise对象（成功返回最终生成的emb文件路径）
 */
export const sendAndCompute = ({ agentId, filterParams = {}, inputFile }) => {
    // 1. 校验必传参数
    if (!agentId) {
        return Promise.reject(new Error('agentId不能为空'))
    }
    if (!inputFile) {
        return Promise.reject(new Error('请选择要上传的输入文件'))
    }

    // 2. 构造FormData（文件上传必须使用FormData格式）
    const formData = new FormData()
    // 仅通过FormData传递agentId（移除URL上的重复传递）
    formData.append('agentId', agentId)
    // 追加筛选条件（需转为JSON字符串，后端接收后需解析）
    formData.append('filterParams', JSON.stringify(filterParams))
    // 追加上传文件（key需与后端接口参数名一致，此处为inputFile）
    formData.append('inputFile', inputFile)

    // 3. 发送请求：移除URL上的agentId拼接
    return request.post(
        `verdict/sendAndCompute`, // 不再拼接query参数
        formData, // 传递FormData对象
        {
            // 文件上传需指定该请求头，让浏览器自动处理边界符
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        }
    )
}

/**
 * 获取所有类案检索任务
 * @returns {Promise} - 请求Promise对象（成功返回所有任务列表）
 */
export const getAllTasks = () => {
    return request.get('verdict/tasks')
}

/**
 * 根据任务id获取某个任务
 * @param {Number|String} taskId - 任务ID
 * @returns {Promise} - 请求Promise对象（成功返回任务详细信息）
 */
export const getTaskById = (taskId) => {
    if (!taskId) {
        return Promise.reject(new Error('任务ID不能为空'))
    }
    return request.get(`verdict/tasks/${taskId}`)
}

/**
 * 根据任务id编辑某个任务
 * @param {Number|String} taskId - 任务ID
 * @param {Object} task - 要更新的任务信息（支持部分字段更新）
 * @returns {Promise} - 请求Promise对象（成功返回更新结果）
 */
export const updateTask = (taskId, task) => {
    if (!taskId) {
        return Promise.reject(new Error('任务ID不能为空'))
    }
    if (!task) {
        return Promise.reject(new Error('任务信息不能为空'))
    }
    return request.put(
        `verdict/tasks/${taskId}`,
        task,
        {
            headers: {
                'Content-Type': 'application/json;charset=UTF-8'
            }
        }
    )
}

/**
 * 根据fileId和agentId获取文件信息
 * @param {String} fileId - 文件ID
 * @param {String} agentId - 代理ID
 * @returns {Promise} - 请求Promise对象（成功返回文件信息）
 */
export const getFileInfo = (fileId, agentId) => {
    if (!fileId) {
        return Promise.reject(new Error('文件ID不能为空'))
    }
    if (!agentId) {
        return Promise.reject(new Error('代理ID不能为空'))
    }
    const params = new URLSearchParams()
    params.append('fileId', fileId)
    params.append('agentId', agentId)
    return request.post('/directory/fileFolder/getFile', params.toString())
}

/**
 * 根据fileId和agentId读取文件内容
 * @param {String} fileId - 文件ID
 * @param {String} agentId - 代理ID
 * @returns {Promise} - 请求Promise对象（成功返回文件内容）
 */
export const readFileContent = (fileId, agentId) => {
    if (!fileId) {
        return Promise.reject(new Error('文件ID不能为空'))
    }
    if (!agentId) {
        return Promise.reject(new Error('代理ID不能为空'))
    }
    const params = new URLSearchParams()
    params.append('fileId', fileId)
    params.append('agentId', agentId)
    return request.post('verdict/readFile', params.toString())
}