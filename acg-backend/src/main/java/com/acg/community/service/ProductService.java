package com.acg.community.service;

import com.acg.community.entity.Product;
import com.acg.community.vo.ProductVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ProductService extends IService<Product> {

    Page<ProductVO> listProducts(String keyword, Long categoryId, int page, int size);

    ProductVO getProductDetail(Long id);

    void createProduct(Product product);

    void updateProduct(Product product);
}
