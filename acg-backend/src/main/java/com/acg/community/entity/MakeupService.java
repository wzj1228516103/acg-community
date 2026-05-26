package com.acg.community.entity;

import com.acg.community.common.BaseEntity;
import com.acg.community.enums.GoodsStatus;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_makeup_service")
public class MakeupService extends BaseEntity {

    private Long artistId;

    private String name;

    private String description;

    private BigDecimal price;

    private Integer duration;

    private String images;

    private GoodsStatus status;
}
