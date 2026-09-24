# 漫化 ACG 社区平台前端

这是漫化 ACG 社区平台的前端工程，基于 Vue 3 构建。它为二次元文化爱好者、化妆师、假睫毛造型师、商家及管理员提供商品交易、化妆服务预约、站内交流和运营管理界面。

## 功能

### 用户功能

- 注册、登录、个人资料和账号设置。
- 商品浏览、分类筛选、商品详情、购物车和订单结算。
- 化妆服务浏览、服务详情、预约时段选择和预约管理。
- 商品和服务收藏。
- 站内聊天。

### 创作者与商家功能

- 商家发布商品并维护商品信息。
- 化妆师发布服务并维护预约时段。

### 管理后台

- 仪表盘数据展示。
- 用户、商品、分类、订单与化妆服务管理。
- 商家入驻和化妆师认证申请审核。

## 技术栈

| 用途 | 技术 |
| --- | --- |
| UI 框架 | Vue 3 |
| 构建工具 | Vite 5 |
| 路由 | Vue Router 4 |
| 状态管理 | Pinia |
| 组件库 | Element Plus |
| HTTP 客户端 | Axios |
| 图表 | ECharts |
| 样式 | Sass |

## 目录结构

```text
acg-frontend/
├── src/
│   ├── api/              # 用户、商品、订单、预约、聊天与后台接口
│   ├── assets/           # 全局样式与静态资源
│   ├── layouts/          # 前台主布局
│   ├── router/           # 路由定义和权限拦截
│   ├── stores/           # 用户与购物车状态
│   ├── utils/            # Axios 请求封装
│   └── views/            # 页面视图
│       ├── admin/        # 管理后台
│       ├── artist/       # 化妆师服务发布
│       ├── cart/         # 购物车与结算
│       ├── chat/         # 聊天
│       ├── makeup/       # 化妆服务
│       ├── merchant/     # 商品发布
│       ├── order/        # 订单
│       ├── shop/         # 商城
│       └── user/         # 登录、注册和个人中心
├── index.html
├── vite.config.js
└── package.json
```

## 环境要求

- Node.js 18 或更高版本
- npm 9 或更高版本

## 快速开始

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

默认访问地址为 `http://localhost:5173`。开发服务器会将 `/api` 请求代理到 `http://localhost:8081`，因此请先启动后端服务。

### 生产构建

```bash
npm run build
```

构建结果输出至 `dist/`。

### 本地预览构建结果

```bash
npm run preview
```

## 路由与权限

前端使用路由守卫控制访问：

- 购物车、结算、订单、聊天、收藏、个人中心和发布页面需要登录。
- `/admin` 下的页面需要管理员角色。
- 登录令牌存储在浏览器本地存储中，并通过 `Authorization` 请求头发送给后端。

## 后端对接

默认代理配置位于 `vite.config.js`：

```text
/api -> http://localhost:8081
```

生产环境应通过反向代理或环境化配置将 `/api` 指向实际后端地址，并确保 WebSocket 与文件上传路径可访问。

## 开发规范

- 接口调用集中在 `src/api`，避免在页面组件中重复封装请求。
- 页面级状态优先放在组件内；跨页面共享状态使用 Pinia。
- 修改页面后运行 `npm run build` 验证生产构建。
- 不提交 `node_modules`、`dist` 或包含真实凭据的环境配置文件。

## 相关项目

- 后端仓库：<https://github.com/Sad-femboy-meow/acg-community-backend>
- 综合项目说明：<https://github.com/wzj1228516103/acg-community>

