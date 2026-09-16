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

## One UI derivative work

Jorge Palacios (`Jorgeprdz`) maintains this derivative and contributed the following derivative-specific work:

- Android Live Update / promoted ongoing publisher for successful recognition results;
- Samsung One UI Now Bar integration at the central recognition-state layer;
- capability gating and safe fallback behavior;
- mapper and lifecycle tests for the presentation surface;
- paired-signing build workflow for Ambient Music Mod and Pixel Ambient Music;
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
