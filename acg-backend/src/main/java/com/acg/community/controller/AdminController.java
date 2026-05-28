package com.acg.community.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.acg.community.common.PageResult;
import com.acg.community.common.Result;
import com.acg.community.entity.*;
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

    public AdminController(UserService userService,
                           ProductService productService,
                           CategoryService categoryService,
                           OrderService orderService,
                           MakeupServiceService makeupServiceService) {
        this.userService = userService;
        this.productService = productService;
        this.categoryService = categoryService;
        this.orderService = orderService;
        this.makeupServiceService = makeupServiceService;
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

        Map<String, Object> data = new HashMap<>();
        data.put("userCount", userCount);
        data.put("todayUserCount", todayUserCount);
        data.put("orderCount", orderCount);
        data.put("todayOrderCount", todayOrderCount);
        data.put("totalAmount", totalAmount);
        data.put("productCount", productCount);
        data.put("makeupServiceCount", makeupServiceCount);
        data.put("pendingApplications", pendingApplications);

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
    public Result<PageResult<Product>> listProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        checkAdmin();

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Product::getCreatedAt);
        Page<Product> result = productService.page(new Page<>(page, size), wrapper);
        PageResult<Product> pageResult = new PageResult<>(
                result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return Result.success(pageResult);
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
    public Result<PageResult<Order>> listOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        checkAdmin();

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Order::getStatus, OrderStatus.values()[status]);
        }
        wrapper.orderByDesc(Order::getCreatedAt);
        Page<Order> result = orderService.page(new Page<>(page, size), wrapper);
        PageResult<Order> pageResult = new PageResult<>(
                result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return Result.success(pageResult);
    }

    @PutMapping("/orders/{id}/status")
    public Result<Void> updateOrderStatus(@PathVariable Long id, @RequestParam int statusCode) {
        checkAdmin();

        OrderStatus status = OrderStatus.values()[statusCode];
        orderService.updateOrderStatus(id, status);
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
}
