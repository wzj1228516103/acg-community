package com.acg.community.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.acg.community.common.PageResult;
import com.acg.community.common.Result;
import com.acg.community.entity.Product;
import com.acg.community.entity.User;
import com.acg.community.enums.GoodsStatus;
import com.acg.community.enums.Role;
import com.acg.community.exception.BusinessException;
import com.acg.community.service.ProductService;
import com.acg.community.service.UserService;
import com.acg.community.vo.ProductVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;
    private final UserService userService;

    public ProductController(ProductService productService, UserService userService) {
        this.productService = productService;
        this.userService = userService;
    }

    @PostMapping("/create")
    public Result<Void> createProduct(@RequestBody Product product) {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userService.getById(userId);
        if (user == null || (user.getRole() != Role.MERCHANT && user.getRole().getCode() < 3)) {
            throw new BusinessException(403, "仅认证商家可发布商品");
        }
        product.setMerchantId(userId);
        product.setStatus(GoodsStatus.INACTIVE);
        productService.save(product);
        return Result.success("发布成功，等待审核", null);
    }

    @GetMapping("/list")
    public Result<PageResult<ProductVO>> listProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ProductVO> result = productService.listProducts(keyword, categoryId, page, size);
        PageResult<ProductVO> pageResult = new PageResult<>(
                result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    public Result<ProductVO> getProductDetail(@PathVariable Long id) {
        return Result.success(productService.getProductDetail(id));
    }
}
