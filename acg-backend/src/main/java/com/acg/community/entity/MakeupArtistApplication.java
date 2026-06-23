package com.acg.community.entity;

import com.acg.community.common.BaseEntity;
import com.acg.community.enums.ApplyStatus;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_makeup_artist_application")
public class MakeupArtistApplication extends BaseEntity {

    private Long userId;

    private String realName;

    private String idCard;

    private Integer experienceYears;

    private String portfolioImages;

    private String certificates;

    private String selfIntro;

    private ApplyStatus status;

    private String reason;
}
