package com.acg.community.entity;

import com.acg.community.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_chat_room")
public class ChatRoom extends BaseEntity {

    private String name;

    private String participantsJson;
}
