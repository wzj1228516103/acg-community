package com.acg.community.controller;

import com.acg.community.common.PageResult;
import com.acg.community.common.Result;
import com.acg.community.service.MakeupServiceService;
import com.acg.community.vo.MakeupServiceVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/makeup")
public class MakeupController {

    private final MakeupServiceService makeupServiceService;

    public MakeupController(MakeupServiceService makeupServiceService) {
        this.makeupServiceService = makeupServiceService;
    }

    @GetMapping("/services")
    public Result<PageResult<MakeupServiceVO>> listServices(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<MakeupServiceVO> result = makeupServiceService.listActiveServices(page, size);
        PageResult<MakeupServiceVO> pageResult = new PageResult<>(
                result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return Result.success(pageResult);
    }

    @GetMapping("/service/{id}")
    public Result<MakeupServiceVO> getServiceDetail(@PathVariable Long id) {
        return Result.success(makeupServiceService.getServiceDetail(id));
    }
}
