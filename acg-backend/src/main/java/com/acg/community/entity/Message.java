package com.acg.community.entity;

import com.acg.community.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_message")
public class Message extends BaseEntity {

    private Long roomId;

    private Long senderId;

    private String content;

    private String messageType;
}
