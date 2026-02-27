package com.huzhu.controller.module;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huzhu.common.ApiResponse;
import com.huzhu.common.PageResponse;
import com.huzhu.entity.QualityControl;
import com.huzhu.service.QualityControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/quality-controls")
public class QualityControlController {

    @Autowired
    private QualityControlService qualityControlService;

    @GetMapping("/page")
    public ApiResponse<PageResponse<QualityControl>> page(@RequestParam(defaultValue = "1") Long pageNum,
                                                          @RequestParam(defaultValue = "10") Long pageSize,
                                                          @RequestParam(required = false) String keyword,
                                                          @RequestParam(required = false) String status) {
        Page<QualityControl> page = new Page<>(pageNum, pageSize);
        QueryWrapper<QualityControl> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like("quality_no", keyword)
                    .or().like("product_name", keyword)
                    .or().like("batch_no", keyword)
                    .or().like("tester", keyword));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("id");
        qualityControlService.page(page, wrapper);
        return ApiResponse.success(PageResponse.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @PostMapping
    public ApiResponse<Boolean> create(@RequestBody QualityControl item) {
        return ApiResponse.success("创建成功", qualityControlService.save(item));
    }

    @PutMapping("/{id}")
    public ApiResponse<Boolean> update(@PathVariable Long id, @RequestBody QualityControl item) {
        item.setId(id);
        return ApiResponse.success("更新成功", qualityControlService.updateById(item));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> remove(@PathVariable Long id) {
        return ApiResponse.success("删除成功", qualityControlService.removeById(id));
    }
}
