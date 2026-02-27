package com.huzhu.controller.module;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huzhu.common.ApiResponse;
import com.huzhu.common.PageResponse;
import com.huzhu.entity.Permission;
import com.huzhu.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/permissions")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @GetMapping("/page")
    public ApiResponse<PageResponse<Permission>> page(@RequestParam(defaultValue = "1") Long pageNum,
                                                      @RequestParam(defaultValue = "10") Long pageSize,
                                                      @RequestParam(required = false) String keyword) {
        Page<Permission> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Permission> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like("permission_name", keyword)
                    .or().like("permission_code", keyword)
                    .or().like("url", keyword));
        }
        wrapper.orderByAsc("sort").orderByDesc("id");
        permissionService.page(page, wrapper);
        return ApiResponse.success(PageResponse.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @PostMapping
    public ApiResponse<Boolean> create(@RequestBody Permission permission) {
        return ApiResponse.success("创建成功", permissionService.save(permission));
    }

    @PutMapping("/{id}")
    public ApiResponse<Boolean> update(@PathVariable Long id, @RequestBody Permission permission) {
        permission.setId(id);
        return ApiResponse.success("更新成功", permissionService.updateById(permission));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> remove(@PathVariable Long id) {
        return ApiResponse.success("删除成功", permissionService.removeById(id));
    }
}
