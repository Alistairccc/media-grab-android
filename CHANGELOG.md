# Changelog

All notable changes to this project are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and the project uses
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.3.0] - 2026-11-02

### Added

- Douyin parsing through the signed web detail API with a WebView-issued guest
  session (`a_bogus` + `x-secsdk-web-signature`).
- Bilibili parsing through `view` + `playurl` (`fnval=1`), including multi-page
  `?p=N` selection.
- Foreground `dataSync` download service with progress, completion, failure, and
  cancel notifications.
- MediaStore output in `Movies/MediaGrab` using `IS_PENDING`.
- Real cover images with Coil; gradient fallback when loading fails.
- Startup cleanup of stale pending MediaStore entries.
- R8/resource-shrunk release build with optional local signing.
- Timber logging, planted only in debug builds; sensitive values are not logged.

### Removed

- Recent downloads / local history. The home screen now focuses on parse,
  download, and save.

### Fixed

- Cancelled or failed downloads delete their pending MediaStore row.
- Release builds no longer require a checked-in signing key.

## [0.1.0] - 2026-11-02

### Added

- Initial Android scaffold, NOIR theme, single-page Compose UI.
- Manual paste, link normalization, and fake parse/download flow.
- Apache-2.0 license, CI, ktlint, lint, and unit tests.
