# Changelog

All notable changes to this project are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and the project uses
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.3.1] - 2026-11-03

### Added

- The About dialog links to the GitHub repository.

### Changed

- Redesigned adaptive app icon (Media Stack) with a themed monochrome layer.
- Lightened the page background to `#24292E`, with input, card, sheet, and
  dialog surfaces at `#2F363D` / `#373E47` for clearer separation.
- Increased the inline parse button contrast with an accent tint, white label,
  and a neutral disabled state.
- Link input remains editable; removed the custom paste button. Links are
  typed or pasted through the system long-press menu, then parsed with the
  `解析链接` button inside the input field or the keyboard Go key.
- Completion now shows only `已保存完成`; removed the non-functional
  gallery/view actions.

### Removed

- Empty-state illustration and its two hint lines; the idle area is now blank.

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
