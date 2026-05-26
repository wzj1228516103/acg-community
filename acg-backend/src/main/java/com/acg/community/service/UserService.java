package com.acg.community.service;

import com.acg.community.dto.LoginDTO;
import com.acg.community.dto.RegisterDTO;
import com.acg.community.dto.UserUpdateDTO;
import com.acg.community.entity.User;
import com.acg.community.vo.UserVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserService extends IService<User> {

    UserVO login(LoginDTO dto);

    void register(RegisterDTO dto);

    UserVO getUserInfo(Long userId);

    void updateProfile(Long userId, UserUpdateDTO dto);
}
