# Verification

This document defines the evidence expected before claiming a device is confirmed compatible.

## 1. Build and test verification

CI must complete successfully for the commit under test:

```bash
./gradlew :app:testDebugUnitTest --stacktrace
./gradlew :app:assembleDebugAndroidTest --stacktrace
./gradlew :app:compileReleaseKotlin --stacktrace
```

The public CI workflow uses an ephemeral signing key only to satisfy the inherited Gradle signing configuration during validation. It is not the public release signer.

## 2. Package verification

Expected package IDs:

```text
com.kieronquinn.app.ambientmusicmod
com.kieronquinn.app.pixelambientmusic
```

Verify installed packages:

```bash
adb shell pm path com.kieronquinn.app.ambientmusicmod
adb shell pm path com.kieronquinn.app.pixelambientmusic
```

## 3. Recognition verification

Trigger a normal Ambient Music Mod recognition on the physical device and confirm that a real track result is returned.

For the initial Galaxy S25 validation, the recognised track was:

```text
Dream On — Aerosmith
```

A successful recognition verifies only the upstream recognition path. It does not by itself prove Now Bar integration.

## 4. Notification verification

The derivative publisher uses:

```text
Package: com.kieronquinn.app.ambientmusicmod
Channel: now_playing_surface_v1
Notification ID: 0x4E50 (20048 decimal)
```

Inspect notification state with:

```bash
adb shell dumpsys notification --noredact \
  | grep -i -A40 -B10 -E 'now_playing_surface_v1|ambientmusicmod|promoted|FLAG_PROMOTED_ONGOING'
```

Evidence should show the Ambient Music Mod notification and, when Android grants promotion, the promoted ongoing state or corresponding promoted flag in system output.

Do not infer promotion merely from the app requesting it.

## 5. Samsung Now Bar verification

Lock the device while the recognised result remains active.

A confirmed result requires visual evidence that Samsung SystemUI renders the recognised title and artist inside the real Now Bar surface.

This is separate from application-level notification verification because Samsung SystemUI owns the final presentation decision.

## 6. Capability boundary

The publisher requests promoted behavior only when its runtime checks pass. A device may legitimately fall back to a standard notification if one or more platform capabilities are unavailable.

Do not bypass that state with private Samsung APIs, package spoofing, privileged permissions or SystemUI modifications when verifying this project.

## 7. Evidence hygiene

Before attaching logs or screenshots to GitHub:

- remove account identifiers and personal notifications;
- avoid publishing device serial numbers;
- do not include signing passwords, keystore contents or recovery text;
- crop screenshots to the relevant UI when practical.

## Initial validated target

```text
Device: Samsung Galaxy S25
Model: SM-S931B
Android: 16
One UI: 8.x
Recognition: PASS
Samsung Now Bar visual rendering: PASS
```
