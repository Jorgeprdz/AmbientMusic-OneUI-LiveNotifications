# Installation

## Release pair

A working public release consists of two APKs signed with the same project certificate plus a checksum file:

- `PixelAmbientMusic-1.3.5-paired.apk`
- `AmbientMusicMod-OneUI.apk`
- `SHA256SUMS.txt`

Package IDs:

```text
PAM: com.kieronquinn.app.pixelambientmusic
AMM: com.kieronquinn.app.ambientmusicmod
```

The first public derivative release is `v0.1.0 Experimental` with AMM `versionCode 241`.

## Before migrating

If Ambient Music Mod is already installed, create an application backup before uninstalling anything. A signing-certificate change prevents Android from accepting an in-place update from an upstream-signed installation to this derivative pair.

If the previous AMM/PAM installation uses a different signer, both packages may need to be uninstalled before the derivative pair can be installed.

If you already installed one of the pre-release paired derivative builds from this project, `v0.1.0` uses the same stable project signer and a higher AMM versionCode, so the AMM APK is intended to update that derivative in place.

## Verify downloads

Keep the three release files in one directory and run:

```bash
sha256sum -c SHA256SUMS.txt
```

Both APK entries should report `OK` before installation.

## Install order

1. Install `PixelAmbientMusic-1.3.5-paired.apk`.
2. Install `AmbientMusicMod-OneUI.apk`.
3. Launch Ambient Music for One UI.
4. Complete the same Shizuku and recognition setup required by upstream Ambient Music Mod.
5. Grant notification permission when Android requests it.

The order matters because Ambient Music Mod expects the Pixel Ambient Music service to be available during setup.

## Why the pair must match

Ambient Music Mod and Pixel Ambient Music use signature-protected communication. Android only grants those signature-level permissions when both packages are signed with the same certificate.

For official project releases, the release workflow verifies the certificate digest on both APKs before the pair is published.

## Shizuku

This derivative does not remove upstream Ambient Music Mod requirements. Where upstream Ambient Music Mod requires Shizuku, this derivative requires it as well.

No root access is required for the One UI Live Update integration itself.

## Notification requirements

The Now Bar path depends on Android notification capabilities. Keep app notifications enabled and allow notification permission on Android versions that request it.

On supported Android versions, the app requests promoted ongoing behavior only after capability checks pass. Samsung SystemUI then decides whether the Live Update is displayed in Now Bar.

## Clean migration with ADB

Advanced users may verify package state with:

```bash
adb shell pm path com.kieronquinn.app.pixelambientmusic
adb shell pm path com.kieronquinn.app.ambientmusicmod
```

Install PAM first and AMM second:

```bash
adb install PixelAmbientMusic-1.3.5-paired.apk
adb install AmbientMusicMod-OneUI.apk
```

For an existing installation already signed with this derivative's stable signer, use normal package replacement for a newer versionCode:

```bash
adb install -r AmbientMusicMod-OneUI.apk
```

Do not use `-r` to force a signature-incompatible update. If Android reports a signing conflict, back up application data, uninstall the conflicting pair, then perform a clean install.

## Restoring an Ambient Music Mod backup

Use Ambient Music Mod's built-in Backup & Restore flow after the derivative installation is complete. A restore can recover only the data actually present in the backup file.

## Updating later releases

`v0.1.0` is the bootstrap release for this repository's self-update path. Builds created before `v0.1.0` still use the upstream Ambient Music Mod update endpoint, so install `v0.1.0` manually once from this repository.

From `v0.1.0` onward, **Check for updates** uses `Jorgeprdz/AmbientMusic-OneUI-LiveNotifications` for AMM updates while PAM continues to use `KieronQuinn/NowPlaying`.

The AMM updater selects `AmbientMusicMod-OneUI.apk` explicitly from paired releases. Future project releases signed with the same certificate and a higher versionCode can update in place, subject to normal Android package rules.
