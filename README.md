# MediaGrab

[![Android CI](https://github.com/Alistairccc/media-grab-android/actions/workflows/android.yml/badge.svg)](https://github.com/Alistairccc/media-grab-android/actions/workflows/android.yml)
[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Android-10%2B-3DDC84?logo=android&logoColor=white)](#)

**复制分享链接 → 粘贴 → 保存到相册。**

MediaGrab 是一个专注、轻量的 Android 视频保存工具，支持抖音和哔哩哔哩的**单条视频**。不登录、不读取账号数据、不接管账号，只做一件事：把你有权保存的视频放进系统相册。

> 📷 界面截图与录屏正在制作中，后续补充到这里。

## 三步完成

1. **复制**：在抖音或 B 站复制视频分享链接
2. **粘贴或输入**：打开 MediaGrab，点「粘贴」；也可以直接在输入框里输入链接
3. **保存**：确认视频信息后点「保存到相册」

下载在后台进行，切到其他 App 也不会中断；完成后可以在系统相册的 `Movies/MediaGrab` 中找到文件。

## 功能

- **抖音**：解析无水印单文件视频，清晰度以接口返回为准（实测 1080P）
- **哔哩哔哩**：单条视频，带音轨 MP4；未登录画质以平台实际提供为准（实测单条 720P）
- **两种输入**：一键粘贴分享链接，或手动输入后点「解析链接」
- **保存到相册**：直接写入 `Movies/MediaGrab`，不需要存储权限
- **后台下载**：前台服务 + 通知进度，支持取消
- **真实封面**：解析后显示视频封面，加载失败时回退 NOIR 渐变占位
- **干净**：无广告、无登录、无账号体系、无数据上传

## 界面预览

<!--
配图 / 录屏完成后，把图片放到 docs/images/ 并替换这里，例如：

| 空态 | 解析结果 | 下载中 |
|---|---|---|
| ![](docs/images/empty.png) | ![](docs/images/ready.png) | ![](docs/images/downloading.png) |

也可以放一段 GIF 或视频链接。
-->

截图与录屏制作中。

## 支持范围

| 支持 | 暂不支持 |
|---|---|
| 抖音单条视频（分享短链 / 视频页链接） | 抖音图集、合集、直播 |
| B 站单条视频（`BV` 链接 / `b23.tv` 短链） | B 站番剧、直播、批量队列 |
| Android 10（API 29）及以上 | 1080P DASH 音视频合并 |
| 后台下载、通知取消 | 登录、账号同步、云端记录 |

## 常见问题

**需要登录吗？**
不需要。抖音解析所需的游客 Cookie 由 App 内隐藏 WebView 获取，只用于签名请求，不读取账号信息。

**视频保存在哪里？**
系统相册的 `Movies/MediaGrab`，也可以在文件管理器的同一路径下找到。

**为什么抖音第一次解析要等几秒？**
首次需要启动隐藏 WebView 获取游客 Cookie，之后会复用。网络较慢时等待时间会变长。

**为什么 B 站不是 1080P？**
未登录时画质由平台决定，App 不承诺固定清晰度，以接口实际返回为准。本项目不做 DASH 音视频合并。

**会上传我的链接或数据吗？**
不会。App 不采集设备标识，不上传解析内容，也没有自建服务端。

**可以批量下载吗？**
不支持。当前只处理单条视频，避免变成批量抓取工具。

## 下载

从 [Releases](https://github.com/Alistairccc/media-grab-android/releases/latest) 下载最新 APK（当前 `v0.3.0`，Android 10+）。

### 从源码构建

```bash
git clone https://github.com/Alistairccc/media-grab-android.git
cd media-grab-android
./gradlew assembleDebug
```

APK 输出在 `app/build/outputs/apk/debug/app-debug.apk`。

构建环境：

- JDK 17
- Android SDK 36 / build-tools 36.1.0
- 最低运行版本 Android 10（API 29）

### Release 构建

```bash
./gradlew assembleRelease
```

release 会开启 R8 与资源压缩。正式分发前请创建仓库根目录的 `keystore.properties`：

```properties
storeFile=/path/to/release.jks
storePassword=...
keyAlias=...
keyPassword=...
```

没有该文件时会回退 debug 签名，仅适合本地验证，不要用于正式分发。

## 技术栈

Kotlin · Jetpack Compose · Material 3 · OkHttp · Coroutines · Coil · Timber

- 抖音：原生 `a_bogus` / `x-secsdk-web-signature` 签名，WebView 仅负责签发游客 Cookie
- B 站：`x/web-interface/view` + `x/player/playurl`
- 下载：MediaStore `IS_PENDING` + 前台服务
- 最低 Android 10，targetSdk 36

## 参与贡献

- 开发与提交流程见 [CONTRIBUTING.md](CONTRIBUTING.md)
- 行为准则见 [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md)
- 安全问题请按 [SECURITY.md](SECURITY.md) 私下报告
- 版本变更见 [CHANGELOG.md](CHANGELOG.md)

## 合规

本项目仅用于个人学习和保存**已获授权**的内容。使用者应遵守平台服务条款与著作权法，不得下载、传播侵权内容。项目不提供破解、去 DRM 或绕过付费的能力。

## 许可

[Apache-2.0](LICENSE)
