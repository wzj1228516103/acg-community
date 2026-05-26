package com.acg.community.vo;

import com.acg.community.enums.Role;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {

    private Long id;

    private String username;

    private String nickname;

    private String phone;

    private String avatarUrl;

    private Role role;

    private LocalDateTime createdAt;
}
