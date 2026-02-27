package com.huzhu.controller.module;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huzhu.common.ApiResponse;
import com.huzhu.common.PageResponse;
import com.huzhu.entity.Inventory;
import com.huzhu.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventories")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping("/page")
    public ApiResponse<PageResponse<Inventory>> page(@RequestParam(defaultValue = "1") Long pageNum,
                                                     @RequestParam(defaultValue = "10") Long pageSize,
                                                     @RequestParam(required = false) String keyword,
                                                     @RequestParam(required = false) Integer status) {
        Page<Inventory> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Inventory> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like("product_name", keyword)
                    .or().like("product_code", keyword)
                    .or().like("category", keyword)
                    .or().like("warehouse", keyword));
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("id");
        inventoryService.page(page, wrapper);
        return ApiResponse.success(PageResponse.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @PostMapping
    public ApiResponse<Boolean> create(@RequestBody Inventory item) {
        return ApiResponse.success("创建成功", inventoryService.save(item));
    }

    @PutMapping("/{id}")
    public ApiResponse<Boolean> update(@PathVariable Long id, @RequestBody Inventory item) {
        item.setId(id);
        return ApiResponse.success("更新成功", inventoryService.updateById(item));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> remove(@PathVariable Long id) {
        return ApiResponse.success("删除成功", inventoryService.removeById(id));
    }

    @GetMapping("/alerts")
    public ApiResponse<List<Inventory>> alerts(@RequestParam(defaultValue = "500") Double threshold) {
        QueryWrapper<Inventory> wrapper = new QueryWrapper<>();
        wrapper.lt("quantity", threshold).orderByAsc("quantity");
        return ApiResponse.success(inventoryService.list(wrapper));
    }
}
