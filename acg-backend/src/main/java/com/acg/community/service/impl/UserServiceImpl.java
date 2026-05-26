package com.acg.community.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.acg.community.dto.LoginDTO;
import com.acg.community.dto.RegisterDTO;
import com.acg.community.dto.UserUpdateDTO;
import com.acg.community.entity.User;
import com.acg.community.enums.Role;
import com.acg.community.exception.BusinessException;
import com.acg.community.mapper.UserMapper;
import com.acg.community.service.UserService;
import com.acg.community.vo.UserVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    @Override
    public UserVO login(LoginDTO dto) {
        User user = lambdaQuery()
                .eq(User::getUsername, dto.getUsername())
                .one();
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        String md5Password = DigestUtil.md5Hex(dto.getPassword());
        if (!md5Password.equals(user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        return toUserVO(user);
    }

    @Override
    public void register(RegisterDTO dto) {
        long count = lambdaQuery()
                .eq(User::getUsername, dto.getUsername())
                .count();
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(DigestUtil.md5Hex(dto.getPassword()));
        user.setRole(Role.USER);
        user.setNickname("漫小团_" + RandomUtil.randomString(4));
        userMapper.insert(user);
        log.info("用户注册成功: {}", dto.getUsername());
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return toUserVO(user);
    }

    @Override
    public void updateProfile(Long userId, UserUpdateDTO dto) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        lambdaUpdate()
                .eq(User::getId, userId)
                .set(dto.getNickname() != null, User::getNickname, dto.getNickname())
                .set(dto.getPhone() != null, User::getPhone, dto.getPhone())
                .set(dto.getAvatarUrl() != null, User::getAvatarUrl, dto.getAvatarUrl())
                .update();
    }

    private UserVO toUserVO(User user) {
        UserVO vo = new UserVO();
        BeanUtil.copyProperties(user, vo);
        return vo;
    }
}
