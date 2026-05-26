package com.acg.community.service.impl;

import com.acg.community.entity.Favorite;
import com.acg.community.enums.FavoriteType;
import com.acg.community.mapper.FavoriteMapper;
import com.acg.community.service.FavoriteService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    @Resource
    private FavoriteMapper favoriteMapper;

    @Override
    public void toggleFavorite(Long userId, FavoriteType itemType, Long itemId) {
        Favorite existing = lambdaQuery()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getItemType, itemType)
                .eq(Favorite::getItemId, itemId)
                .one();
        if (existing != null) {
            removeById(existing.getId());
            log.info("取消收藏: userId={}, itemType={}, itemId={}", userId, itemType, itemId);
        } else {
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setItemType(itemType);
            favorite.setItemId(itemId);
            favoriteMapper.insert(favorite);
            log.info("添加收藏: userId={}, itemType={}, itemId={}", userId, itemType, itemId);
        }
    }

    @Override
    public boolean isFavorited(Long userId, FavoriteType itemType, Long itemId) {
        return lambdaQuery()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getItemType, itemType)
                .eq(Favorite::getItemId, itemId)
                .count() > 0;
    }

    @Override
    public List<Favorite> getUserFavorites(Long userId, FavoriteType itemType) {
        return lambdaQuery()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getItemType, itemType)
                .orderByDesc(Favorite::getCreatedAt)
                .list();
    }
}
