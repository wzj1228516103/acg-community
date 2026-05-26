package com.acg.community.entity;

import com.acg.community.common.BaseEntity;
import com.acg.community.enums.OrderStatus;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_order")
public class Order extends BaseEntity {

    private Long userId;

    private BigDecimal totalAmount;

    private OrderStatus status;

    private String receiverName;

    private String receiverPhone;

    private String receiverAddress;
}
