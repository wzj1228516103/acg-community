package com.acg.community.service;

import com.acg.community.entity.MakeupService;
import com.acg.community.vo.MakeupServiceVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

public interface MakeupServiceService extends IService<MakeupService> {

    Page<MakeupServiceVO> listActiveServices(int page, int size);

    MakeupServiceVO getServiceDetail(Long id);
}
