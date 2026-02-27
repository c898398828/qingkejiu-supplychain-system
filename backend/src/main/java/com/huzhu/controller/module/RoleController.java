package com.huzhu.controller.module;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huzhu.common.ApiResponse;
import com.huzhu.common.PageResponse;
import com.huzhu.entity.Role;
import com.huzhu.entity.RolePermission;
import com.huzhu.service.RolePermissionService;
import com.huzhu.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;
    @Autowired
    private RolePermissionService rolePermissionService;

    @GetMapping("/page")
    public ApiResponse<PageResponse<Role>> page(@RequestParam(defaultValue = "1") Long pageNum,
                                                @RequestParam(defaultValue = "10") Long pageSize,
                                                @RequestParam(required = false) String keyword) {
        Page<Role> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Role> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like("role_name", keyword).or().like("description", keyword));
        }
        wrapper.orderByDesc("id");
        roleService.page(page, wrapper);
        return ApiResponse.success(PageResponse.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @PostMapping
    public ApiResponse<Boolean> create(@RequestBody Role role) {
        return ApiResponse.success("创建成功", roleService.save(role));
    }

    @PutMapping("/{id}")
    public ApiResponse<Boolean> update(@PathVariable Long id, @RequestBody Role role) {
        role.setId(id);
        return ApiResponse.success("更新成功", roleService.updateById(role));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> remove(@PathVariable Long id) {
        return ApiResponse.success("删除成功", roleService.removeById(id));
    }

    @GetMapping("/{id}/permissions")
    public ApiResponse<List<Long>> getRolePermissions(@PathVariable Long id) {
        QueryWrapper<RolePermission> wrapper = new QueryWrapper<>();
        wrapper.eq("role_id", id);
        List<RolePermission> records = rolePermissionService.list(wrapper);
        List<Long> permissionIds = new ArrayList<>();
        for (RolePermission record : records) {
            permissionIds.add(record.getPermissionId());
        }
        return ApiResponse.success(permissionIds);
    }

    @PutMapping("/{id}/permissions")
    public ApiResponse<Map<String, Object>> updateRolePermissions(@PathVariable Long id,
                                                                  @RequestBody Map<String, List<Long>> request) {
        List<Long> permissionIds = request.get("permissionIds");
        QueryWrapper<RolePermission> removeWrapper = new QueryWrapper<>();
        removeWrapper.eq("role_id", id);
        rolePermissionService.remove(removeWrapper);

        int assigned = 0;
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                RolePermission record = new RolePermission();
                record.setRoleId(id);
                record.setPermissionId(permissionId);
                if (rolePermissionService.save(record)) {
                    assigned++;
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("roleId", id);
        result.put("assignedCount", assigned);
        return ApiResponse.success("权限分配成功", result);
    }
}
