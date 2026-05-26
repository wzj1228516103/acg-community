package com.acg.community.service;

import com.acg.community.entity.Favorite;
import com.acg.community.enums.FavoriteType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface FavoriteService extends IService<Favorite> {

    void toggleFavorite(Long userId, FavoriteType itemType, Long itemId);

    boolean isFavorited(Long userId, FavoriteType itemType, Long itemId);

    List<Favorite> getUserFavorites(Long userId, FavoriteType itemType);
}
