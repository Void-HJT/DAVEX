import request from '@/utils/request'

export const createMpcTask = ({ file, mpcTask }) => {
  const formData = new FormData()
  formData.append('file', file)
  const mpcTaskJson = JSON.stringify(mpcTask)
  const mpcTaskBlob = new Blob([mpcTaskJson], { type: 'application/json' })
  formData.append('mpcTask', mpcTaskBlob) // 添加 mpcTask，指定类型
  let res = request.post(
      '/MpcTasks/create_with_input',
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        }
      }
  )
  return res
}

// MPC 管理页面不传参数时查询全部，隐私计算页面传参数时按功能查询。
export const getMpcList = (taskType) => {
  return request.get('/Mpc/list', {
    params: taskType ? { taskType } : {},
  })
}

export const uploadMpc = ({ file, mpc }) => {
    const formData = new FormData()
    formData.append('file', file)
    const mpcJson = JSON.stringify(mpc)
    const mpcBlob = new Blob([mpcJson], { type: 'application/json' })
    formData.append('mpc', mpcBlob)
    let res = request.post(
        '/Mpc/create',
        formData,
        {
            headers: {
                'Content-Type': 'multipart/form-data',
            }
        }
    )
    return res
}