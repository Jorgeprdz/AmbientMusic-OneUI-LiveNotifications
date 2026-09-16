# Ambient Music for One UI — Live Notifications

## Status

Design approved for implementation.

This document defines a new public GitHub project derived from the current `feature/nowbar-m1m2` work in `Jorgeprdz/AmbientMusicMod`.

## Project identity

**Repository name:** `AmbientMusic-OneUI-LiveNotifications`

**Display name:** **Ambient Music for One UI**

**Tagline:** *Now Playing recognition in Samsung's Now Bar using Android Live Updates.*

The project is an unofficial community derivative focused on bringing Ambient Music Mod recognition results into Samsung One UI's Now Bar through public Android Live Update APIs.

The project must prominently state that it is not affiliated with Samsung, Google, or Kieron Quinn.

## Product scope

The project keeps Ambient Music Mod's existing recognition pipeline and adds a Samsung One UI presentation layer for successful recognitions.

The One UI Live Update path is always enabled in this derivative. There is no user-facing toggle for the promoted Live Update integration.

The supported flow is:

`Pixel Ambient Music / Now Playing -> Ambient Music Mod recognition -> RecognitionState.Recognised -> NowPlayingSurfaceLifecycle -> Android Live Update notification -> Samsung Now Bar`

The implementation must continue to use public Android notification APIs only. It must not depend on Samsung private APIs, package spoofing, fake MediaSession behavior, root SystemUI changes, or privileged Samsung framework hooks.

## Base and upstream relationship

The new repository is derived from:

- `KieronQuinn/AmbientMusicMod`
- `KieronQuinn/NowPlaying`
- the validated One UI Live Update work currently in `Jorgeprdz/AmbientMusicMod` on `feature/nowbar-m1m2`

The new repository should preserve the full source needed to build the derivative, not merely publish a patch file.

The project may use its own repository identity and documentation, but it must clearly identify Ambient Music Mod as the primary upstream base and must not imply that the recognition engine was created by this derivative.

## Licensing

Ambient Music Mod and Now Playing are GPLv3 projects. This derivative must therefore remain GPLv3 when distributing modified source and binaries.

The repository must:

- include the GPLv3 license text;
- retain applicable copyright and license notices;
- mark the project as a modified derivative;
- make the corresponding source available for distributed binaries;
- keep third-party license notices intact;
- preserve existing upstream notices in source files where applicable.

No proprietary relicensing of upstream GPLv3 code is permitted.

## Credits and attribution

The README must contain a prominent **Credits & Acknowledgements** section.

Required credits:

- **Kieron Quinn** — creator of Ambient Music Mod and the related Now Playing porting work that forms the primary technical base of this project.
- **KieronQuinn/AmbientMusicMod** — upstream application and recognition integration.
- **KieronQuinn/NowPlaying** — Pixel Ambient Music / Now Playing component used by Ambient Music Mod.
- **Google / Android Open Source Project** — Android platform APIs, including notification and Live Update capabilities used by this project. This does not imply endorsement or affiliation.
- **Samsung** — One UI and Now Bar as the target presentation environment. This does not imply endorsement or affiliation.
- **Jorge Palacios (`Jorgeprdz`)** — One UI Live Update / Now Bar integration, paired-signing workflow, Samsung Galaxy S25 physical validation, packaging, documentation, and maintenance of this derivative.

The project must avoid language that implies ownership of the upstream recognition implementation.

## Application behavior

### Recognition path

The existing Ambient Music Mod recognition architecture remains unchanged.

The Now Bar integration must remain connected at the central service-level recognition state, next to the existing widget / overlay presentation paths.

On `RecognitionState.Recognised`:

- map track title and artist into a `NowPlayingSurfaceEvent`;
- derive the timeout from recognition metadata where available;
- publish a public, ongoing status notification;
- request promoted ongoing behavior when the Android platform reports the required capabilities.

On failed recognition or error:

- clear the Now Bar / Live Update notification.

On recording or recognising states:

- do not replace the existing successful result with a transient state.

On service destruction or disable:

- clear the Live Update notification.

### Promotion policy

This derivative always uses the experimental promoted policy when its capability checks pass.

Promotion remains gated by runtime platform checks, including:

- notification permission;
- app notification enablement;
- channel availability;
- promotable characteristics;
- Android API support;
- `canPostPromotedNotifications()` capability.

If promotion is unavailable, the notification may fall back to standard notification behavior rather than crashing or using unsupported workarounds.

## Compatibility

Initial tested target:

- Samsung Galaxy S25
- Model: `SM-S931B`
- Android 16
- One UI 8.x

Initial physical validation:

- successful recognition of `Dream On` by Aerosmith;
- title and artist rendered in Samsung Now Bar;
- persistent lock-screen Now Bar presentation after recognition;
- visible progress treatment in the Now Bar surface.

The README must distinguish **tested** devices from **expected** compatibility. Devices not physically validated must not be presented as confirmed compatible.

## Package and signing model

The project currently depends on signature-protected communication between Ambient Music Mod and Pixel Ambient Music / Now Playing.

The public release process therefore distributes a paired set of APKs signed with the same project signing key:

- `AmbientMusicMod-OneUI.apk`
- `PixelAmbientMusic-1.3.5-paired.apk`

The signing key itself must never be committed to the repository or uploaded as a public artifact.

GitHub Actions may receive signing material only through repository secrets.

The public repository must document that both APKs need the same signer and that installing this derivative over an upstream-signed installation may require uninstalling the upstream pair first.

## CI and release pipeline

The repository must provide GitHub Actions workflows that:

1. set up JDK 17 and Android SDK API 36;
2. load the stable project signing key from encrypted GitHub Actions secrets;
3. run unit tests;
4. compile instrumentation-test sources;
5. build the Ambient Music Mod release APK;
6. obtain the pinned Pixel Ambient Music / Now Playing release used by the project;
7. sign both APKs with the same stable key;
8. verify both APK signatures;
9. verify the package IDs;
10. verify that both certificate SHA-256 digests match the configured expected certificate;
11. generate SHA-256 checksums;
12. upload a release-ready paired artifact bundle.

The workflow must fail closed on signer mismatches, missing secrets, package mismatches, test failures, or APK verification failures.

## Release model

The first public release should be `v0.1.0` and clearly marked **Experimental**.

Release assets:

- `AmbientMusicMod-OneUI.apk`
- `PixelAmbientMusic-1.3.5-paired.apk`
- `SHA256SUMS.txt`

The release notes must state:

- tested device and software version;
- experimental status;
- requirement to install the paired APKs together;
- upgrade / signature caveat;
- use of public Android Live Update APIs;
- absence of Samsung private API dependency;
- upstream acknowledgements and GPLv3 source availability.

## Repository presentation

The repository should look like a maintained open-source project rather than a throwaway test dump.

Required top-level documentation:

- `README.md`
- `LICENSE`
- `CHANGELOG.md`
- `CONTRIBUTING.md`
- `SECURITY.md`
- `docs/ARCHITECTURE.md`
- `docs/INSTALLATION.md`
- `docs/COMPATIBILITY.md`
- `docs/UPSTREAM.md`

Recommended README structure:

1. project title and concise tagline;
2. screenshot / visual proof from a real Samsung device;
3. status badges;
4. what the project does;
5. compatibility;
6. installation;
7. how it works;
8. limitations;
9. building from source;
10. release signing model;
11. credits and acknowledgements;
12. license;
13. non-affiliation notice.

The README should use restrained technical language and avoid marketing claims that exceed tested evidence.

## Screenshot and media policy

A real Samsung Galaxy S25 screenshot showing the recognised song in Now Bar should be used as the primary proof image when added to the new repository.

Before publication, screenshots should be checked for personal or account information and cropped if necessary.

No third-party logos should be presented in a way that suggests sponsorship or endorsement.

## Security and secrets

The following must never be committed:

- project signing keystore;
- signing passwords;
- recovery text containing signing credentials;
- local properties containing secrets;
- GitHub secret exports;
- device-specific private data.

The repository should include a `SECURITY.md` explaining how to report security issues and explicitly noting that release signing material is intentionally excluded from source control.

## Non-goals

This project will not initially:

- replace Samsung SystemUI;
- use private Samsung APIs;
- support root-only SystemUI patches;
- impersonate LiveBridge or another package;
- provide a generic notification-to-Now-Bar bridge for arbitrary apps;
- redesign Ambient Music Mod's recognition engine;
- claim compatibility with every One UI device;
- remove the requirement for Shizuku where Ambient Music Mod itself requires it.

## Verification requirements

Before the first public release, implementation is considered release-ready only after all of the following are verified:

- unit tests pass;
- instrumentation-test sources compile;
- release build succeeds;
- paired APK signatures match the stable project signer;
- package IDs are correct;
- physical recognition succeeds on the Galaxy S25;
- the recognition is visible in Samsung Now Bar;
- `dumpsys notification` evidence confirms the promoted ongoing state when present;
- README installation steps are followed once from a clean install;
- no signing secrets or private device data are present in the repository history intended for publication.

## Initial project state

The initial new-repository import should be based on the current validated One UI implementation from `feature/nowbar-m1m2`, including:

- Android 16 / compileSdk 36 support;
- public Live Update publisher;
- promotion capability checks;
- recognition-state mapper and lifecycle;
- service-level production wiring;
- stable paired-signing CI strategy;
- verified Galaxy S25 physical Now Bar behavior.

The new repository will then receive its own README, project documentation, workflows, branding text, release metadata, and public release process while retaining the GPLv3 upstream source and attribution obligations.
