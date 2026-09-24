# 漫化 ACG 社区平台后端

这是漫化 ACG 社区平台的后端服务，基于 Spring Boot 构建，为前端提供用户认证、商品与订单、化妆服务预约、聊天、文件上传和后台管理等 REST 接口及 WebSocket 支持。

## 核心能力

- 用户注册、登录、资料维护和角色权限管理。
- 商品分类、商品发布、商品查询、购物车和订单管理。
- 化妆服务发布、可预约时段维护和预约流程管理。
- 商家入驻申请、化妆师认证申请和后台审核。
- 收藏、聊天房间与实时消息。
- 文件上传与静态文件访问。
- 分页查询、统一响应格式、统一异常处理和逻辑删除。

## 技术栈

| 用途 | 技术 |
| --- | --- |
| 基础框架 | Java 17、Spring Boot 3.2 |
| Web | Spring Web、Spring Validation、WebSocket |
| 数据访问 | MyBatis-Plus、MySQL Connector/J |
| 身份认证 | Sa-Token |
| 缓存 | Spring Data Redis |
| 工具库 | Hutool、Lombok、Fastjson2 |
| 构建工具 | Maven |

## 目录结构

```text
acg-backend/
├── src/main/java/com/acg/community/
│   ├── common/           # 统一响应、分页、实体基类和自动填充
│   ├── config/           # 跨域、Redis、MyBatis-Plus、认证与 MVC 配置
│   ├── controller/       # REST 控制器
│   ├── dto/              # 请求参数对象
│   ├── entity/           # 数据库实体
│   ├── enums/            # 业务枚举
│   ├── exception/        # 业务异常与全局异常处理
│   ├── mapper/           # MyBatis-Plus Mapper
│   ├── service/          # 业务接口及实现
│   ├── util/             # Redis 等工具类
│   └── vo/               # 响应视图对象
├── src/main/resources/
│   ├── application.yml   # 应用配置
│   └── db/schema.sql     # MySQL 初始化脚本
└── pom.xml
```

## 环境要求

- JDK 17
- Maven 3.9 或更高版本
- MySQL 8
- Redis 6 或更高版本

## 快速开始

### 1. 初始化数据库

在 MySQL 中执行 `src/main/resources/db/schema.sql`。脚本会创建 `acg_community` 数据库、业务表和必要索引。

### 2. 配置环境变量

在启动前设置以下变量；变量名与 `application.yml` 中的占位符对应：

| 环境变量 | 说明 |
| --- | --- |
| `DB_URL` | MySQL JDBC 连接地址 |
| `DB_USERNAME` | MySQL 用户名 |
| `DB_PASSWORD` | MySQL 密码 |
| `REDIS_HOST` | Redis 主机地址 |
| `REDIS_PORT` | Redis 端口 |
| `FILE_UPLOAD_DIR` | 上传文件的本地目录 |
| `FILE_BASE_URL` | 上传文件的外部访问基础地址 |

PowerShell 示例：

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/acg_community?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai'
$env:DB_USERNAME = 'root'
$env:DB_PASSWORD = 'your-password'
$env:REDIS_HOST = 'localhost'
$env:REDIS_PORT = '6379'
```

### 3. 启动服务

```bash
mvn spring-boot:run
```

服务默认监听 `8081` 端口，统一接口前缀为 `/api`，例如 `http://localhost:8081/api`。

### 4. 打包运行

```bash
mvn clean package -DskipTests
java -jar target/acg-community-1.0.0.jar
```

## 配置说明

- `server.port`：服务端口，默认 `8081`。
- `server.servlet.context-path`：接口上下文路径，默认 `/api`。
- `sa-token.token-name`：认证请求头名称，默认 `Authorization`。
- `file.upload-dir`：上传文件本地存储目录。
- `file.base-url`：上传文件的外部访问地址。

生产环境不要在 `application.yml` 中直接保存数据库密码、Token 或第三方服务密钥。应通过环境变量、外部配置中心或受权限保护的部署变量注入。

## 数据模型

初始化脚本主要包含以下业务数据：

- 用户、商品分类、商品、订单和订单明细。
- 化妆服务、化妆师可预约时段与预约记录。
- 化妆师认证和商家入驻申请。
- 收藏、聊天房间和消息。

数据表使用逻辑删除字段，实体公共字段由自动填充逻辑统一处理。

## 接口与认证

- 控制器位于 `controller` 包，按用户、商品、订单、预约、聊天、审核和后台模块组织。
- 后端以统一结果对象返回接口响应，分页列表使用分页结果对象封装。
- 受保护接口通过 Sa-Token 校验 `Authorization` 请求头。
- 前端开发服务默认将 `/api` 代理到此服务。

## 开发与测试

```bash
mvn test
mvn clean package
```

提交前建议至少执行一次 `mvn test` 或 `mvn clean package`。不要提交 `target/`、`uploads/`、本地覆盖配置或任何真实凭据。

## 相关项目

- 前端仓库：<https://github.com/Sad-femboy-meow/acg-community-frondend>
- 综合项目说明：<https://github.com/wzj1228516103/acg-community>
