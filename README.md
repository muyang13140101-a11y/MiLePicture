# MiLePicture - 多图库安全聚合搜索应用

> **版本**：1.0.0 (合规与安全架构基线)  
> **技术栈**：
> - 📱 **Android 客户端**：Kotlin + Jetpack Compose + Material 3 + Coil + Retrofit
> - ⚡ **聚合后端**：Node.js + TypeScript + Express + Axios + NodeCache

---

## 📁 目录结构

```text
MiLePicture\
├── app\                    # 📱 原生 Android Studio 项目核心模块 (Kotlin + Compose)
│   ├── src\main\
│   │   ├── java\com\milepicture\app\
│   │   │   ├── data\       # 数据模型与 Retrofit API 接口
│   │   │   ├── ui\         # Jetpack Compose 瀑布流、详情页、搜索栏与主题
│   │   │   └── MainActivity.kt
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts        # 根工程 Gradle 配置
├── settings.gradle.kts     # 根工程 Gradle 模块声明
├── server\                 # 🚀 安全聚合后端（管理 API 密钥、并发搜索、24h缓存、数据清洗）
│   ├── src\
│   │   ├── adapters\       # 各大图库适配器 (Openverse, The Met, Wikimedia, Unsplash, Pixabay, Wallhaven)
│   │   ├── config\         # 图库注册表与合规策略
│   │   ├── services\       # 聚合搜索调度与公平交织算法
│   │   ├── types\          # 统一图片模型 UnifiedImage
│   │   └── index.ts        # RESTful API 服务入口
│   ├── .env                # 环境变量（可填入可选 API Key）
│   └── package.json
└── README.md               # 📖 完整开发与调试使用说明（本文档）
```

---

## 🚀 第一步：启动本地聚合后端

后端负责代理所有图库请求，保护 API Key 不被 APK 反编译破解，并提供统一的图片数据格式。

1. 打开终端（PowerShell 或 CMD）：
   ```powershell
   cd server
   npm run dev
   ```
2. 看到如下输出即表示后端启动成功：
   ```text
   ======================================================
   🚀 MiLePicture 聚合后端服务已启动: http://localhost:3000
   📡 Android 模拟器通信地址: http://10.0.2.2:3000
   ======================================================
   ```
3. 可以在浏览器中打开 `http://localhost:3000/v1/search?q=art` 进行快速验证。

---

## 📱 第二步：在 Android Studio 中打开与运行

1. **直接打开项目**：
   - 在 Android Studio 中直接打开本仓库根目录：`MiLePicture`。
2. **等待 Gradle 同步**：
   - Android Studio 会自动识别并同步项目（若未自动触发，点击右上角大象图标 **Sync Project with Gradle Files** 即可）。
3. **一键运行应用**：
   - 在顶部运行模块选择 **`app`**，选择你的 **Android 模拟器** 或 **真机**。
   - 点击绿色的运行按钮 **▶ Run 'app'** 即可！

---

## 🌐 网络与设备联调说明

| 运行环境 | 默认后端连接地址 | 配置位置 |
|---|---|---|
| **官方 Android 模拟器** | `http://10.0.2.2:3000/` | 已默认预设在 `ApiClient.kt`，**开箱即连，无需修改** |
| **USB 真机调试 / 局域网** | `http://<电脑局域网IP>:3000/` | 在 `ApiClient.kt` 中将 `BASE_URL` 修改为你的电脑 IP（如 `http://192.168.1.5:3000/`） |

---

## 🔑 图库 API 密钥配置说明（可选）

为保障安全性，首发版本中：
- **开箱即用（无需任何 Key）**：
  - **Openverse**：海量 Creative Commons 开放许可作品
  - **The Met (大都会艺术博物馆)**：全部公有领域 (CC0) 经典艺术馆藏
  - **Wikimedia Commons (维基共享)**：自由版权历史人文多媒体
- **可选填入个人 API Key**：
  打开 `D:\MiLePicture\server\.env`，填入对应 Key 即可立即解锁更多图库：
  ```env
  UNSPLASH_ACCESS_KEY=你的UnsplashKey
  PIXABAY_API_KEY=你的PixabayKey
  WALLHAVEN_API_KEY=你的WallhavenKey
  ```
- **合规暂缓**：
  - **Pexels** 与 **SVG Repo** 预留了适配器接口，遵循合规策略默认处于待定状态。

---

## 🎨 核心功能特性

1. **多图库公平交织瀑布流**：打破单一数据源壁垒，混合展示各平台精选素材。
2. **沉浸式标签筛选与搜索**：支持自然风景、大都会馆藏、赛博朋克、插画矢量、宇宙星空等热门分类一键直达。
3. **公有领域 (Public Domain / CC0) 专区过滤**：可一键过滤出完全免版权风险的公有领域馆藏素材。
4. **严格版权归因卡片**：图片详情页清晰展示作者姓名、协议类型（CC0、CC-BY-SA、Unsplash License 等）及源站原始链接。
5. **本地灵感收藏夹**：轻触红心即可快速收藏灵感作品。
