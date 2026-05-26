import request from '@/utils/request'

export const getOrCreateRoomApi = (targetUserId) => request.post('/chat/room', null, { params: { targetUserId } })

export const getRoomsApi = () => request.get('/chat/rooms')

export const sendMessageApi = (roomId, content) => request.post('/chat/send', null, { params: { roomId, content } })

export const getMessagesApi = (roomId) => request.get('/chat/messages', { params: { roomId } })
