# Contributing to MediaGrab

Thanks for taking the time to contribute.

## Scope

MediaGrab is a personal backup tool for single Douyin / Bilibili videos.
Changes that add login, DRM bypass, watermarks, batch queues, or content
distribution are out of scope.

## Before you start

- Search existing issues and pull requests.
- For non-trivial changes, open an issue first so the approach can be discussed.
- Never include cookies, tokens, personal links, or `local.properties` in a
  report or commit.

## Development setup

Requirements:

- JDK 17
- Android SDK 36 / build-tools 36.1.0
- Android Studio or the Gradle CLI

Build and verify:

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew ktlintCheck
```

Device tests (requires a connected device/emulator):

```bash
./gradlew connectedDebugAndroidTest
```

Release builds use `keystore.properties` when present and fall back to the
debug keystore otherwise. Do not commit signing material.

## Code style

- Kotlin / Compose style is enforced by ktlint (`.editorconfig`).
- Keep `parser` free of Android APIs so it stays unit-testable.
- Keep UI stateless where possible; side effects belong in the ViewModel.
- New user-facing copy belongs in `res/values/strings.xml`.
- Do not log URLs, query strings, cookies, tokens, or file paths. Timber is
  planted only for debug builds.

## Tests

- Bug fixes and behavior changes need a test that fails before the fix and
  passes after it.
- Parsers should use fixtures with `MockWebServer`.
- Run the full verification above before opening a pull request.

## Commits and pull requests

- Use [Conventional Commits](https://www.conventionalcommits.org/):
  `feat:`, `fix:`, `refactor:`, `test:`, `docs:`, `chore:`.
- Keep pull requests focused. Describe the problem, the change, and how it was
  verified.
- Fill in the pull request template and confirm the checklist.
- By contributing you agree that your changes are licensed under the
  [Apache License 2.0](LICENSE).
