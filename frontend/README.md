# LogiFlow 前端

LogiFlow 管理后台，基于 Vue 3 + TypeScript + Vite + Naive UI。

## 技术栈

- Vue 3（`<script setup>` + TypeScript）
- Vite 8 · Vue Router · Pinia
- Naive UI（通过 `unplugin-vue-components` 按需自动导入）
- ESLint + oxlint + Prettier

## 开发

```bash
npm install
npm run dev     # http://localhost:5173
```

开发服务器已配置代理：`/api` 请求转发到 `http://localhost:8080`，本地联调无需处理跨域。

## 构建与检查

```bash
npm run build   # 类型检查 + 生产构建，产物在 dist/
npm run lint    # ESLint + oxlint
npm run format  # Prettier 格式化
```

## 目录结构

```text
src/
├── assets/     # 全局样式与静态资源
├── router/     # 路由配置
├── stores/     # Pinia 状态（当前为空）
└── views/      # 页面组件
```

## 与后端联调

先启动后端（见仓库根目录 README），前端直接请求 `/api/**`。除登录接口外，请求需在 `Authorization` 头中携带 `Bearer <token>`。
