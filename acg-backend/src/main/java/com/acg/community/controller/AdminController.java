package com.acg.community.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.acg.community.common.PageResult;
import com.acg.community.common.Result;
import com.acg.community.entity.*;
import com.acg.community.enums.ApplyStatus;
import com.acg.community.enums.GoodsStatus;
import com.acg.community.enums.OrderStatus;
import com.acg.community.enums.Role;
import com.acg.community.exception.BusinessException;
import com.acg.community.service.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final MakeupServiceService makeupServiceService;
    private final MakeupArtistApplicationService makeupArtistApplicationService;
    private final MerchantApplicationService merchantApplicationService;

    public AdminController(UserService userService,
                           ProductService productService,
                           CategoryService categoryService,
                           OrderService orderService,
                           MakeupServiceService makeupServiceService,
                           MakeupArtistApplicationService makeupArtistApplicationService,
                           MerchantApplicationService merchantApplicationService) {
        this.userService = userService;
        this.productService = productService;
        this.categoryService = categoryService;
        this.orderService = orderService;
        this.makeupServiceService = makeupServiceService;
        this.makeupArtistApplicationService = makeupArtistApplicationService;
        this.merchantApplicationService = merchantApplicationService;
    }

    private void checkAdmin() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userService.getById(userId);
        if (user == null || user.getRole().getCode() < 3) {
            throw new BusinessException(403, "无权限访问");
        }
    }

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        checkAdmin();

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        long userCount = userService.count();
        LambdaQueryWrapper<User> todayUserWrapper = new LambdaQueryWrapper<>();
        todayUserWrapper.ge(User::getCreatedAt, todayStart);
        long todayUserCount = userService.count(todayUserWrapper);

        long orderCount = orderService.count();
        LambdaQueryWrapper<Order> todayOrderWrapper = new LambdaQueryWrapper<>();
        todayOrderWrapper.ge(Order::getCreatedAt, todayStart);
        long todayOrderCount = orderService.count(todayOrderWrapper);

        LambdaQueryWrapper<Order> completedWrapper = new LambdaQueryWrapper<>();
        completedWrapper.ne(Order::getStatus, OrderStatus.CANCELLED);
        List<Order> orders = orderService.list(completedWrapper);
        BigDecimal totalAmount = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long productCount = productService.count();

        long makeupServiceCount = makeupServiceService.count();

        LambdaQueryWrapper<MakeupService> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(MakeupService::getStatus, GoodsStatus.INACTIVE);
        long pendingApplications = makeupServiceService.count(pendingWrapper);

        List<String> dates = new ArrayList<>();
        List<Long> dailyUsers = new ArrayList<>();
        List<Long> dailyOrders = new ArrayList<>();
        List<BigDecimal> dailyAmounts = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            dates.add(day.getMonthValue() + "/" + day.getDayOfMonth());
            LocalDateTime dayStart = day.atStartOfDay();
            LocalDateTime dayEnd = day.plusDays(1).atStartOfDay();

            LambdaQueryWrapper<User> uw = new LambdaQueryWrapper<>();
            uw.ge(User::getCreatedAt, dayStart).lt(User::getCreatedAt, dayEnd);
            dailyUsers.add(userService.count(uw));

            LambdaQueryWrapper<Order> ow = new LambdaQueryWrapper<>();
            ow.ge(Order::getCreatedAt, dayStart).lt(Order::getCreatedAt, dayEnd);
            dailyOrders.add(orderService.count(ow));

            List<Order> dayOrders = orderService.list(ow);
            BigDecimal dayAmount = dayOrders.stream()
                    .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            dailyAmounts.add(dayAmount);
        }

        long paidCount = orders.stream().filter(o -> o.getStatus() == OrderStatus.PAID).count();
        long shippedCount = orders.stream().filter(o -> o.getStatus() == OrderStatus.SHIPPED).count();
        long completedCount = orders.stream().filter(o -> o.getStatus() == OrderStatus.COMPLETED).count();

        Map<String, Object> data = new HashMap<>();
        data.put("userCount", userCount);
        data.put("todayUserCount", todayUserCount);
        data.put("orderCount", orderCount);
        data.put("todayOrderCount", todayOrderCount);
        data.put("totalAmount", totalAmount);
        data.put("productCount", productCount);
        data.put("makeupServiceCount", makeupServiceCount);
        data.put("pendingApplications", pendingApplications);
        data.put("chartDates", dates);
        data.put("chartDailyUsers", dailyUsers);
        data.put("chartDailyOrders", dailyOrders);
        data.put("chartDailyAmounts", dailyAmounts);
        data.put("orderStatusData", List.of(
                Map.of("name", "待付款", "value", orders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count()),
                Map.of("name", "已支付", "value", paidCount),
                Map.of("name", "已发货", "value", shippedCount),
                Map.of("name", "已完成", "value", completedCount)
        ));

        return Result.success(data);
    }

    @GetMapping("/users")
    public Result<PageResult<User>> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer role) {
        checkAdmin();

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getPhone, keyword));
        }
        if (role != null) {
            wrapper.eq(User::getRole, Role.of(role));
        }
        wrapper.orderByDesc(User::getCreatedAt);
        Page<User> result = userService.page(new Page<>(page, size), wrapper);
        PageResult<User> pageResult = new PageResult<>(
                result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return Result.success(pageResult);
    }

    @PutMapping("/users/{id}/status")
    public Result<Void> toggleUserStatus(@PathVariable Long id) {
        checkAdmin();

        User user = userService.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setDeleted(user.getDeleted() != null && user.getDeleted() == 1 ? 0 : 1);
        userService.updateById(user);
        return Result.success("操作成功", null);
    }

    @PutMapping("/users/{id}/role")
    public Result<Void> updateUserRole(@PathVariable Long id, @RequestParam int roleCode) {
        checkAdmin();

        User user = userService.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setRole(Role.of(roleCode));
        userService.updateById(user);
        return Result.success("角色更新成功", null);
    }

    @GetMapping("/products")
    public Result<Map<String, Object>> listProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        checkAdmin();

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Product::getCreatedAt);
        Page<Product> result = productService.page(new Page<>(page, size), wrapper);

        List<Long> categoryIds = result.getRecords().stream().map(Product::getCategoryId).filter(java.util.Objects::nonNull).distinct().collect(java.util.stream.Collectors.toList());
        Map<Long, String> categoryMap = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            List<Category> categories = categoryService.listByIds(categoryIds);
            categories.forEach(c -> categoryMap.put(c.getId(), c.getName()));
        }

        List<Long> merchantIds = result.getRecords().stream().map(Product::getMerchantId).filter(java.util.Objects::nonNull).distinct().collect(java.util.stream.Collectors.toList());
        Map<Long, String> merchantMap = new HashMap<>();
        if (!merchantIds.isEmpty()) {
            List<User> merchants = userService.listByIds(merchantIds);
            merchants.forEach(u -> merchantMap.put(u.getId(), u.getNickname() != null ? u.getNickname() : u.getUsername()));
        }

        List<Map<String, Object>> records = result.getRecords().stream().map(product -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", String.valueOf(product.getId()));
            item.put("name", product.getName());
            item.put("description", product.getDescription());
            item.put("price", product.getPrice());
            item.put("stock", product.getStock());
            item.put("images", product.getImages());
            item.put("categoryId", String.valueOf(product.getCategoryId()));
            item.put("categoryName", categoryMap.getOrDefault(product.getCategoryId(), "-"));
            item.put("merchantId", product.getMerchantId() != null ? String.valueOf(product.getMerchantId()) : null);
            item.put("merchantName", product.getMerchantId() != null ? merchantMap.getOrDefault(product.getMerchantId(), "-") : "-");
            item.put("status", product.getStatus().getCode());
            item.put("createdAt", product.getCreatedAt());
            item.put("updatedAt", product.getUpdatedAt());
            return item;
        }).collect(java.util.stream.Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", result.getTotal());
        data.put("current", result.getCurrent());
        data.put("size", result.getSize());
        return Result.success(data);
    }

    @PostMapping("/products")
    public Result<Void> createProduct(@RequestBody Product product) {
        checkAdmin();

        productService.save(product);
        return Result.success("创建成功", null);
    }

    @PutMapping("/products/{id}")
    public Result<Void> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        checkAdmin();

        product.setId(id);
        productService.updateById(product);
        return Result.success("更新成功", null);
    }

    @PutMapping("/products/{id}/status")
    public Result<Void> toggleProductStatus(@PathVariable Long id) {
        checkAdmin();

        Product product = productService.getById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        product.setStatus(product.getStatus() == GoodsStatus.ACTIVE ? GoodsStatus.INACTIVE : GoodsStatus.ACTIVE);
        productService.updateById(product);
        return Result.success("操作成功", null);
    }

    @GetMapping("/categories")
    public Result<List<Category>> listCategories() {
        checkAdmin();

        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSortOrder);
        return Result.success(categoryService.list(wrapper));
    }

    @PostMapping("/categories")
    public Result<Void> createCategory(@RequestBody Category category) {
        checkAdmin();

        categoryService.save(category);
        return Result.success("创建成功", null);
    }

    @PutMapping("/categories/{id}")
    public Result<Void> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        checkAdmin();

        category.setId(id);
        categoryService.updateById(category);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/categories/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        checkAdmin();

        categoryService.removeById(id);
        return Result.success("删除成功", null);
    }

    @GetMapping("/orders")
    public Result<Map<String, Object>> listOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        checkAdmin();

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            try {
                wrapper.eq(Order::getStatus, OrderStatus.valueOf(status));
            } catch (IllegalArgumentException ignored) {
            }
        }
        wrapper.orderByDesc(Order::getCreatedAt);
        Page<Order> result = orderService.page(new Page<>(page, size), wrapper);

        List<Long> userIds = result.getRecords().stream().map(Order::getUserId).distinct().collect(java.util.stream.Collectors.toList());
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userService.listByIds(userIds);
            users.forEach(u -> userMap.put(u.getId(), u));
        }

        List<Map<String, Object>> records = result.getRecords().stream().map(order -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", String.valueOf(order.getId()));
            item.put("userId", String.valueOf(order.getUserId()));
            item.put("totalAmount", order.getTotalAmount());
            item.put("status", order.getStatus().name());
            item.put("receiverName", order.getReceiverName());
            item.put("receiverPhone", order.getReceiverPhone());
            item.put("receiverAddress", order.getReceiverAddress());
            item.put("createdAt", order.getCreatedAt());
            item.put("updatedAt", order.getUpdatedAt());
            User user = userMap.get(order.getUserId());
            if (user != null) {
                item.put("username", user.getUsername());
                item.put("nickname", user.getNickname());
            }
            return item;
        }).collect(java.util.stream.Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", result.getTotal());
        data.put("current", result.getCurrent());
        data.put("size", result.getSize());
        return Result.success(data);
    }

    @PutMapping("/orders/{id}/status")
    public Result<Void> updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        checkAdmin();

        OrderStatus orderStatus;
        try {
            orderStatus = OrderStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("无效的订单状态");
        }
        orderService.updateOrderStatus(id, orderStatus);
        return Result.success("订单状态更新成功", null);
    }

    @GetMapping("/makeup-services")
    public Result<PageResult<MakeupService>> listMakeupServices(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        checkAdmin();

        LambdaQueryWrapper<MakeupService> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(MakeupService::getCreatedAt);
        Page<MakeupService> result = makeupServiceService.page(new Page<>(page, size), wrapper);
        PageResult<MakeupService> pageResult = new PageResult<>(
                result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return Result.success(pageResult);
    }

    @PutMapping("/makeup-services/{id}/status")
    public Result<Void> toggleMakeupServiceStatus(@PathVariable Long id) {
        checkAdmin();

        MakeupService service = makeupServiceService.getById(id);
        if (service == null) {
            throw new BusinessException("化妆服务不存在");
        }
        service.setStatus(service.getStatus() == GoodsStatus.ACTIVE ? GoodsStatus.INACTIVE : GoodsStatus.ACTIVE);
        makeupServiceService.updateById(service);
        return Result.success("操作成功", null);
    }

    @GetMapping("/applications/artist")
    public Result<PageResult<MakeupArtistApplication>> listArtistApplications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        checkAdmin();

        Page<MakeupArtistApplication> result = makeupArtistApplicationService.listApplications(page, size, null);
        PageResult<MakeupArtistApplication> pageResult = new PageResult<>(
                result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return Result.success(pageResult);
    }

    @PutMapping("/applications/artist/{id}")
    public Result<Void> reviewArtistApplication(@PathVariable Long id, @RequestParam String action) {
        checkAdmin();

        if ("approve".equals(action)) {
            makeupArtistApplicationService.approveApplication(id);
        } else if ("reject".equals(action)) {
            makeupArtistApplicationService.rejectApplication(id);
        } else {
            throw new BusinessException("无效的操作");
        }
        return Result.success("审核成功", null);
    }

    @GetMapping("/applications/merchant")
    public Result<PageResult<MerchantApplication>> listMerchantApplications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        checkAdmin();

        Page<MerchantApplication> result = merchantApplicationService.listApplications(page, size, null);
        PageResult<MerchantApplication> pageResult = new PageResult<>(
                result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return Result.success(pageResult);
    }

    @PutMapping("/applications/merchant/{id}")
    public Result<Void> reviewMerchantApplication(@PathVariable Long id, @RequestParam String action) {
        checkAdmin();

        if ("approve".equals(action)) {
            merchantApplicationService.approveApplication(id);
        } else if ("reject".equals(action)) {
            merchantApplicationService.rejectApplication(id);
        } else {
            throw new BusinessException("无效的操作");
        }
        return Result.success("审核成功", null);
    }
}
