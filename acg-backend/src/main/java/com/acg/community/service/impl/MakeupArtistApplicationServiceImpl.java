package com.acg.community.service.impl;

import com.acg.community.entity.MakeupArtistApplication;
import com.acg.community.entity.User;
import com.acg.community.enums.ApplyStatus;
import com.acg.community.enums.Role;
import com.acg.community.exception.BusinessException;
import com.acg.community.mapper.MakeupArtistApplicationMapper;
import com.acg.community.mapper.UserMapper;
import com.acg.community.service.MakeupArtistApplicationService;
import com.acg.community.util.RedisUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class MakeupArtistApplicationServiceImpl extends ServiceImpl<MakeupArtistApplicationMapper, MakeupArtistApplication> implements MakeupArtistApplicationService {

    @Resource
    private MakeupArtistApplicationMapper applicationMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisUtil redisUtil;

    @Override
    public Page<MakeupArtistApplication> listApplications(int page, int size, ApplyStatus status) {
        LambdaQueryWrapper<MakeupArtistApplication> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(MakeupArtistApplication::getStatus, status);
        }
        wrapper.orderByDesc(MakeupArtistApplication::getCreatedAt);
        return applicationMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    @Transactional
    public void approveApplication(Long id) {
        MakeupArtistApplication app = getById(id);
        if (app == null) {
            throw new BusinessException("申请不存在");
        }
        if (app.getStatus() != ApplyStatus.PENDING) {
            throw new BusinessException("该申请已处理");
        }
        lambdaUpdate().eq(MakeupArtistApplication::getId, id).set(MakeupArtistApplication::getStatus, ApplyStatus.APPROVED).update();
        userMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<User>()
                .eq(User::getId, app.getUserId())
                .set(User::getRole, Role.MAKEUP_ARTIST));
        redisUtil.delete("acg:user:info:" + app.getUserId());
        log.info("化妆师申请通过, applicationId={}, userId={}", id, app.getUserId());
    }

    @Override
    public void rejectApplication(Long id) {
        MakeupArtistApplication app = getById(id);
        if (app == null) {
            throw new BusinessException("申请不存在");
        }
        if (app.getStatus() != ApplyStatus.PENDING) {
            throw new BusinessException("该申请已处理");
        }
        lambdaUpdate().eq(MakeupArtistApplication::getId, id).set(MakeupArtistApplication::getStatus, ApplyStatus.REJECTED).update();
        log.info("化妆师申请驳回, applicationId={}", id);
    }

    @Override
    public Long submitApplication(Long userId, String reason) {
        long count = lambdaQuery()
                .eq(MakeupArtistApplication::getUserId, userId)
                .eq(MakeupArtistApplication::getStatus, ApplyStatus.PENDING)
                .count();
        if (count > 0) {
            throw new BusinessException("您已有待审核的申请，请等待审核");
        }
        MakeupArtistApplication app = new MakeupArtistApplication();
        app.setUserId(userId);
        app.setReason(reason);
        app.setStatus(ApplyStatus.PENDING);
        applicationMapper.insert(app);
        return app.getId();
    }
}
