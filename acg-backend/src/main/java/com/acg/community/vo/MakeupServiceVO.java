package com.acg.community.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MakeupServiceVO {

    private Long id;
    private Long artistId;
    private String artistName;
    private String artistNickname;
    private String artistAvatar;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer duration;
    private String images;
    private Integer status;
    private LocalDateTime createdAt;
}
