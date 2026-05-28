package com.acg.community.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SlotVO {

    private Long id;

    private Long serviceId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Boolean booked;
}
