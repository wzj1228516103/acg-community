package com.acg.community.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GoodsStatus {

    ACTIVE(0, "上架"),
    INACTIVE(1, "下架");

    @EnumValue
    @JsonValue
    private final int code;
    private final String desc;
}
