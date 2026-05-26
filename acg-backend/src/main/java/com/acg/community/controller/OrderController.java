package com.acg.community.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.acg.community.common.PageResult;
import com.acg.community.common.Result;
import com.acg.community.dto.CreateOrderDTO;
import com.acg.community.service.OrderService;
import com.acg.community.vo.OrderVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public Result<Long> createOrder(@Valid @RequestBody CreateOrderDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        Long orderId = orderService.createOrder(userId, dto);
        return Result.success("下单成功", orderId);
    }

    @GetMapping("/list")
    public Result<PageResult<OrderVO>> listOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = StpUtil.getLoginIdAsLong();
        Page<OrderVO> result = orderService.getUserOrders(userId, page, size);
        PageResult<OrderVO> pageResult = new PageResult<>(
                result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    public Result<OrderVO> getOrderDetail(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(orderService.getOrderDetail(id, userId));
    }

    @PutMapping("/{id}/pay")
    public Result<Void> payOrder(@PathVariable Long id) {
        orderService.updateOrderStatus(id, com.acg.community.enums.OrderStatus.PAID);
        return Result.success("支付成功", null);
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long id) {
        orderService.updateOrderStatus(id, com.acg.community.enums.OrderStatus.CANCELLED);
        return Result.success("取消成功", null);
    }

    @PutMapping("/{id}/receive")
    public Result<Void> confirmReceive(@PathVariable Long id) {
        orderService.updateOrderStatus(id, com.acg.community.enums.OrderStatus.COMPLETED);
        return Result.success("确认收货成功", null);
    }
}
