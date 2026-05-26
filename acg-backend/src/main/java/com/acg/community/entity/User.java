package com.acg.community.entity;

import com.acg.community.common.BaseEntity;
import com.acg.community.enums.Role;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user")
public class User extends BaseEntity {

    private String username;

    private String password;

    private String nickname;

    private String phone;

    private String avatarUrl;

    private Role role;
}
