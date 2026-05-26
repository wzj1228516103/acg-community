package com.acg.community.entity;

import com.acg.community.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_order_item")
public class OrderItem extends BaseEntity {

    private Long orderId;

    private Long productId;

    private String productName;

    private String productImage;

    private Integer quantity;

    private BigDecimal price;

    private BigDecimal subtotal;
}
