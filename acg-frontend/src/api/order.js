import request from '@/utils/request'

export const createOrderApi = (data) => request.post('/order/create', data)

export const getOrdersApi = (params) => request.get('/order/list', { params })

export const getOrderDetailApi = (id) => request.get(`/order/${id}`)

export const payOrderApi = (id) => request.put(`/order/${id}/pay`)

export const cancelOrderApi = (id) => request.put(`/order/${id}/cancel`)

export const confirmReceiveApi = (id) => request.put(`/order/${id}/receive`)
