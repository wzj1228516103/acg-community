package com.acg.community.entity;

import com.acg.community.common.BaseEntity;
import com.acg.community.enums.ApplyStatus;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_merchant_application")
public class MerchantApplication extends BaseEntity {

    private Long userId;

    private String businessName;

    private String businessLicense;

    private String contactInfo;

    private ApplyStatus status;
}
