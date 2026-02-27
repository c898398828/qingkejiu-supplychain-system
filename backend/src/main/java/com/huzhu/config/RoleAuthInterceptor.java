package com.huzhu.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huzhu.common.ApiResponse;
import com.huzhu.entity.Role;
import com.huzhu.entity.User;
import com.huzhu.service.RoleService;
import com.huzhu.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Component
public class RoleAuthInterceptor implements HandlerInterceptor {

    private static final Set<String> PUBLIC_PREFIX = new HashSet<>(Arrays.asList(
            "/auth/",
            "/error",
            "/actuator/"
    ));

    private final UserService userService;
    private final RoleService roleService;
    private final ObjectMapper objectMapper;
    private final Map<String, Set<String>> roleModules;

    public RoleAuthInterceptor(UserService userService, RoleService roleService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.roleService = roleService;
        this.objectMapper = objectMapper;
        this.roleModules = buildRoleModuleMap();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || isPublic(uri)) {
            return true;
        }

        String module = resolveModule(uri);
        if (module == null) {
            return true;
        }

        User currentUser = resolveCurrentUser(request);
        if (currentUser == null || currentUser.getStatus() == null || currentUser.getStatus() != 1) {
            writeError(response, 401, "未登录或账号不可用");
            return false;
        }

        if (isAdmin(currentUser)) {
            return true;
        }

        String roleName = resolveRoleName(currentUser.getRoleId());
        Set<String> allowed = roleModules.getOrDefault(roleName, Collections.emptySet());
        if (!allowed.contains(module)) {
            // 兼容旧 role_id 编号语义（演示历史数据）
            allowed = fallbackByRoleId(currentUser.getRoleId());
        }

        if (!allowed.contains(module)) {
            writeError(response, 403, "当前角色无权限访问该模块");
            return false;
        }

        return true;
    }

    private boolean isPublic(String uri) {
        for (String prefix : PUBLIC_PREFIX) {
            if (uri.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private User resolveCurrentUser(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (!StringUtils.hasText(auth)) {
            return null;
        }
        String token = auth.trim();
        if (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }
        if (!token.startsWith("huzhu-token-")) {
            return null;
        }
        String idPart = token.substring("huzhu-token-".length());
        Long userId;
        try {
            userId = Long.parseLong(idPart);
        } catch (NumberFormatException e) {
            return null;
        }
        return userService.getById(userId);
    }

    private boolean isAdmin(User user) {
        if (user.getRoleId() != null && user.getRoleId() == 1) {
            return true;
        }
        String roleName = resolveRoleName(user.getRoleId());
        return "admin".equalsIgnoreCase(roleName);
    }

    private String resolveRoleName(Integer roleId) {
        if (roleId == null) {
            return "";
        }
        Role role = roleService.getById(roleId.longValue());
        return role == null || !StringUtils.hasText(role.getRoleName()) ? "" : role.getRoleName().trim();
    }

    private String resolveModule(String uri) {
        LinkedHashMap<String, String> pathModuleMap = new LinkedHashMap<>();
        pathModuleMap.put("/users", "users");
        pathModuleMap.put("/roles", "roles");
        pathModuleMap.put("/permissions", "permissions");
        pathModuleMap.put("/planting-bases", "planting-bases");
        pathModuleMap.put("/purchases", "purchases");
        pathModuleMap.put("/inventories", "inventories");
        pathModuleMap.put("/production-processes", "production-processes");
        pathModuleMap.put("/quality-controls", "quality-controls");
        pathModuleMap.put("/orders", "orders");
        pathModuleMap.put("/deliveries", "deliveries");
        pathModuleMap.put("/customers", "customers");
        pathModuleMap.put("/tasks", "tasks");
        pathModuleMap.put("/documents", "documents");
        pathModuleMap.put("/notices", "notices");
        pathModuleMap.put("/trace-records", "trace-records");
        pathModuleMap.put("/dashboard", "dashboard");
        pathModuleMap.put("/analysis", "analysis");

        for (Map.Entry<String, String> entry : pathModuleMap.entrySet()) {
            if (uri.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private Map<String, Set<String>> buildRoleModuleMap() {
        Map<String, Set<String>> map = new LinkedHashMap<>();
        Set<String> common = new HashSet<>(Arrays.asList("dashboard", "analysis", "tasks", "documents", "notices"));

        map.put("planting_manager", union(common, "planting-bases", "purchases", "inventories"));
        map.put("purchase_manager", union(common, "purchases", "inventories"));
        map.put("production_manager", union(common, "production-processes", "inventories"));
        map.put("quality_manager", union(common, "quality-controls", "trace-records"));
        map.put("sales_manager", union(common, "orders", "deliveries", "customers"));
        map.put("logistics_manager", union(common, "deliveries", "orders"));
        return map;
    }

    private Set<String> fallbackByRoleId(Integer roleId) {
        if (roleId == null) {
            return Collections.emptySet();
        }
        Set<String> common = new HashSet<>(Arrays.asList("dashboard", "analysis", "tasks", "documents", "notices"));
        switch (roleId) {
            case 2:
                return union(common, "planting-bases", "purchases", "inventories");
            case 3:
                return union(common, "purchases", "inventories", "production-processes");
            case 4:
                return union(common, "production-processes", "orders", "deliveries", "customers");
            case 5:
                return union(common, "quality-controls", "trace-records");
            case 6:
                return union(common, "orders", "deliveries", "customers");
            case 7:
                return union(common, "deliveries", "orders");
            default:
                return Collections.emptySet();
        }
    }

    private Set<String> union(Set<String> base, String... modules) {
        Set<String> result = new HashSet<>(base);
        result.addAll(Arrays.asList(modules));
        return result;
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        ApiResponse<Object> body = new ApiResponse<>();
        body.setCode(status);
        body.setMessage(message);
        body.setData(null);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}

