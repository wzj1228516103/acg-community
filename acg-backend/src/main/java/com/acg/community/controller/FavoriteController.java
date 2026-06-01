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

    private FavoriteType resolveType(String itemType) {
        if (itemType == null) {
            throw new com.acg.community.exception.BusinessException("收藏类型不能为空");
        }
        return switch (itemType.toLowerCase()) {
            case "product" -> FavoriteType.PRODUCT;
            case "makeup", "makeup_artist" -> FavoriteType.MAKEUP_ARTIST;
            case "makeup_service", "service" -> FavoriteType.MAKEUP_SERVICE;
            default -> {
                try {
                    yield FavoriteType.valueOf(itemType.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new com.acg.community.exception.BusinessException("无效的收藏类型: " + itemType);
                }
            }
        };
    }

    @PostMapping("/toggle")
    public Result<Void> toggleFavorite(@RequestParam String itemType,
                                       @RequestParam Long itemId) {
        Long userId = StpUtil.getLoginIdAsLong();
        favoriteService.toggleFavorite(userId, resolveType(itemType), itemId);
        return Result.success("操作成功", null);
    }

    @GetMapping("/check")
    public Result<Boolean> isFavorited(@RequestParam String itemType,
                                       @RequestParam Long itemId) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(favoriteService.isFavorited(userId, resolveType(itemType), itemId));
    }

    @GetMapping("/list")
    public Result<List<Favorite>> listFavorites(@RequestParam String itemType) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(favoriteService.getUserFavorites(userId, resolveType(itemType)));
    }
}
