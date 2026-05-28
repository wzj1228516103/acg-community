package com.acg.community.service;

import com.acg.community.entity.MerchantApplication;
import com.acg.community.enums.ApplyStatus;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

public interface MerchantApplicationService extends IService<MerchantApplication> {
    Page<MerchantApplication> listApplications(int page, int size, ApplyStatus status);
    void approveApplication(Long id);
    void rejectApplication(Long id);
}
