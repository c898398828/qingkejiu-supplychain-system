package com.huzhu.controller.module;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huzhu.common.ApiResponse;
import com.huzhu.common.PageResponse;
import com.huzhu.entity.ProductionProcess;
import com.huzhu.service.ProductionProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/production-processes")
public class ProductionProcessController {

    @Autowired
    private ProductionProcessService productionProcessService;

    @GetMapping("/page")
    public ApiResponse<PageResponse<ProductionProcess>> page(@RequestParam(defaultValue = "1") Long pageNum,
                                                             @RequestParam(defaultValue = "10") Long pageSize,
                                                             @RequestParam(required = false) String keyword,
                                                             @RequestParam(required = false) String status) {
        Page<ProductionProcess> page = new Page<>(pageNum, pageSize);
        QueryWrapper<ProductionProcess> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like("process_no", keyword)
                    .or().like("product_name", keyword)
                    .or().like("batch_no", keyword)
                    .or().like("operator", keyword));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("id");
        productionProcessService.page(page, wrapper);
        return ApiResponse.success(PageResponse.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @PostMapping
    public ApiResponse<Boolean> create(@RequestBody ProductionProcess item) {
        return ApiResponse.success("创建成功", productionProcessService.save(item));
    }

    @PutMapping("/{id}")
    public ApiResponse<Boolean> update(@PathVariable Long id, @RequestBody ProductionProcess item) {
        item.setId(id);
        return ApiResponse.success("更新成功", productionProcessService.updateById(item));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> remove(@PathVariable Long id) {
        return ApiResponse.success("删除成功", productionProcessService.removeById(id));
    }

    @GetMapping("/progress")
    public ApiResponse<Map<String, Object>> progress() {
        List<ProductionProcess> records = productionProcessService.list();
        long total = records.size();
        long todo = records.stream().filter(item -> "待处理".equals(item.getStatus())).count();
        long processing = records.stream().filter(item -> "处理中".equals(item.getStatus())).count();
        long done = records.stream().filter(item -> "已完成".equals(item.getStatus())).count();
        Map<String, Object> data = new HashMap<>();
        data.put("total", total);
        data.put("todo", todo);
        data.put("processing", processing);
        data.put("done", done);
        return ApiResponse.success(data);
    }
}
