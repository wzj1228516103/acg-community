package com.acg.community.entity;

import com.acg.community.common.BaseEntity;
import com.acg.community.enums.BookingStatus;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_makeup_booking")
public class MakeupBooking extends BaseEntity {

    private Long userId;

    private Long serviceId;

    private Long slotId;

    private BookingStatus status;

    private String notes;

    private String contactName;

    private String contactPhone;
}
