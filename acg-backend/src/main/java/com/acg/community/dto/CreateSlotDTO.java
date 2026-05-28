package com.acg.community.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateSlotDTO {

    @NotNull(message = "服务ID不能为空")
    private Long serviceId;

    @NotNull(message = "时间段不能为空")
    private List<SlotTime> slots;

    @Data
    public static class SlotTime {

        @NotNull(message = "开始时间不能为空")
        private LocalDateTime startTime;

        @NotNull(message = "结束时间不能为空")
        private LocalDateTime endTime;
    }
}
