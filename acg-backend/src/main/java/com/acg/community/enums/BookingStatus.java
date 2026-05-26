package com.acg.community.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BookingStatus {

    PENDING(0, "待确认"),
    CONFIRMED(1, "已确认"),
    COMPLETED(2, "已完成"),
    CANCELLED(3, "已取消");

    @EnumValue
    @JsonValue
    private final int code;
    private final String desc;
}
