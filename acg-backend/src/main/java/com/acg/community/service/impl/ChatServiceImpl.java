package com.acg.community.service.impl;

import com.acg.community.entity.ChatRoom;
import com.acg.community.entity.Message;
import com.acg.community.exception.BusinessException;
import com.acg.community.mapper.ChatRoomMapper;
import com.acg.community.mapper.MessageMapper;
import com.acg.community.service.ChatService;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatServiceImpl extends ServiceImpl<ChatRoomMapper, ChatRoom> implements ChatService {

    @Resource
    private ChatRoomMapper chatRoomMapper;

    @Resource
    private MessageMapper messageMapper;

    @Override
    public ChatRoom getOrCreateRoom(Long userId1, Long userId2) {
        List<ChatRoom> rooms = lambdaQuery().list();
        List<Long> targetSorted = Arrays.asList(userId1, userId2);
        targetSorted.sort(Comparator.naturalOrder());

        for (ChatRoom room : rooms) {
            if (room.getParticipantsJson() != null) {
                try {
                    List<Long> participants = JSON.parseArray(room.getParticipantsJson(), Long.class)
                            .stream().sorted().collect(Collectors.toList());
                    if (participants.equals(targetSorted)) {
                        return room;
                    }
                } catch (Exception ignored) {
                }
            }
        }
        ChatRoom room = new ChatRoom();
        room.setName("chat_" + userId1 + "_" + userId2);
        room.setParticipantsJson(JSON.toJSONString(targetSorted));
        chatRoomMapper.insert(room);
        log.info("创建聊天室: roomId={}, participants={}", room.getId(), room.getParticipantsJson());
        return room;
    }

    @Override
    public List<ChatRoom> getUserRooms(Long userId) {
        return lambdaQuery()
                .like(ChatRoom::getParticipantsJson, String.valueOf(userId))
                .orderByDesc(ChatRoom::getUpdatedAt)
                .list();
    }

    @Override
    public void sendMessage(Long roomId, Long senderId, String content) {
        ChatRoom room = getById(roomId);
        if (room == null) {
            throw new BusinessException("聊天室不存在");
        }
        Message message = new Message();
        message.setRoomId(roomId);
        message.setSenderId(senderId);
        message.setContent(content);
        message.setMessageType("TEXT");
        messageMapper.insert(message);
        lambdaUpdate()
                .eq(ChatRoom::getId, roomId)
                .set(ChatRoom::getUpdatedAt, LocalDateTime.now())
                .update();
        log.info("发送消息: roomId={}, senderId={}", roomId, senderId);
    }

    @Override
    public List<Message> getRoomMessages(Long roomId) {
        ChatRoom room = getById(roomId);
        if (room == null) {
            throw new BusinessException("聊天室不存在");
        }
        return messageMapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getRoomId, roomId)
                        .orderByAsc(Message::getCreatedAt)
        );
    }
}
