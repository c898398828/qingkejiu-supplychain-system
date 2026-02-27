package com.huzhu.controller.module;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huzhu.common.ApiResponse;
import com.huzhu.common.PageResponse;
import com.huzhu.entity.Order;
import com.huzhu.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/page")
    public ApiResponse<PageResponse<Order>> page(@RequestParam(defaultValue = "1") Long pageNum,
                                                 @RequestParam(defaultValue = "10") Long pageSize,
                                                 @RequestParam(required = false) String keyword,
                                                 @RequestParam(required = false) String status) {
        Page<Order> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like("order_no", keyword)
                    .or().like("customer_name", keyword)
                    .or().like("customer_phone", keyword)
                    .or().like("address", keyword));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("id");
        orderService.page(page, wrapper);
        return ApiResponse.success(PageResponse.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @PostMapping
    public ApiResponse<Boolean> create(@RequestBody Order item) {
        return ApiResponse.success("创建成功", orderService.save(item));
    }

    @PutMapping("/{id}")
    public ApiResponse<Boolean> update(@PathVariable Long id, @RequestBody Order item) {
        item.setId(id);
        return ApiResponse.success("更新成功", orderService.updateById(item));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> remove(@PathVariable Long id) {
        return ApiResponse.success("删除成功", orderService.removeById(id));
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats() {
        List<Order> records = orderService.list();
        long total = records.size();
        long paid = records.stream().filter(item -> "已完成".equals(item.getStatus())).count();
        long processing = records.stream().filter(item -> "处理中".equals(item.getStatus())).count();
        long pending = records.stream().filter(item -> "待支付".equals(item.getStatus())).count();
        BigDecimal totalAmount = records.stream()
                .map(Order::getTotalAmount)
                .filter(item -> item != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Object> data = new HashMap<>();
        data.put("total", total);
        data.put("paid", paid);
        data.put("processing", processing);
        data.put("pending", pending);
        data.put("totalAmount", totalAmount);
        return ApiResponse.success(data);
    }
}
