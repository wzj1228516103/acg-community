package com.acg.community.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.acg.community.common.Result;
import com.acg.community.entity.ChatRoom;
import com.acg.community.entity.Message;
import com.acg.community.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/room")
    public Result<ChatRoom> getOrCreateRoom(@RequestParam Long targetUserId) {
        Long userId = StpUtil.getLoginIdAsLong();
        ChatRoom room = chatService.getOrCreateRoom(userId, targetUserId);
        return Result.success(room);
    }

    @GetMapping("/rooms")
    public Result<List<ChatRoom>> listRooms() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(chatService.getUserRooms(userId));
    }

    @PostMapping("/send")
    public Result<Void> sendMessage(@RequestParam Long roomId,
                                    @RequestParam String content) {
        Long userId = StpUtil.getLoginIdAsLong();
        chatService.sendMessage(roomId, userId, content);
        return Result.success("发送成功", null);
    }

    @GetMapping("/messages")
    public Result<List<Message>> getMessages(@RequestParam Long roomId) {
        return Result.success(chatService.getRoomMessages(roomId));
    }
}
