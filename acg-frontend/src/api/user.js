import request from '@/utils/request'

export const loginApi = (data) => request.post('/user/login', data)

export const registerApi = (data) => request.post('/user/register', data)

export const getUserInfoApi = () => request.get('/user/info')

export const updateProfileApi = (data) => request.put('/user/profile', data)

export const logoutApi = () => request.post('/user/logout')
