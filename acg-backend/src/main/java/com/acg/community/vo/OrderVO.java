package com.acg.community.vo;

import com.acg.community.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderVO {

    private Long id;

    private Long userId;

    private String username;

    private BigDecimal totalAmount;

    private OrderStatus status;

    private String receiverName;

    private String receiverPhone;

    private String receiverAddress;

    private List<OrderItemVO> items;

    private LocalDateTime createdAt;
}
