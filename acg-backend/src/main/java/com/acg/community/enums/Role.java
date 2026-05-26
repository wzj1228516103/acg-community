package com.acg.community.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {

    USER(0, "普通用户"),
    MAKEUP_ARTIST(1, "认证化妆师"),
    MERCHANT(2, "认证商家"),
    ADMIN(3, "管理员"),
    SUPER_ADMIN(4, "超级管理员");

    @EnumValue
    @JsonValue
    private final int code;
    private final String desc;

    public static Role of(int code) {
        for (Role role : values()) {
            if (role.code == code) {
                return role;
            }
        }
        return USER;
    }
}
