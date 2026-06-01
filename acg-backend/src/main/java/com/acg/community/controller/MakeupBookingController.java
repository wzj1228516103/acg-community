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

    @PutMapping("/{id}/confirm")
    public Result<Void> confirmBooking(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        makeupBookingService.confirmBooking(id, userId);
        return Result.success("确认成功", null);
    }

    @PutMapping("/{id}/complete")
    public Result<Void> completeBooking(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        makeupBookingService.completeBooking(id, userId);
        return Result.success("完成成功", null);
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancelBooking(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        makeupBookingService.cancelBooking(id, userId);
        return Result.success("取消成功", null);
    }
}
