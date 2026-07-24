# 漫化 ACG 社区平台

面向化妆师、假睫毛造型师及二次元文化爱好者的综合社区平台。项目提供商品交易、化妆服务预约、用户交流和后台运营管理能力，采用 Vue 3 与 Spring Boot 的前后端分离架构。

## 功能概览

### 用户端

- 用户注册、登录、个人资料维护和角色权限控制。
- 商品分类浏览、商品详情、购物车、订单创建和订单查询。
- 化妆服务浏览、服务详情、可预约时段选择和预约管理。
- 收藏商品或服务，以及站内实时聊天。
- 商家商品发布和化妆师服务发布。

### 运营端

- 用户、商品和商品分类管理。
- 订单、化妆服务和预约相关记录管理。
- 商家入驻与化妆师认证申请审核。
- 数据概览仪表盘。

## 技术栈

| 层级 | 技术 |
| --- | --- |
| 前端 | Vue 3、Vite、Vue Router、Pinia、Element Plus、Axios、ECharts |
| 后端 | Java 17、Spring Boot 3.2、MyBatis-Plus、Sa-Token、WebSocket |
| 数据存储 | MySQL 8、Redis |
| 构建工具 | Maven、npm |

## 项目结构

```text
acg-community/
├── acg-frontend/                         # Vue 3 前端工程
│   ├── src/api/                          # HTTP 接口封装
│   ├── src/assets/                       # 全局样式与静态资源
│   ├── src/layouts/                      # 页面布局
│   ├── src/router/                       # 路由与前端权限控制
│   ├── src/stores/                       # Pinia 状态管理
│   └── src/views/                        # 商城、预约、订单、聊天和后台页面
├── acg-backend/                          # Spring Boot 后端工程
│   ├── src/main/java/com/acg/community/
│   │   ├── controller/                   # REST 接口控制器
│   │   ├── service/                      # 业务服务及实现
│   │   ├── mapper/                       # MyBatis-Plus 数据访问层
│   │   ├── entity/                       # 数据库实体
│   │   ├── dto/                          # 请求数据对象
│   │   └── config/                       # 应用配置
│   └── src/main/resources/
│       ├── application.yml               # 应用配置
│       └── db/schema.sql                 # MySQL 初始化脚本
├── 需求文档/                              # 需求说明
└── 项目介绍.md                            # 项目简介
```

## 环境要求

- JDK 17
- Maven 3.9 或更高版本
- Node.js 18 或更高版本
- MySQL 8
- Redis

## 本地运行

### 1. 初始化数据库

在 MySQL 8 中执行 [schema.sql](acg-backend/src/main/resources/db/schema.sql)。该脚本会创建 `acg_community` 数据库及所需数据表。

根据本地环境修改 [application.yml](acg-backend/src/main/resources/application.yml) 中的数据库连接、Redis 地址和文件上传配置。不要将生产环境的密码、令牌或密钥提交到版本库。

### 2. 启动 Redis

确保 Redis 服务已在配置的主机与端口启动。默认配置使用 `localhost:6379`。

### 3. 启动后端

```powershell
cd acg-backend
mvn spring-boot:run
```

后端服务默认地址为 `http://localhost:8081/api`。

### 4. 启动前端

```powershell
cd acg-frontend
npm install
npm run dev
```

前端开发服务器默认地址为 `http://localhost:5173`。开发环境下，Vite 会将 `/api` 请求代理到 `http://localhost:8081`。

## 构建部署

### 构建前端

```powershell
cd acg-frontend
npm run build
```

构建产物输出到 `acg-frontend/dist`。

### 打包后端

```powershell
cd acg-backend
mvn clean package -DskipTests
```

生成的可执行 JAR 位于 `acg-backend/target` 目录，可使用以下命令启动：

```powershell
java -jar target/acg-community-1.0.0.jar
```

部署前请将数据库、Redis、文件存储及跨域策略调整为目标环境的配置。

## 角色说明

| 角色 | 主要能力 |
| --- | --- |
| 普通用户 | 浏览商品和服务、购物下单、预约、收藏、聊天 |
| 化妆师 | 发布化妆服务、维护可预约时段、处理预约 |
| 商家 | 发布和管理商品 |
| 管理员 | 管理用户、分类、商品、订单、服务和入驻申请 |

## 开发说明

- 后端统一使用 `/api` 作为接口上下文路径。
- 登录令牌通过 `Authorization` 请求头传递，认证由 Sa-Token 提供。
- 前端路由对需要登录和管理员权限的页面进行了拦截。
- 商品图片及其他文件通过后端上传接口管理，部署时应配置可持久化的文件存储路径。

## 相关文档

- [项目介绍](项目介绍.md)
- [需求文档](需求文档/需求文档.md)
