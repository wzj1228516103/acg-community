package com.acg.community.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.acg.community.common.PageResult;
import com.acg.community.common.Result;
import com.acg.community.entity.MakeupService;
import com.acg.community.entity.User;
import com.acg.community.enums.GoodsStatus;
import com.acg.community.enums.Role;
import com.acg.community.exception.BusinessException;
import com.acg.community.service.MakeupServiceService;
import com.acg.community.service.UserService;
import com.acg.community.vo.MakeupServiceVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/makeup")
public class MakeupController {

    private final MakeupServiceService makeupServiceService;
    private final UserService userService;

    public MakeupController(MakeupServiceService makeupServiceService, UserService userService) {
        this.makeupServiceService = makeupServiceService;
        this.userService = userService;
    }

    @PostMapping("/service/create")
    public Result<Void> createService(@RequestBody MakeupService service) {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userService.getById(userId);
        if (user == null || (user.getRole() != Role.MAKEUP_ARTIST && user.getRole().getCode() < 3)) {
            throw new BusinessException(403, "仅认证化妆师可发布服务");
        }
        service.setArtistId(userId);
        service.setStatus(GoodsStatus.INACTIVE);
        makeupServiceService.save(service);
        return Result.success("发布成功，等待审核", null);
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
