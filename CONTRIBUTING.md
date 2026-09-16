# Contributing

Thanks for helping improve Ambient Music for One UI.

This repository is a GPLv3 derivative of Ambient Music Mod. Contributions should preserve upstream attribution and keep the One UI integration narrow, testable and based on public Android APIs.

## Ground rules

- Keep applicable upstream copyright and license notices intact.
- Do not submit signing keys, passwords, recovery files, private device data or account information.
- Do not add Samsung private APIs, package impersonation, fake MediaSession behavior, root SystemUI modifications or privileged Samsung hooks to the One UI Live Update path.
- Do not claim a device as confirmed compatible without physical evidence.
- Keep changes focused; avoid unrelated refactors in feature or bug-fix pull requests.

## Development requirements

The current project baseline uses:

```text
JDK 17
compileSdk 36
targetSdk 36
AndroidX Core 1.17+
```

Before opening a pull request, run:

```bash
./gradlew :app:testDebugUnitTest --stacktrace
./gradlew :app:assembleDebugAndroidTest --stacktrace
./gradlew :app:compileReleaseKotlin --stacktrace
```

## Tests

Behavior changes should include regression tests. For the One UI presentation layer, prefer focused tests around:

- promotion capability gating;
- recognition-state lifecycle semantics;
- title/artist/timeout mapping;
- notification construction behavior where practical.

A successful recognition must not be replaced or cleared merely because the recognition pipeline enters a transient `Recording` or `Recognising` state.

## Compatibility reports

A new device should be added to `docs/COMPATIBILITY.md` only after physical validation. Include:

- exact device model;
- Android version;
- One UI version;
- tested project release or commit;
- recognition result;
- notification-system evidence;
- real Now Bar visual evidence.

Sanitize screenshots and logs before publishing them.

## Release signing

Contributors do not need the project's stable signing key. Local or CI test builds may use a disposable key.

Official public release artifacts are produced by the repository's paired-signing workflow and must pass certificate, package-ID and checksum verification.

## Upstream changes

When porting an upstream Ambient Music Mod change, keep the upstream origin clear in the commit or pull-request description and avoid presenting upstream work as derivative-original work.
