package com.acg.community.entity;

import com.acg.community.common.BaseEntity;
import com.acg.community.enums.GoodsStatus;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_product")
public class Product extends BaseEntity {

    private String name;

    private String description;

    private BigDecimal price;

    private Integer stock;

    private String images;

    private Long categoryId;

    private Long merchantId;

    private GoodsStatus status;
}
