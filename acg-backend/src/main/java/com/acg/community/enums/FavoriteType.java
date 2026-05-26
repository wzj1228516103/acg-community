package com.acg.community.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FavoriteType {

    PRODUCT(0, "商品"),
    MAKEUP_ARTIST(1, "化妆师"),
    MAKEUP_SERVICE(2, "化妆服务");

    @EnumValue
    @JsonValue
    private final int code;
    private final String desc;
}
