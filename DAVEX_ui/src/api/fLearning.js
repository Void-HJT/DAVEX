import request from '@/utils/request'

export const activeRay = ({ port }) => {
    // 使用 URLSearchParams 来构建查询字符串
    const params = new URLSearchParams();
    params.append('port', port);  // 将 port 作为查询参数添加

    // 使用 params 作为请求的查询参数
    let res = request.get('/SecretFlowTask/active-mainRay', { params });

    return res;
}


export const executeTask = ({ taskName,outputPath }) => {
    // 使用 URLSearchParams 来构建查询字符串
    const params = new URLSearchParams();
    params.append('taskName', taskName);  // 将 port 作为查询参数添加
    params.append('outputPath',outputPath );  // 将 port 作为查询参数添加

    const config = {
        params,
        timeout: 3000000 // 超时时间设置为30秒
    };
    // 使用 params 作为请求的查询参数
    let res = request.get('/SecretFlowTask/executeTask', config);

    return res;
}

export const stopRay = () => {
    let res = request.get('/SecretFlowTask/stop-mainRay');

    return res;
}

export const getRayStatus = () => {
    let res = request.get('/SecretFlowTask/getRayStatus');
    return res;
}