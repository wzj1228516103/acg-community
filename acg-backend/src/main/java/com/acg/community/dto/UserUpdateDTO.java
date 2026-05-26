package com.acg.community.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateDTO {

    @Size(max = 30, message = "昵称长度不能超过30个字符")
    private String nickname;

    @Size(max = 20, message = "手机号长度不能超过20个字符")
    private String phone;

    @Size(max = 255, message = "头像URL长度不能超过255个字符")
    private String avatarUrl;
}
