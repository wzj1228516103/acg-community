package com.acg.community.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.acg.community.entity.Category;
import com.acg.community.entity.Product;
import com.acg.community.entity.User;
import com.acg.community.enums.GoodsStatus;
import com.acg.community.exception.BusinessException;
import com.acg.community.mapper.CategoryMapper;
import com.acg.community.mapper.ProductMapper;
import com.acg.community.mapper.UserMapper;
import com.acg.community.service.ProductService;
import com.acg.community.util.RedisUtil;
import com.acg.community.vo.ProductVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private static final long CACHE_TTL = 300;
    private static final String CACHE_KEY_LIST = "acg:product:list:";
    private static final String CACHE_KEY_DETAIL = "acg:product:detail:";

    @Resource
    private ProductMapper productMapper;

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisUtil redisUtil;

    @Override
    public Page<ProductVO> listProducts(String keyword, Long categoryId, int page, int size) {
        String cacheKey = CACHE_KEY_LIST + (keyword != null ? keyword : "all") + ":" + (categoryId != null ? categoryId : 0) + ":" + page + ":" + size;

        Page<ProductVO> cached = redisUtil.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, GoodsStatus.ACTIVE);
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(Product::getName, keyword);
        }
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        wrapper.orderByDesc(Product::getCreatedAt);
        Page<Product> productPage = productMapper.selectPage(new Page<>(page, size), wrapper);

        if (productPage.getRecords().isEmpty()) {
            Page<ProductVO> emptyPage = new Page<>(productPage.getCurrent(), productPage.getSize(), 0);
            redisUtil.set(cacheKey, emptyPage, 60);
            return emptyPage;
        }

        Set<Long> categoryIds = productPage.getRecords().stream()
                .map(Product::getCategoryId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> merchantIds = productPage.getRecords().stream()
                .map(Product::getMerchantId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, String> categoryNameMap = Collections.emptyMap();
        if (!categoryIds.isEmpty()) {
            categoryNameMap = categoryMapper.selectBatchIds(categoryIds).stream()
                    .collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));
        }

        Map<Long, String> merchantNameMap = Collections.emptyMap();
        if (!merchantIds.isEmpty()) {
            merchantNameMap = userMapper.selectBatchIds(merchantIds).stream()
                    .collect(Collectors.toMap(User::getId, u -> u.getNickname() != null ? u.getNickname() : u.getUsername(), (a, b) -> a));
        }

        Map<Long, String> finalCategoryNameMap = categoryNameMap;
        Map<Long, String> finalMerchantNameMap = merchantNameMap;

        Page<ProductVO> voPage = new Page<>(productPage.getCurrent(), productPage.getSize(), productPage.getTotal());
        voPage.setRecords(productPage.getRecords().stream().map(p -> {
            ProductVO vo = new ProductVO();
            BeanUtil.copyProperties(p, vo);
            vo.setCategoryName(finalCategoryNameMap.get(p.getCategoryId()));
            vo.setMerchantName(finalMerchantNameMap.get(p.getMerchantId()));
            return vo;
        }).toList());

        redisUtil.set(cacheKey, voPage, CACHE_TTL);
        return voPage;
    }

    @Override
    public ProductVO getProductDetail(Long id) {
        String cacheKey = CACHE_KEY_DETAIL + id;

        ProductVO cached = redisUtil.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        Product product = getById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        ProductVO vo = toProductVO(product);
        redisUtil.set(cacheKey, vo, CACHE_TTL);
        return vo;
    }

    @Override
    public void createProduct(Product product) {
        if (StrUtil.isBlank(product.getName())) {
            throw new BusinessException("商品名称不能为空");
        }
        if (product.getPrice() == null) {
            throw new BusinessException("商品价格不能为空");
        }
        product.setStatus(GoodsStatus.ACTIVE);
        productMapper.insert(product);
        redisUtil.deleteByPrefix(CACHE_KEY_LIST);
        log.info("商品创建成功: {}", product.getName());
    }

    @Override
    public void updateProduct(Product product) {
        Product existing = getById(product.getId());
        if (existing == null) {
            throw new BusinessException("商品不存在");
        }
        productMapper.updateById(product);
        redisUtil.delete(CACHE_KEY_DETAIL + product.getId());
        redisUtil.deleteByPrefix(CACHE_KEY_LIST);
    }

    private ProductVO toProductVO(Product product) {
        ProductVO vo = new ProductVO();
        BeanUtil.copyProperties(product, vo);
        if (product.getCategoryId() != null) {
            Category category = categoryMapper.selectById(product.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }
        if (product.getMerchantId() != null) {
            User merchant = userMapper.selectById(product.getMerchantId());
            if (merchant != null) {
                vo.setMerchantName(merchant.getNickname() != null ? merchant.getNickname() : merchant.getUsername());
            }
        }
        return vo;
    }
}
