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
import com.acg.community.util.RedisUtil;
import com.acg.community.vo.UserVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final long USER_CACHE_TTL = 3600;
    private static final String USER_CACHE_KEY = "acg:user:info:";

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisUtil redisUtil;

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
        UserVO vo = toUserVO(user);
        redisUtil.set(USER_CACHE_KEY + vo.getId(), vo, USER_CACHE_TTL);
        return vo;
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
        String cacheKey = USER_CACHE_KEY + userId;
        UserVO cached = redisUtil.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        UserVO vo = toUserVO(user);
        redisUtil.set(cacheKey, vo, USER_CACHE_TTL);
        return vo;
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
        redisUtil.delete(USER_CACHE_KEY + userId);
    }

    private UserVO toUserVO(User user) {
        UserVO vo = new UserVO();
        BeanUtil.copyProperties(user, vo);
        return vo;
    }
}
