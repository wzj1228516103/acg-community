package com.acg.community.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.acg.community.common.Result;
import com.acg.community.entity.Favorite;
import com.acg.community.enums.FavoriteType;
import com.acg.community.service.FavoriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/favorite")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/toggle")
    public Result<Void> toggleFavorite(@RequestParam FavoriteType itemType,
                                       @RequestParam Long itemId) {
        Long userId = StpUtil.getLoginIdAsLong();
        favoriteService.toggleFavorite(userId, itemType, itemId);
        return Result.success("操作成功", null);
    }

    @GetMapping("/check")
    public Result<Boolean> isFavorited(@RequestParam FavoriteType itemType,
                                       @RequestParam Long itemId) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(favoriteService.isFavorited(userId, itemType, itemId));
    }

    @GetMapping("/list")
    public Result<List<Favorite>> listFavorites(@RequestParam FavoriteType itemType) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(favoriteService.getUserFavorites(userId, itemType));
    }
}
