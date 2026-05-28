package com.acg.community.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.acg.community.common.Result;
import com.acg.community.dto.CreateSlotDTO;
import com.acg.community.service.ArtistSlotService;
import com.acg.community.vo.SlotVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/makeup/slot")
public class ArtistSlotController {

    private final ArtistSlotService artistSlotService;

    public ArtistSlotController(ArtistSlotService artistSlotService) {
        this.artistSlotService = artistSlotService;
    }

    @PostMapping("/create")
    public Result<Void> createSlots(@Valid @RequestBody CreateSlotDTO dto) {
        Long artistId = StpUtil.getLoginIdAsLong();
        artistSlotService.createSlots(artistId, dto);
        return Result.success("设置成功", null);
    }

    @GetMapping("/available")
    public Result<List<SlotVO>> getAvailableSlots(@RequestParam Long serviceId) {
        return Result.success(artistSlotService.getAvailableSlots(serviceId));
    }
}
