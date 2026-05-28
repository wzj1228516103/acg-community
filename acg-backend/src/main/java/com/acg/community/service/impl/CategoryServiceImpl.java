package com.acg.community.service.impl;

import com.acg.community.entity.Category;
import com.acg.community.mapper.CategoryMapper;
import com.acg.community.service.CategoryService;
import com.acg.community.util.RedisUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private static final long CACHE_TTL = 600;
    private static final String CACHE_KEY = "acg:category:list";

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private RedisUtil redisUtil;

    public List<Category> listActive() {
        List<Category> cached = redisUtil.get(CACHE_KEY);
        if (cached != null) {
            return cached;
        }

        List<Category> list = lambdaQuery()
                .eq(Category::getIsActive, true)
                .orderByAsc(Category::getSortOrder)
                .list();

        redisUtil.set(CACHE_KEY, list, CACHE_TTL);
        return list;
    }
}
