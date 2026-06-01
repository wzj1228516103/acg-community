package com.acg.community.service;

import com.acg.community.dto.CreateOrderDTO;
import com.acg.community.entity.Order;
import com.acg.community.entity.OrderItem;
import com.acg.community.enums.OrderStatus;
import com.acg.community.vo.OrderVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface OrderService extends IService<Order> {

    Long createOrder(Long userId, CreateOrderDTO dto);

    Page<OrderVO> getUserOrders(Long userId, int page, int size);

    OrderVO getOrderDetail(Long orderId, Long userId);

    void updateOrderStatus(Long orderId, OrderStatus status);

    void updateOrderStatus(Long orderId, Long userId, OrderStatus status);
}
