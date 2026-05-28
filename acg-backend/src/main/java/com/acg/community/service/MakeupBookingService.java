package com.acg.community.service;

import com.acg.community.dto.CreateMakeupBookingDTO;
import com.acg.community.entity.MakeupBooking;
import com.baomidou.mybatisplus.extension.service.IService;

public interface MakeupBookingService extends IService<MakeupBooking> {

    Long createBooking(Long userId, CreateMakeupBookingDTO dto);
}
