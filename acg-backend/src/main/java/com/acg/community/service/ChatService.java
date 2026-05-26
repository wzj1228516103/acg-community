package com.acg.community.service;

import com.acg.community.entity.ChatRoom;
import com.acg.community.entity.Message;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ChatService extends IService<ChatRoom> {

    ChatRoom getOrCreateRoom(Long userId1, Long userId2);

    List<ChatRoom> getUserRooms(Long userId);

    void sendMessage(Long roomId, Long senderId, String content);

    List<Message> getRoomMessages(Long roomId);
}
