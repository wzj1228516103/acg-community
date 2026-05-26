package com.acg.community.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.acg.community.dto.CreateOrderDTO;
import com.acg.community.entity.Order;
import com.acg.community.entity.OrderItem;
import com.acg.community.entity.Product;
import com.acg.community.enums.GoodsStatus;
import com.acg.community.enums.OrderStatus;
import com.acg.community.exception.BusinessException;
import com.acg.community.mapper.OrderItemMapper;
import com.acg.community.mapper.OrderMapper;
import com.acg.community.mapper.ProductMapper;
import com.acg.community.service.OrderService;
import com.acg.community.vo.OrderItemVO;
import com.acg.community.vo.OrderVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private OrderItemMapper orderItemMapper;

    @Resource
    private ProductMapper productMapper;

    @Override
    @Transactional
    public Long createOrder(Long userId, CreateOrderDTO dto) {
        List<CreateOrderDTO.OrderItemDTO> itemDTOs = dto.getItems();
        if (itemDTOs == null || itemDTOs.isEmpty()) {
            throw new BusinessException("订单商品不能为空");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        order.setReceiverName(dto.getReceiverName());
        order.setReceiverPhone(dto.getReceiverPhone());
        order.setReceiverAddress(dto.getReceiverAddress());

        // Calculate total
        for (CreateOrderDTO.OrderItemDTO itemDTO : itemDTOs) {
            Product product = productMapper.selectById(itemDTO.getProductId());
            if (product == null) {
                throw new BusinessException("商品不存在: " + itemDTO.getProductId());
            }
            if (product.getStock() < itemDTO.getQuantity()) {
                throw new BusinessException("商品库存不足: " + product.getName());
            }
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            totalAmount = totalAmount.add(subtotal);
        }

        order.setTotalAmount(totalAmount);
        orderMapper.insert(order);

        // Insert order items
        for (CreateOrderDTO.OrderItemDTO itemDTO : itemDTOs) {
            Product product = productMapper.selectById(itemDTO.getProductId());
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setProductId(itemDTO.getProductId());
            item.setProductName(product.getName());
            item.setProductImage(product.getImages());
            item.setQuantity(itemDTO.getQuantity());
            item.setPrice(product.getPrice());
            item.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
            orderItemMapper.insert(item);

            // Decrease stock
            product.setStock(product.getStock() - itemDTO.getQuantity());
            productMapper.updateById(product);
        }

        log.info("订单创建成功, orderId={}, userId={}, totalAmount={}", order.getId(), userId, totalAmount);
        return order.getId();
    }

    @Override
    public Page<OrderVO> getUserOrders(Long userId, int page, int size) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId).orderByDesc(Order::getCreatedAt);
        Page<Order> orderPage = orderMapper.selectPage(new Page<>(page, size), wrapper);

        Page<OrderVO> voPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        voPage.setRecords(orderPage.getRecords().stream().map(this::toOrderVO).toList());
        return voPage;
    }

    @Override
    public OrderVO getOrderDetail(Long orderId, Long userId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权查看该订单");
        }
        return toOrderVO(order);
    }

    @Override
    public void updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        lambdaUpdate().eq(Order::getId, orderId).set(Order::getStatus, status).update();
        log.info("订单状态更新, orderId={}, status={}", orderId, status);
    }

    private OrderVO toOrderVO(Order order) {
        OrderVO vo = new OrderVO();
        BeanUtil.copyProperties(order, vo);
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, order.getId());
        List<OrderItem> items = orderItemMapper.selectList(wrapper);
        List<OrderItemVO> itemVOs = items.stream().map(item -> {
            OrderItemVO itemVO = new OrderItemVO();
            BeanUtil.copyProperties(item, itemVO);
            return itemVO;
        }).toList();
        vo.setItems(itemVOs);
        return vo;
    }
}
