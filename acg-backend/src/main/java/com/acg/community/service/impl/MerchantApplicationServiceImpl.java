package com.acg.community.service.impl;

import com.acg.community.entity.MerchantApplication;
import com.acg.community.entity.User;
import com.acg.community.enums.ApplyStatus;
import com.acg.community.enums.Role;
import com.acg.community.exception.BusinessException;
import com.acg.community.mapper.MerchantApplicationMapper;
import com.acg.community.mapper.UserMapper;
import com.acg.community.service.MerchantApplicationService;
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
public class MerchantApplicationServiceImpl extends ServiceImpl<MerchantApplicationMapper, MerchantApplication> implements MerchantApplicationService {

    @Resource
    private MerchantApplicationMapper applicationMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisUtil redisUtil;

    @Override
    public Page<MerchantApplication> listApplications(int page, int size, ApplyStatus status) {
        LambdaQueryWrapper<MerchantApplication> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(MerchantApplication::getStatus, status);
        }
        wrapper.orderByDesc(MerchantApplication::getCreatedAt);
        return applicationMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    @Transactional
    public void approveApplication(Long id) {
        MerchantApplication app = getById(id);
        if (app == null) {
            throw new BusinessException("申请不存在");
        }
        if (app.getStatus() != ApplyStatus.PENDING) {
            throw new BusinessException("该申请已处理");
        }
        lambdaUpdate().eq(MerchantApplication::getId, id).set(MerchantApplication::getStatus, ApplyStatus.APPROVED).update();
        userMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<User>()
                .eq(User::getId, app.getUserId())
                .set(User::getRole, Role.MERCHANT));
        redisUtil.delete("acg:user:info:" + app.getUserId());
        log.info("商家申请通过, applicationId={}, userId={}", id, app.getUserId());
    }

    @Override
    public void rejectApplication(Long id) {
        MerchantApplication app = getById(id);
        if (app == null) {
            throw new BusinessException("申请不存在");
        }
        if (app.getStatus() != ApplyStatus.PENDING) {
            throw new BusinessException("该申请已处理");
        }
        lambdaUpdate().eq(MerchantApplication::getId, id).set(MerchantApplication::getStatus, ApplyStatus.REJECTED).update();
        log.info("商家申请驳回, applicationId={}", id);
    }

    @Override
    public Long submitApplication(Long userId, String reason, String shopName, String businessLicense) {
        long count = lambdaQuery()
                .eq(MerchantApplication::getUserId, userId)
                .eq(MerchantApplication::getStatus, ApplyStatus.PENDING)
                .count();
        if (count > 0) {
            throw new BusinessException("您已有待审核的申请，请等待审核");
        }
        MerchantApplication app = new MerchantApplication();
        app.setUserId(userId);
        app.setReason(reason);
        app.setShopName(shopName);
        app.setBusinessLicense(businessLicense);
        app.setStatus(ApplyStatus.PENDING);
        applicationMapper.insert(app);
        return app.getId();
    }
}
