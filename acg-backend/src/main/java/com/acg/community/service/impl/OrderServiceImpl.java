package com.acg.community.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.acg.community.dto.CreateOrderDTO;
import com.acg.community.entity.Order;
import com.acg.community.entity.OrderItem;
import com.acg.community.entity.Product;
import com.acg.community.enums.OrderStatus;
import com.acg.community.exception.BusinessException;
import com.acg.community.mapper.OrderItemMapper;
import com.acg.community.mapper.OrderMapper;
import com.acg.community.mapper.ProductMapper;
import com.acg.community.service.OrderService;
import com.acg.community.util.RedisUtil;
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

    private static final long ORDER_LIST_TTL = 120;
    private static final long ORDER_DETAIL_TTL = 300;
    private static final String ORDER_LIST_KEY = "acg:order:list:";
    private static final String ORDER_DETAIL_KEY = "acg:order:detail:";

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private OrderItemMapper orderItemMapper;

    @Resource
    private ProductMapper productMapper;

    @Resource
    private RedisUtil redisUtil;

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

        for (CreateOrderDTO.OrderItemDTO itemDTO : itemDTOs) {
            Product product = productMapper.selectById(itemDTO.getProductId());
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setProductId(itemDTO.getProductId());
            item.setProductName(product.getName());

            String images = product.getImages();
            if (images != null && images.length() > 500) {
                images = images.substring(0, 500);
            }
            item.setProductImage(images);

            item.setQuantity(itemDTO.getQuantity());
            item.setPrice(product.getPrice());
            item.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
            orderItemMapper.insert(item);

            product.setStock(product.getStock() - itemDTO.getQuantity());
            productMapper.updateById(product);
        }

        redisUtil.deleteByPrefix(ORDER_LIST_KEY + userId);
        log.info("订单创建成功, orderId={}, userId={}, totalAmount={}", order.getId(), userId, totalAmount);
        return order.getId();
    }

    @Override
    public Page<OrderVO> getUserOrders(Long userId, int page, int size) {
        String cacheKey = ORDER_LIST_KEY + userId + ":" + page + ":" + size;

        Page<OrderVO> cached = redisUtil.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId).orderByDesc(Order::getCreatedAt);
        Page<Order> orderPage = orderMapper.selectPage(new Page<>(page, size), wrapper);

        Page<OrderVO> voPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        voPage.setRecords(orderPage.getRecords().stream().map(this::toOrderVO).toList());

        redisUtil.set(cacheKey, voPage, ORDER_LIST_TTL);
        return voPage;
    }

    @Override
    public OrderVO getOrderDetail(Long orderId, Long userId) {
        String cacheKey = ORDER_DETAIL_KEY + orderId;

        OrderVO cached = redisUtil.get(cacheKey);
        if (cached != null) {
            if (!cached.getUserId().equals(userId)) {
                throw new BusinessException("无权查看该订单");
            }
            return cached;
        }

        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权查看该订单");
        }
        OrderVO vo = toOrderVO(order);
        redisUtil.set(cacheKey, vo, ORDER_DETAIL_TTL);
        return vo;
    }

    @Override
    public void updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        lambdaUpdate().eq(Order::getId, orderId).set(Order::getStatus, status).update();
        redisUtil.delete(ORDER_DETAIL_KEY + orderId);
        redisUtil.deleteByPrefix(ORDER_LIST_KEY + order.getUserId());
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
