package com.acg.community.service.impl;

import com.acg.community.entity.MakeupBooking;
import com.acg.community.mapper.MakeupBookingMapper;
import com.acg.community.service.MakeupBookingService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MakeupBookingServiceImpl extends ServiceImpl<MakeupBookingMapper, MakeupBooking> implements MakeupBookingService {

    @Resource
    private MakeupBookingMapper makeupBookingMapper;
}
