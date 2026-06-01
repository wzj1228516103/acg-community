import request from '@/utils/request'

export const getMakeupServicesApi = (params) => request.get('/makeup/services', { params })

export const getMakeupServiceDetailApi = (id) => request.get(`/makeup/service/${id}`)

export const createMakeupBookingApi = (data) => request.post('/makeup/booking/create', data)

export const getAvailableSlotsApi = (serviceId) => request.get('/makeup/slot/available', { params: { serviceId } })

export const createArtistSlotsApi = (data) => request.post('/makeup/slot/create', data)

export const createMakeupServiceApi = (data) => request.post('/makeup/service/create', data)
