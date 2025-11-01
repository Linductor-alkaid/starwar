# 星际防卫战 - 前端项目

## 项目简介

星际防卫战游戏前端项目，基于 Vue 2.x + Element UI 开发。

## 技术栈

- **Vue** 2.6.14
- **Vue Router** 3.5.4
- **Vuex** 3.6.2
- **Element UI** 2.15.13
- **Axios** 0.27.2
- **Mock.js** 1.1.0（开发环境Mock数据）

## 快速开始

### 安装依赖

```bash
npm install
# 或
yarn install
```

### 启动开发服务器

```bash
npm run serve
# 或
yarn serve
```

访问 http://localhost:8081

### 构建生产版本

```bash
npm run build
# 或
yarn build
```

## 项目结构

```
frontend/
├── public/              # 静态资源
├── src/
│   ├── api/            # API接口封装
│   ├── assets/         # 资源文件
│   ├── components/     # 公共组件
│   ├── mock/          # Mock数据
│   ├── router/        # 路由配置
│   ├── store/         # Vuex状态管理
│   ├── styles/        # 样式文件
│   ├── utils/         # 工具函数
│   ├── views/         # 页面组件
│   ├── App.vue        # 根组件
│   └── main.js        # 入口文件
├── .env.development   # 开发环境配置
├── .env.production    # 生产环境配置
├── vue.config.js     # Vue CLI配置
└── package.json       # 项目依赖
```

## 开发说明

### Mock数据

开发环境下，如果后端接口未就绪，可以使用Mock数据。在 `.env.development` 中设置 `VUE_APP_USE_MOCK=true`。

### API接口

所有API接口定义在 `src/api/` 目录下，按照功能模块划分。

### 状态管理

使用Vuex管理全局状态，包括用户信息、游戏数据等。

## 注意事项

1. 确保后端服务运行在 http://localhost:8080
2. 开发环境默认启用Mock数据
3. 生产环境请确保 `VUE_APP_USE_MOCK=false`

