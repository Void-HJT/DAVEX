import request from '@/utils/request'

export const createPsiTask = ({ rootId }) => {
  const 
  let res = request.post(
    '/MpcTasks/create_with_input',
    params.toString(),
  )
  return res
}