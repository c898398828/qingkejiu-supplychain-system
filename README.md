# 青稞酒系统开发文档

## 1. 项目概述
本项目为“互助县青稞酒产业链协同管理系统”，采用前后端分离架构：
- 前端：Vue 3 + Vite + TypeScript
- 后端：Spring Boot 2.7 + MyBatis-Plus + MySQL

主要功能覆盖：系统管理、供应链管理、生产管理、销售管理、协同办公、质量追溯，以及登录鉴权与权限控制。
<img width="1920" height="928" alt="image" src="https://github.com/user-attachments/assets/8d86e1b5-84c3-43f0-9120-5d6a9bbdd913" />
<img width="1920" height="928" alt="image" src="https://github.com/user-attachments/assets/ebac626e-de85-4146-b52a-4bc70ff9e87c" />
<img width="1920" height="928" alt="image" src="https://github.com/user-attachments/assets/d0935804-675b-44e6-94c0-e9e24c2ea2ea" />
<img width="1920" height="928" alt="image" src="https://github.com/user-attachments/assets/1d17a1c1-1012-4878-976c-28e41d76b3e0" />
<img width="1920" height="928" alt="image" src="https://github.com/user-attachments/assets/75739e45-001a-4622-b2e9-cdaa34199be6" />
<img width="1920" height="928" alt="image" src="https://github.com/user-attachments/assets/5ef9e8c3-e220-47a2-bf7d-840e9d099c51" />
<img width="1920" height="928" alt="image" src="https://github.com/user-attachments/assets/bae53052-a0b1-4676-8db5-1e18e43c0a05" />
<img width="1920" height="928" alt="image" src="https://github.com/user-attachments/assets/768b48a9-e627-4158-ad6f-95df2e91ad8a" />



## 2. 目录结构
```text
huzhu-barley-wine-system/
├─ frontend/                # Vue 前端
│  ├─ src/
│  │  ├─ views/huzhu/       # 互助系统页面（登录、布局、模块）
│  │  ├─ router/            # 路由守卫
│  │  ├─ services/api.ts    # API 与请求拦截器
│  │  └─ lib/               # 鉴权、UI 配置等工具
│  └─ package.json
├─ backend/                 # Spring Boot 后端
│  ├─ src/main/java/com/huzhu/
│  │  ├─ controller/        # 控制器
│  │  ├─ service/           # 业务层
│  │  └─ config/            # 安全配置、拦截器
│  ├─ src/main/resources/application.yml
│  └─ pom.xml
├─ skills/                  # 本地技能说明
└─ 开发文档.md
```

## 3. 运行环境要求
### 前端
- Node.js：`20.19+`（或 `22.12+`）
- npm：建议最新稳定版

### 后端
- JDK：`1.8`
- Maven：`3.8+`
- MySQL：`8.x`

## 4. 本地开发启动
### 4.1 启动后端
1. 创建数据库：`barley_wine_system`
2. 导入数据：
   - `backend/barley_wine_system.sql`（或 `backend/sql/` 下脚本）
3. 检查配置文件：`backend/src/main/resources/application.yml`
   - 默认数据库账号：`root`
   - 默认数据库密码：`123456`
   - 默认端口与上下文：`http://localhost:8080/api`
4. 启动：
```bash
cd backend
mvn spring-boot:run
```

### 4.2 启动前端
```bash
cd frontend
npm install
npm run dev
```
默认访问：`http://localhost:5173`

## 5. 构建与检查
### 前端
```bash
cd frontend
npm run type-check
npm run build-only
```

### 后端
```bash
cd backend
mvn clean package
```

## 6. 鉴权与权限机制
### 6.1 登录流程
- 前端登录页：`frontend/src/views/huzhu/HuzhuLoginView.vue`
- 登录接口：`POST /api/auth/login`
- 后端返回：`token + user`

### 6.2 Token 存储与请求注入
- 存储工具：`frontend/src/lib/huzhuAuth.ts`
- Token 键：`huzhu-token`
- 用户信息键：`huzhu-user`
- 请求拦截：`frontend/src/services/api.ts`
  - 自动注入 `Authorization: Bearer <token>`
  - 401 自动清理鉴权并跳转登录

### 6.3 路由与接口权限
- 路由守卫：`frontend/src/router/index.ts`
  - 未登录访问受限页面会跳转 `/login`
- 后端拦截器：`backend/src/main/java/com/huzhu/config/RoleAuthInterceptor.java`
  - 基于 `Authorization` 解析 token
  - 按角色限制模块访问

## 7. UI 配置项（本地持久化）
- 字体：`huzhu-frontend-font-key`
- 管理员头像：`huzhu-frontend-avatar-key`
- 侧栏折叠：`huzhu-sidebar-collapsed`
- 侧栏悬停预览：`huzhu-sidebar-hover-preview`

配置页：`frontend/src/views/huzhu/HuzhuSettingsView.vue`

## 8. 近期前端改造说明
1. 登录页重构：强化视觉层次、交互反馈、状态信息。
2. 侧栏交互升级：
   - 可折叠
   - 折叠态抽屉导航
   - 悬停预览（可在系统配置中开关）
   - 点击锁定抽屉
3. 顶栏样式增强：玻璃质感、光晕与扫光效果。

## 9. 常见问题
### 9.1 前端构建提示 Node 版本过低
现象：Vite 提示 Node 需 `20.19+`。
处理：升级 Node.js 到 `20.19+` 或 `22.12+`。

### 9.2 登录后立刻被踢回登录页
优先检查：
- 后端是否正常运行
- 请求头是否携带 `Authorization`
- 本地 `huzhu-token` 是否存在且未损坏

### 9.3 权限菜单与可访问页面不一致
检查：
- 前端 `src/lib/huzhu/modules.ts` 的角色-模块映射
- 后端 `RoleAuthInterceptor` 的角色模块映射

## 10. Git 协作建议
- `main`：稳定发布分支
- `feature/*`：功能开发分支
- 提交信息建议：
  - `feat: ...` 新功能
  - `fix: ...` 缺陷修复
  - `style: ...` 纯样式调整
  - `docs: ...` 文档更新

---
文档维护者：项目开发团队
