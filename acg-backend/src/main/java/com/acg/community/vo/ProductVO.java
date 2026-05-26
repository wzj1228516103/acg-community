package com.acg.community.vo;

import com.acg.community.entity.Product;
import com.acg.community.enums.GoodsStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductVO {

    private Long id;

    private String name;

    private String description;

    private BigDecimal price;

    private Integer stock;

    private String images;

    private GoodsStatus status;

    private String categoryName;

    private Long merchantId;

    private String merchantName;

    private String merchantNickname;

    private LocalDateTime createdAt;

    public static ProductVO toVO(Product product, String categoryName, String merchantName, String merchantNickname) {
        ProductVO vo = new ProductVO();
        vo.setId(product.getId());
        vo.setName(product.getName());
        vo.setDescription(product.getDescription());
        vo.setPrice(product.getPrice());
        vo.setStock(product.getStock());
        vo.setImages(product.getImages());
        vo.setStatus(product.getStatus());
        vo.setCategoryName(categoryName);
        vo.setMerchantId(product.getMerchantId());
        vo.setMerchantName(merchantName);
        vo.setMerchantNickname(merchantNickname);
        vo.setCreatedAt(product.getCreatedAt());
        return vo;
    }
}
