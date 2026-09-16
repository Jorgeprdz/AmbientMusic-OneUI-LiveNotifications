# Ambient Music for One UI

> Now Playing recognition in Samsung's Now Bar using Android Live Updates.

[![CI](https://github.com/Jorgeprdz/AmbientMusic-OneUI-LiveNotifications/actions/workflows/ci.yml/badge.svg)](https://github.com/Jorgeprdz/AmbientMusic-OneUI-LiveNotifications/actions/workflows/ci.yml)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)

**Experimental · Unofficial community project**

Ambient Music for One UI is a GPLv3 derivative of Kieron Quinn's [Ambient Music Mod](https://github.com/KieronQuinn/AmbientMusicMod). It publishes successful Now Playing recognitions as Android Live Updates so supported Samsung One UI devices can present them in the Now Bar.

This project is not affiliated with or endorsed by Samsung, Google, or Kieron Quinn.

## Proof on real hardware

The integration has been physically validated on a Samsung Galaxy S25 (`SM-S931B`) running Android 16 / One UI 8.x. During validation, Ambient Music Mod recognised **Dream On — Aerosmith** and Samsung SystemUI rendered the title and artist in the real lock-screen Now Bar, including the platform's progress treatment.

A sanitized real-device screenshot will be kept in `docs/assets/` as release evidence; compatibility claims remain limited to devices explicitly listed below.

## What it does

Ambient Music Mod already brings Pixel-style ambient music recognition to non-Pixel Android devices. This derivative keeps that recognition pipeline and adds a focused presentation path for Samsung One UI:

```text
Pixel Ambient Music / Now Playing
        ↓
Ambient Music Mod recognition
        ↓
RecognitionState.Recognised
        ↓
Android Live Update notification
        ↓
Samsung SystemUI / Now Bar
```

The One UI Live Update path is **always enabled** in this derivative. Promotion is still capability-gated at runtime: if the platform does not permit promoted ongoing notifications, the publisher falls back safely instead of using private Samsung APIs or unsupported workarounds.

## Tested compatibility

| Device | Model | Android | One UI | Status |
|---|---|---:|---:|---|
| Samsung Galaxy S25 | SM-S931B | 16 | 8.x | Physically validated |

The initial validation recognised **Dream On — Aerosmith** and rendered the title and artist in Samsung's real lock-screen Now Bar. Devices not listed here are not claimed as confirmed compatible.

See [docs/COMPATIBILITY.md](docs/COMPATIBILITY.md) for the testing policy.

## Installation

Public releases distribute a paired set of APKs:

- `AmbientMusicMod-OneUI.apk`
- `PixelAmbientMusic-1.3.5-paired.apk`

Install **Pixel Ambient Music first**, then **Ambient Music for One UI**. Both APKs must be signed by the same project certificate because communication between the two components is protected by signature-level permissions.

If you are migrating from upstream-signed Ambient Music Mod / Now Playing builds, back up your Ambient Music Mod data first. Android will not accept an in-place update when the signing certificate changes, so uninstalling the previous pair may be required.

Full instructions: [docs/INSTALLATION.md](docs/INSTALLATION.md).

## How it works

The One UI integration attaches to the central recognition state inside `AmbientMusicModForegroundService`, alongside Ambient Music Mod's existing presentation paths. A successful `RecognitionState.Recognised` is mapped to title, artist and timeout metadata, then published by `AndroidLiveUpdateNowPlayingSurfacePublisher` as a public Android notification with promoted ongoing behavior requested when supported.

The implementation uses public Android APIs, including Android's promoted ongoing / Live Update notification capabilities. Samsung SystemUI makes the final decision about whether an eligible notification is surfaced in Now Bar.

It does **not** use:

- private Samsung framework APIs;
- Samsung package impersonation;
- fake MediaSession behavior;
- root SystemUI modifications;
- privileged Samsung framework hooks.

Architecture details: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Limitations

This project is experimental. The initial release is intentionally conservative about compatibility claims.

Ambient recognition remains dependent on the same prerequisites as upstream Ambient Music Mod, including Shizuku where upstream requires it. A valid Android Live Update request also does not guarantee that every OEM or One UI version will choose to render the notification in a Now Bar surface.

This repository is not intended to become a generic notification-to-Now-Bar bridge for arbitrary applications.

## Building from source

The project currently targets:

- Android 16 / `compileSdk 36`
- `targetSdk 36`
- JDK 17
- AndroidX Core 1.17+

Release builds require a signing configuration in `local.properties` compatible with the existing Gradle setup. The signing key used for public releases is intentionally not part of this repository.

Because Pixel Ambient Music and Ambient Music Mod use signature-protected communication, a compatible PAM APK must be signed with the same certificate as the AMM build.

CI uses a disposable key for compile/test validation only. Public release artifacts use the stable release signer provided to GitHub Actions through encrypted repository secrets.

## Paired signing

The paired-signing workflow verifies all of the following before producing release-ready artifacts:

1. the pinned Pixel Ambient Music package is the expected upstream binary;
2. both APK package IDs are correct;
3. both APKs verify successfully with `apksigner`;
4. both APKs have the same certificate SHA-256 digest;
5. that digest matches the configured stable release certificate;
6. SHA-256 checksums are generated for the public assets.

No keystore, signing password, recovery file or `local.properties` containing secrets is committed to the repository.

## Credits & Acknowledgements

This project exists because of substantial upstream work. Credit belongs clearly and prominently to:

- **Kieron Quinn** — creator of Ambient Music Mod and the related Now Playing porting work that forms the primary technical base of this project.
- **[KieronQuinn/AmbientMusicMod](https://github.com/KieronQuinn/AmbientMusicMod)** — upstream application and recognition integration.
- **[KieronQuinn/NowPlaying](https://github.com/KieronQuinn/NowPlaying)** — Pixel Ambient Music / Now Playing component used by Ambient Music Mod.
- **Google / Android Open Source Project** — Android platform APIs used by this project, including notification and Live Update capabilities. No endorsement or affiliation is implied.
- **Samsung** — One UI and Now Bar are the target presentation environment. No endorsement or affiliation is implied.
- **Jorge Palacios (`Jorgeprdz`)** — One UI Live Update / Now Bar integration, paired-signing workflow, Samsung Galaxy S25 physical validation, derivative packaging, documentation and maintenance.

The recognition engine and original Ambient Music Mod application are upstream work and are not claimed as original work of this derivative. See [docs/UPSTREAM.md](docs/UPSTREAM.md) for provenance details.

## License

Ambient Music for One UI is distributed under the **GNU General Public License v3.0**, consistent with its GPLv3 upstream base. See [LICENSE](LICENSE).

Modified source corresponding to distributed builds is kept in this repository. Existing upstream copyright and license notices should be preserved when modifying source files.
