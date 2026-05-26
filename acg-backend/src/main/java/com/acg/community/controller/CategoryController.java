package com.acg.community.controller;

import com.acg.community.common.Result;
import com.acg.community.entity.Category;
import com.acg.community.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/list")
    public Result<List<Category>> listCategories() {
        List<Category> list = categoryService.list(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getIsActive, true)
                        .orderByAsc(Category::getSortOrder));
        return Result.success(list);
    }
}
