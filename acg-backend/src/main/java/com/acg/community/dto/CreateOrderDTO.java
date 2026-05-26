package com.acg.community.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateOrderDTO {

    @NotBlank(message = "收货人姓名不能为空")
    private String receiverName;

    @NotBlank(message = "收货人电话不能为空")
    private String receiverPhone;

    @NotBlank(message = "收货地址不能为空")
    private String receiverAddress;

    @NotEmpty(message = "订单商品不能为空")
    @Valid
    private List<OrderItemDTO> items;

    @Data
    public static class OrderItemDTO {

        @NotNull(message = "商品ID不能为空")
        private Long productId;

        @NotNull(message = "商品数量不能为空")
        private Integer quantity;
    }
}
