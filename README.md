<p align="center">
  <img src="docs/images/icon.png" width="96" alt="MediaGrab icon">
</p>

<h1 align="center">MediaGrab</h1>

<p align="center">
  <a href="https://github.com/Alistairccc/media-grab-android/actions/workflows/android.yml"><img src="https://github.com/Alistairccc/media-grab-android/actions/workflows/android.yml/badge.svg" alt="Android CI"></a>
  <a href="https://github.com/Alistairccc/media-grab-android/releases/latest"><img src="https://img.shields.io/github/v/release/Alistairccc/media-grab-android" alt="Release"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-Apache--2.0-blue.svg" alt="License"></a>
  <img src="https://img.shields.io/badge/Android-10%2B-3DDC84?logo=android&logoColor=white" alt="Android 10+">
</p>

<p align="center"><strong>Copy a share link → paste → parse → save to your gallery.</strong></p>

<p align="center"><a href="README.zh-CN.md">简体中文</a> · English</p>

MediaGrab is a focused, lightweight Android video saver for **single videos** from Douyin, Bilibili, and TikTok. It does not sign in, read account data, or control your account. It does one thing: put videos you have the right to keep into your system gallery.

## Three steps

1. **Copy** a video share link from Douyin, Bilibili, or TikTok.
2. **Paste** it into MediaGrab (or type it), then tap **Parse link**.
3. **Save** — confirm the video details and tap **Save to gallery**.

Downloads continue in the background if you switch apps. When finished, the file is in `Movies/MediaGrab` in your system gallery.

## Features

- **Douyin**: parses watermark-free single-file videos; resolution depends on the API response (1080P measured).
- **Bilibili**: single videos with audio in one MP4; without login, quality depends on what the platform returns (720P measured).
- **TikTok**: parses watermark-free single-file videos. A guest session cookie is created for parsing and playback, with no account sign-in.
- **Direct input**: type or long-press paste a share link, then tap **Parse link** or the keyboard Go key.
- **Save to gallery**: writes directly to `Movies/MediaGrab`, no storage permission required.
- **Background downloads**: foreground service with progress notifications and cancellation.
- **Real covers**: the video thumbnail is loaded after parsing; falls back to a theme gradient when loading fails.
- **Theme switching**: four color themes (Aloha Cyan, Midnight Blue, Violet, Warm Ember) in the overflow menu; the choice is saved locally.
- **About page**: version, open-source license, and the GitHub repository.
- **Clean**: no ads, no sign-in, no accounts, no data uploads.

## Support

| Supported | Not supported |
|---|---|
| Douyin single videos (share links / video pages) | Douyin image posts, collections, live streams |
| Bilibili single videos (`BV` links / `b23.tv` short links) | Bilibili bangumi, live streams, batch queues |
| TikTok single videos (`/video/` links / `vm.tiktok.com` short links) | TikTok image posts, live streams |
| Android 10 (API 29) and above | 1080P DASH audio/video merging |
| Background downloads, notification cancellation | Sign-in, account sync, cloud history |

## FAQ

**Do I need to sign in?**
No. Douyin parsing uses a guest cookie issued by a hidden WebView, only for signed requests; it does not read account information.

**Where are videos saved?**
In your system gallery under `Movies/MediaGrab`; the same path is visible in file managers.

**Why does the first Douyin parse take a few seconds?**
The app must start a hidden WebView once to obtain a guest cookie, then reuses it. Slow networks take longer.

**Does TikTok require sign-in?**
No. The app establishes a guest session cookie for TikTok, used only to read public videos and pass CDN checks. There is no account sign-in and no account data is read.

**Why does TikTok parsing fail?**
TikTok does not expose public data in every region. Some regions or networks return rate limits or empty responses. Confirm that the TikTok website opens normally on the device, then retry.

**Why isn't Bilibili 1080P?**
Without login, quality is decided by the platform. The app does not promise a fixed resolution and does not merge DASH streams.

**Will my links or data be uploaded?**
No. The app collects no device identifiers, uploads no parsed content, and has no backend server.

**Can I download in batches?**
No. MediaGrab handles one video at a time so it does not become a bulk scraping tool.

## Download

Get the latest APK from [Releases](https://github.com/Alistairccc/media-grab-android/releases/latest) (current `v0.4.0`, Android 10+).

Direct download: [MediaGrab-0.4.0.apk](https://github.com/Alistairccc/media-grab-android/releases/download/v0.4.0/MediaGrab-0.4.0.apk)

See [CHANGELOG.md](CHANGELOG.md) for version history.

### Build from source

```bash
git clone https://github.com/Alistairccc/media-grab-android.git
cd media-grab-android
./gradlew assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

Build environment:

- JDK 17
- Android SDK 36 / build-tools 36.1.0
- Minimum runtime: Android 10 (API 29)

### Release build

```bash
./gradlew assembleRelease
```

Release builds enable R8 and resource shrinking. Before distributing, create `keystore.properties` in the repository root:

```properties
storeFile=/path/to/release.jks
storePassword=...
keyAlias=...
keyPassword=...
```

Without this file, the build falls back to debug signing — suitable only for local verification, never for distribution.

## Tech stack

Kotlin · Jetpack Compose · Material 3 · OkHttp · Coroutines · Coil · Timber

- Douyin: native `a_bogus` / `x-secsdk-web-signature` signing; the WebView only issues guest cookies.
- Bilibili: `x/web-interface/view` + `x/player/playurl`.
- Downloads: MediaStore `IS_PENDING` + foreground service.
- Minimum Android 10, targetSdk 36.

## Compliance

This project is for personal study and saving content you are **authorized** to keep. Users must comply with platform terms of service and copyright law. Do not download or redistribute infringing content. The project provides no cracking, DRM removal, or paid-content bypass capability.

## License

[Apache-2.0](LICENSE)
