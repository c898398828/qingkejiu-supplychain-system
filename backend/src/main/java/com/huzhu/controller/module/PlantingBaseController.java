package com.huzhu.controller.module;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huzhu.common.ApiResponse;
import com.huzhu.common.PageResponse;
import com.huzhu.entity.PlantingBase;
import com.huzhu.service.PlantingBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/planting-bases")
public class PlantingBaseController {

    @Autowired
    private PlantingBaseService plantingBaseService;

    @GetMapping("/page")
    public ApiResponse<PageResponse<PlantingBase>> page(@RequestParam(defaultValue = "1") Long pageNum,
                                                        @RequestParam(defaultValue = "10") Long pageSize,
                                                        @RequestParam(required = false) String keyword,
                                                        @RequestParam(required = false) Integer status) {
        Page<PlantingBase> page = new Page<>(pageNum, pageSize);
        QueryWrapper<PlantingBase> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like("base_name", keyword)
                    .or().like("location", keyword)
                    .or().like("manager", keyword)
                    .or().like("contact_phone", keyword));
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("id");
        plantingBaseService.page(page, wrapper);
        return ApiResponse.success(PageResponse.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @PostMapping
    public ApiResponse<Boolean> create(@RequestBody PlantingBase item) {
        return ApiResponse.success("创建成功", plantingBaseService.save(item));
    }

    @PutMapping("/{id}")
    public ApiResponse<Boolean> update(@PathVariable Long id, @RequestBody PlantingBase item) {
        item.setId(id);
        return ApiResponse.success("更新成功", plantingBaseService.updateById(item));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> remove(@PathVariable Long id) {
        return ApiResponse.success("删除成功", plantingBaseService.removeById(id));
    }
}
