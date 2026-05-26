package com.acg.community.entity;

import com.acg.community.common.BaseEntity;
import com.acg.community.enums.FavoriteType;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_favorite")
public class Favorite extends BaseEntity {

    private Long userId;

    private FavoriteType itemType;

    private Long itemId;
}
