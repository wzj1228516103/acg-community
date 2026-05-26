import request from '@/utils/request'

export const toggleFavoriteApi = (itemType, itemId) => request.post('/favorite/toggle', null, { params: { itemType, itemId } })

export const checkFavoriteApi = (itemType, itemId) => request.get('/favorite/check', { params: { itemType, itemId } })

export const getFavoritesApi = (itemType) => request.get('/favorite/list', { params: { itemType } })
