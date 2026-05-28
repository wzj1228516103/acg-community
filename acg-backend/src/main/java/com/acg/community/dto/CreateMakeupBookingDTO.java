package com.acg.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateMakeupBookingDTO {

    @NotNull(message = "服务ID不能为空")
    private Long serviceId;

    @NotNull(message = "请选择时间段")
    private Long slotId;

    @NotBlank(message = "联系人姓名不能为空")
    private String contactName;

    @NotBlank(message = "手机号不能为空")
    private String phone;

    private String notes;
}
