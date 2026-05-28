package com.acg.community.service;

import com.acg.community.entity.Category;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface CategoryService extends IService<Category> {

    List<Category> listActive();
}
