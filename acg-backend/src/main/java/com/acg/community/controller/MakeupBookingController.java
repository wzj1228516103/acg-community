package com.acg.community.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.acg.community.common.Result;
import com.acg.community.dto.CreateMakeupBookingDTO;
import com.acg.community.service.MakeupBookingService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/makeup/booking")
public class MakeupBookingController {

    private final MakeupBookingService makeupBookingService;

    public MakeupBookingController(MakeupBookingService makeupBookingService) {
        this.makeupBookingService = makeupBookingService;
    }

    @PostMapping("/create")
    public Result<Long> createBooking(@Valid @RequestBody CreateMakeupBookingDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        Long bookingId = makeupBookingService.createBooking(userId, dto);
        return Result.success("预约成功", bookingId);
    }
}
