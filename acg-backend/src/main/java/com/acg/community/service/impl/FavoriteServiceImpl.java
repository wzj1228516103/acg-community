package com.acg.community.service.impl;

import com.acg.community.entity.Favorite;
import com.acg.community.enums.FavoriteType;
import com.acg.community.mapper.FavoriteMapper;
import com.acg.community.service.FavoriteService;
import com.acg.community.util.RedisUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    private static final long FAVORITE_LIST_TTL = 180;
    private static final long FAVORITED_FLAG_TTL = 600;
    private static final String FAVORITE_LIST_KEY = "acg:favorite:list:";
    private static final String FAVORITED_KEY = "acg:favorite:check:";

    @Resource
    private FavoriteMapper favoriteMapper;

    @Resource
    private RedisUtil redisUtil;

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
        redisUtil.delete(FAVORITED_KEY + userId + ":" + itemType.getCode() + ":" + itemId);
        redisUtil.deleteByPrefix(FAVORITE_LIST_KEY + userId);
    }

    @Override
    public boolean isFavorited(Long userId, FavoriteType itemType, Long itemId) {
        String cacheKey = FAVORITED_KEY + userId + ":" + itemType.getCode() + ":" + itemId;
        Boolean cached = redisUtil.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        boolean result = lambdaQuery()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getItemType, itemType)
                .eq(Favorite::getItemId, itemId)
                .count() > 0;
        redisUtil.set(cacheKey, result, FAVORITED_FLAG_TTL);
        return result;
    }

    @Override
    public List<Favorite> getUserFavorites(Long userId, FavoriteType itemType) {
        String cacheKey = FAVORITE_LIST_KEY + userId + ":" + itemType.getCode();
        List<Favorite> cached = redisUtil.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        List<Favorite> list = lambdaQuery()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getItemType, itemType)
                .orderByDesc(Favorite::getCreatedAt)
                .list();
        redisUtil.set(cacheKey, list, FAVORITE_LIST_TTL);
        return list;
    }
}
