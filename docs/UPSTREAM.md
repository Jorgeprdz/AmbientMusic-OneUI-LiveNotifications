# Upstream and attribution

Ambient Music for One UI is a modified GPLv3 derivative built on top of Kieron Quinn's Android projects.

## Primary upstream projects

- [KieronQuinn/AmbientMusicMod](https://github.com/KieronQuinn/AmbientMusicMod) — primary application, recognition integration, UI, settings, history, backup/restore, Shizuku integration and service architecture.
- [KieronQuinn/NowPlaying](https://github.com/KieronQuinn/NowPlaying) — Pixel Ambient Music / Now Playing component used by Ambient Music Mod.

The initial One UI work was audited against these upstream reference commits:

- Ambient Music Mod: `061cc6458f1731108fd2e16605d13e57207dc64f`
- NowPlaying: `6dc9086b9b223fb0f804d13b6d8ce83cdc902b83`

The first public derivative baseline was imported from `Jorgeprdz/AmbientMusicMod` branch `feature/nowbar-m1m2` at:

- `f4c8987f680e0ab5b911b4197ff625ea7e6ad176`

## Automated Ambient Music Mod synchronization

`.github/workflows/upstream-sync.yml` monitors exactly `KieronQuinn/AmbientMusicMod@main` once per day and can also be run manually.

The workflow uses a real Git merge so upstream history and authorship remain visible. It never replaces this repository with a copied snapshot.

Before any upstream merge can reach `main`, the candidate must pass:

1. `:app:testDebugUnitTest`
2. `:app:assembleDebugAndroidTest`
3. `:app:compileReleaseKotlin`

These checks use a disposable CI signing key. The upstream-sync workflow does **not** read the stable paired-release signer or any signing recovery material.

The workflow is fail-closed:

- merge conflict → `main` is unchanged;
- test or compile regression → `main` is unchanged;
- `main` moving while validation runs → `main` is unchanged;
- rejected normal push → `main` is unchanged.

Blocked runs create or update one issue titled **`Upstream sync blocked`** with the upstream SHA, failure stage and workflow URL. Repeated failures update that same open issue. A later successful/no-update run closes it.

There is no force-push and no automatic conflict resolution.

## Update routing in the derivative

Ambient Music for One UI deliberately separates its two release sources:

- Application/self updates: `Jorgeprdz/AmbientMusic-OneUI-LiveNotifications`
- Pixel Ambient Music updates: `KieronQuinn/NowPlaying`

The app's **About → GitHub** action points to this derivative repository. Upstream remains credited here and throughout the project documentation.

The public paired release includes both AMM and PAM APKs, but the AMM self-updater selects `AmbientMusicMod-OneUI.apk` by filename rather than relying on GitHub asset ordering.

## One UI derivative work

Jorge Palacios (`Jorgeprdz`) maintains this derivative and contributed the following derivative-specific work:

- Android Live Update / promoted ongoing publisher for successful recognition results;
- Samsung One UI Now Bar integration at the central recognition-state layer;
- capability gating and safe fallback behavior;
- mapper and lifecycle tests for the presentation surface;
- paired-signing build and public release workflow for Ambient Music Mod and Pixel Ambient Music;
- fail-closed automatic synchronization with Ambient Music Mod upstream;
- physical validation on Samsung Galaxy S25 `SM-S931B`;
- derivative packaging, compatibility documentation and release process.

The recognition engine, original Ambient Music Mod application, Pixel Ambient Music porting work and the majority of the application architecture are upstream work and are not claimed as original work of this derivative.

## Other upstream software

Ambient Music Mod itself contains third-party dependencies and a local version of Google's `private-compute-services`; their original licenses and notices remain applicable. Existing source-file notices must be preserved when modifying upstream files.

## License relationship

Ambient Music Mod and NowPlaying are distributed under GPLv3. This derivative therefore remains GPLv3 when distributing modified source and binaries.

The project must continue to provide corresponding source for distributed derivative builds and must not remove applicable upstream copyright or license notices.

## Non-affiliation

References to Google, Android, Samsung, One UI, Pixel and Now Bar describe upstream technologies or target platforms only. They do not imply sponsorship, approval or affiliation.
