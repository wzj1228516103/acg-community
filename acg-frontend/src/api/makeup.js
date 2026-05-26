import request from '@/utils/request'

export const getMakeupServicesApi = (params) => request.get('/makeup/services', { params })

export const getMakeupServiceDetailApi = (id) => request.get(`/makeup/service/${id}`)
