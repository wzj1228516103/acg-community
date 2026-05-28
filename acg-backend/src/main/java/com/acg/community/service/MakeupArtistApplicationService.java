package com.acg.community.service;

import com.acg.community.entity.MakeupArtistApplication;
import com.acg.community.enums.ApplyStatus;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

public interface MakeupArtistApplicationService extends IService<MakeupArtistApplication> {
    Page<MakeupArtistApplication> listApplications(int page, int size, ApplyStatus status);
    void approveApplication(Long id);
    void rejectApplication(Long id);
}
