# Changelog

All notable derivative-specific changes are documented here.

## [v0.1.1] - 2026-10-01

### Fixed

- Pixel Ambient Music paired release is rebuilt with `android:debuggable="false"`.
- Explicit Android 16 `android:pageSizeCompat="enabled"` opt-in prevents the compatibility warning for inherited 4 KiB ELF libraries.
- Pixel Ambient Music APK packaging is aligned and verified with 16 KiB native-library ZIP boundaries.

### Changed

- Paired-release CI now patches and re-verifies the Pixel Ambient Music manifest before signing.
- CI reports legacy 4 KiB arm64 ELF libraries inherited from the Android System Intelligence base instead of misrepresenting them as natively 16 KiB compatible.
- Android release identity is `versionName v0.1.1`, `versionCode 242`.

### Notes

- This release uses Android 16's official page-size compatibility mode for legacy prebuilt native libraries. It does not claim native 16 KiB ELF alignment for those proprietary/prebuilt blobs.
- Ambient Music Mod and Pixel Ambient Music remain signed with the same stable paired release identity.

## [v0.1.0] - 2026-09-16

### Added

- Experimental Android Live Update integration for Samsung One UI Now Bar.
- Service-level publication of successful Ambient Music Mod recognitions.
- Capability-gated promoted ongoing notifications using public Android APIs.
- Recognition-to-surface mapper and lifecycle handling.
- Stable paired-signing workflow for Ambient Music Mod and Pixel Ambient Music.
- Public paired GitHub prerelease publishing with APK signature, package, version and checksum verification.
- Self-update routing to `Jorgeprdz/AmbientMusic-OneUI-LiveNotifications` while preserving PAM updates from `KieronQuinn/NowPlaying`.
- Deterministic AMM APK selection from releases containing both AMM and PAM APKs.
- **About → GitHub** routing to the derivative repository.
- Daily/manual fail-closed synchronization with `KieronQuinn/AmbientMusicMod@main`.
- Automatic `Upstream sync blocked` issue tracking when upstream cannot be merged safely.
- Physical validation on Samsung Galaxy S25 `SM-S931B` running Android 16 / One UI 8.x.
- Professional derivative documentation covering architecture, installation, compatibility, verification, security and upstream attribution.

### Notes

- First public release is experimental.
- Android release identity is `versionName v0.1.0`, `versionCode 241`.
- Confirmed compatibility is limited to physically validated devices listed in `docs/COMPATIBILITY.md`.
- The project remains GPLv3 and retains upstream attribution obligations.
- Builds created before `v0.1.0` require one manual installation of the derivative release before future in-app self-updates can use this repository.
