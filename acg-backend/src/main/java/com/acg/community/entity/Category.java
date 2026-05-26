package com.acg.community.entity;

import com.acg.community.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_category")
public class Category extends BaseEntity {

    private String name;

    private String description;

    private Integer sortOrder;

    private Boolean isActive;
}
