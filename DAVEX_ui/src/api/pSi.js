import request from '@/utils/request'

export const createPsiTask = ({ formData }) => {
  return request({
    url: '/MpcTasks/create_with_input',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
      Accept: '*/*'
    },
  });
};