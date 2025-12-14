# 星际防卫战 - 后端项目

## 项目介绍

星际防卫战后端API服务，基于Spring Boot开发。

## 技术栈

- Spring Boot 2.7.18
- MyBatis
- MySQL 8.0
- JWT认证
- Swagger文档

## 快速开始

### 1. 环境要求

- JDK 8
- Maven 3.6+
- MySQL 8.0

### 2. 配置数据库

修改 `src/main/resources/application.yml` 中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/star_war_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

### 3. 初始化数据库

在MySQL中执行 `docs/database/init.sql` 脚本。

### 4. 运行项目

使用IDEA打开项目，运行 `StarWarApplication.java` 主类。

或者使用Maven命令：

```bash
mvn spring-boot:run
```

### 5. 访问API文档

启动成功后访问：http://localhost:8080/swagger-ui.html

## API接口

### 用户认证
- POST /api/v1/auth/register - 用户注册
- POST /api/v1/auth/login - 用户登录

### 用户信息
- GET /api/v1/user/info - 获取用户信息

### 游戏记录
- POST /api/v1/game/record - 保存游戏记录
- GET /api/v1/game/history - 获取历史记录
- GET /api/v1/game/leaderboard - 获取排行榜

### 讨论区
- POST /api/v1/post/create - 发布帖子
- GET /api/v1/post/list - 获取帖子列表
- GET /api/v1/post/detail/{id} - 获取帖子详情
- POST /api/v1/post/reply - 回复帖子
- GET /api/v1/post/replies/{postId} - 获取回复列表

## 项目结构

```
backend/
├── src/main/java/com/starwar/
│   ├── StarWarApplication.java      # 启动类
│   ├── config/                       # 配置类
│   ├── controller/                   # 控制器
│   ├── service/                      # 服务层
│   ├── mapper/                       # 数据访问层
│   ├── entity/                       # 实体类
│   ├── dto/                          # 数据传输对象
│   ├── vo/                           # 视图对象
│   ├── common/                       # 公共类
│   └── util/                         # 工具类
└── src/main/resources/
    ├── application.yml               # 配置文件
    └── mapper/                       # MyBatis映射文件
```

## 开发规范

1. 使用Lombok简化代码
2. 统一异常处理
3. 统一返回结果格式
4. 接口遵循RESTful规范

## 作者

开发团队

