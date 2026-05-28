package com.acg.community.entity;

import com.acg.community.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_artist_available_slot")
public class ArtistAvailableSlot extends BaseEntity {

    private Long artistId;

    private Long serviceId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Boolean booked;
}
