# MediaGrab

[![Android CI](https://github.com/Alistairccc/media-grab-android/actions/workflows/android.yml/badge.svg)](https://github.com/Alistairccc/media-grab-android/actions/workflows/android.yml)

A minimal Android downloader for single Douyin / Bilibili videos.
复制分享链接 → 手动粘贴 → 解析 → 保存到系统相册。

> 当前状态：M3 已完成（v0.3.0）—— 解析、下载、前台服务/通知、真实封面、
> pending 清理与 minified release 构建均可用。

## 功能

- 抖音：WebView 签发游客 Cookie，原生 `a_bogus` 签名请求，下载无水印单文件
- 哔哩哔哩：单条视频，未登录通常 720P，MP4 含音轨
- 保存到 `Movies/MediaGrab`，无需存储权限
- 前台服务下载 + 通知进度 + 取消，切后台继续
- 真实封面；加载失败回退 NOIR 渐变占位

## 技术栈

Kotlin · Jetpack Compose · Material 3 · OkHttp · Coroutines · Timber
minSdk 29 · targetSdk 36

## 构建

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew ktlintCheck
./gradlew assembleRelease
```

`assembleRelease` 读取仓库根目录的 `keystore.properties`
（`storeFile` / `storePassword` / `keyAlias` / `keyPassword`）；
文件不存在时回退 debug 签名，仅用于本地验证，正式发布请配置自己的 keystore。

## 范围

不做登录、1080P DASH 音视频合并、图集、合集、直播、批量队列。

## 参与贡献

- 开发与提交流程见 [CONTRIBUTING.md](CONTRIBUTING.md)
- 行为准则见 [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md)
- 安全问题请按 [SECURITY.md](SECURITY.md) 私下报告
- 版本变更见 [CHANGELOG.md](CHANGELOG.md)

## 合规

本项目仅用于个人学习和备份已获授权的内容。使用者应遵守平台服务条款与著作权法，不得下载、传播侵权内容。项目不提供破解、去 DRM 或绕过付费的能力。

## 许可

[Apache-2.0](LICENSE)
