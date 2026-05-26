package com.acg.community.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemVO {

    private Long id;

    private Long orderId;

    private Long productId;

    private String productName;

    private String productImage;

    private Integer quantity;

    private BigDecimal price;

    private BigDecimal subtotal;
}
