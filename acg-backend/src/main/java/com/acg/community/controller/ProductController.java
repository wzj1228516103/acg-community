package com.acg.community.controller;

import com.acg.community.common.PageResult;
import com.acg.community.common.Result;
import com.acg.community.entity.Product;
import com.acg.community.service.ProductService;
import com.acg.community.vo.ProductVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
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
