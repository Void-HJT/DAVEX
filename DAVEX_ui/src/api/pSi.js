import request from '@/utils/request'

// export const createPsiTask = ({ formData }) => {
//   return request({
//     url: '/MpcTasks/create_with_input',
//     method: 'post',
//     data: formData,
//     headers: {
//       'Content-Type': 'multipart/form-data',
//       Accept: '*/*'
//     },
//   });
// };

export const createPsiTask = ({ file, mpcTask }) => {
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