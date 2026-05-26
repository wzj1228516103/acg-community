import request from '@/utils/request'

export const getProductsApi = (params) => request.get('/product/list', { params })

export const getProductDetailApi = (id) => request.get(`/product/${id}`)

export const getCategoriesApi = () => request.get('/category/list')
