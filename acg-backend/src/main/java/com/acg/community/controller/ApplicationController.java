package com.acg.community.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.acg.community.common.Result;
import com.acg.community.service.MakeupArtistApplicationService;
import com.acg.community.service.MerchantApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/application")
public class ApplicationController {

    private final MakeupArtistApplicationService artistApplicationService;
    private final MerchantApplicationService merchantApplicationService;

    public ApplicationController(MakeupArtistApplicationService artistApplicationService,
                                  MerchantApplicationService merchantApplicationService) {
        this.artistApplicationService = artistApplicationService;
        this.merchantApplicationService = merchantApplicationService;
    }

    @PostMapping("/artist")
    public Result<Long> submitArtistApplication(@RequestBody Map<String, String> body) {
        Long userId = StpUtil.getLoginIdAsLong();
        String reason = body.getOrDefault("reason", "");
        Long id = artistApplicationService.submitApplication(userId, reason);
        return Result.success("申请提交成功", id);
    }

    @PostMapping("/merchant")
    public Result<Long> submitMerchantApplication(@RequestBody Map<String, String> body) {
        Long userId = StpUtil.getLoginIdAsLong();
        String reason = body.getOrDefault("reason", "");
        String shopName = body.getOrDefault("shopName", "");
        String businessLicense = body.getOrDefault("businessLicense", "");
        Long id = merchantApplicationService.submitApplication(userId, reason, shopName, businessLicense);
        return Result.success("申请提交成功", id);
    }
}