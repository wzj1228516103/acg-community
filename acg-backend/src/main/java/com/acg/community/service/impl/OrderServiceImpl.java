package com.acg.community.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.acg.community.dto.CreateOrderDTO;
import com.acg.community.entity.Order;
import com.acg.community.entity.OrderItem;
import com.acg.community.entity.Product;
import com.acg.community.entity.User;
import com.acg.community.enums.OrderStatus;
import com.acg.community.exception.BusinessException;
import com.acg.community.mapper.OrderItemMapper;
import com.acg.community.mapper.OrderMapper;
import com.acg.community.mapper.ProductMapper;
import com.acg.community.mapper.UserMapper;
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
    private UserMapper userMapper;

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
        for (CreateOrderDTO.OrderItemDTO itemDTO : itemDTOs) {
            Product product = productMapper.selectById(itemDTO.getProductId());
            if (product == null) {
                throw new BusinessException("商品不存在: " + itemDTO.getProductId());
            }
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            totalAmount = totalAmount.add(subtotal);
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        order.setReceiverName(dto.getReceiverName());
        order.setReceiverPhone(dto.getReceiverPhone());
        order.setReceiverAddress(dto.getReceiverAddress());
        order.setTotalAmount(totalAmount);
        orderMapper.insert(order);

        for (CreateOrderDTO.OrderItemDTO itemDTO : itemDTOs) {
            Product product = productMapper.selectById(itemDTO.getProductId());
            int affected = productMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Product>()
                    .eq(Product::getId, itemDTO.getProductId())
                    .ge(Product::getStock, itemDTO.getQuantity())
                    .setSql("stock = stock - " + itemDTO.getQuantity()));
            if (affected == 0) {
                throw new BusinessException("商品库存不足: " + product.getName());
            }

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
        updateOrderStatus(orderId, null, status);
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, Long userId, OrderStatus status) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (userId != null && !order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该订单");
        }
        if (!isValidTransition(order.getStatus(), status)) {
            throw new BusinessException("订单状态不允许从 " + order.getStatus().getDesc() + " 变更为 " + status.getDesc());
        }

        lambdaUpdate().eq(Order::getId, orderId).set(Order::getStatus, status).update();

        if (status == OrderStatus.CANCELLED) {
            restoreStock(orderId);
        }

        redisUtil.delete(ORDER_DETAIL_KEY + orderId);
        redisUtil.deleteByPrefix(ORDER_LIST_KEY + order.getUserId());
        log.info("订单状态更新, orderId={}, status={}", orderId, status);
    }

    private boolean isValidTransition(OrderStatus from, OrderStatus to) {
        return switch (from) {
            case PENDING -> to == OrderStatus.PAID || to == OrderStatus.CANCELLED;
            case PAID -> to == OrderStatus.SHIPPED || to == OrderStatus.CANCELLED;
            case SHIPPED -> to == OrderStatus.COMPLETED;
            default -> false;
        };
    }

    private void restoreStock(Long orderId) {
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(wrapper);
        for (OrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                productMapper.updateById(product);
            }
        }
        log.info("订单取消恢复库存, orderId={}", orderId);
    }

    private OrderVO toOrderVO(Order order) {
        OrderVO vo = new OrderVO();
        BeanUtil.copyProperties(order, vo);
        User user = userMapper.selectById(order.getUserId());
        if (user != null) {
            vo.setUsername(user.getNickname() != null ? user.getNickname() : user.getUsername());
        }
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
