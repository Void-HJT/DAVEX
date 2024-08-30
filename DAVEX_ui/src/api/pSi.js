import request from '@/utils/request'

export const createPsiTask = ({ formData }) => {
  
  const res = request.post('/MpcTasks/create_with_input', formData,{
    headers: {
      'Content-Type': 'multipart/form-data',
      Accept:'*/*'
    },
  })
  return res
}