# Ambient Music for One UI — Live Notifications Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Publish a professional GPLv3 derivative of Ambient Music Mod named **Ambient Music for One UI**, with the validated Samsung Now Bar Live Update integration always enabled, stable paired signing, reproducible CI, documentation, and an experimental `v0.1.0` release.

**Architecture:** Import the validated `feature/nowbar-m1m2` source as the new repository baseline, preserve Ambient Music Mod's recognition architecture, and keep the One UI presentation layer attached only to the central `RecognitionState` flow in `AmbientMusicModForegroundService`. Successful recognitions map to `NowPlayingSurfaceEvent` and publish through Android public Live Update APIs; the derivative always selects `EXPERIMENTAL_PROMOTED`, while the publisher still gates promotion on runtime platform capabilities.

**Tech Stack:** Android/Kotlin, Gradle 8.x, Android 16 / compileSdk 36, AndroidX Core 1.17+, Shizuku where upstream AMM requires it, GitHub Actions, JDK 17, Android SDK build-tools 35.0.0, `apksigner`, `aapt`, Bash, GitHub CLI, GPLv3.

**Spec:** `docs/superpowers/specs/2026-09-15-ambient-music-oneui-live-notifications-design.md`

## Global Constraints

- Repository: `Jorgeprdz/AmbientMusic-OneUI-LiveNotifications`.
- Display name: **Ambient Music for One UI**.
- Tagline: *Now Playing recognition in Samsung's Now Bar using Android Live Updates.*
- Unofficial derivative; no implied affiliation with Samsung, Google, or Kieron Quinn.
- GPLv3 remains the project license; retain upstream notices and corresponding source.
- The One UI Live Update path is always enabled in this derivative; no user-facing toggle.
- Public Android notification APIs only; no Samsung private APIs, package spoofing, fake MediaSession behavior, root SystemUI changes, or privileged Samsung framework hooks.
- Initial validated target: Samsung Galaxy S25 `SM-S931B`, Android 16, One UI 8.x.
- Preserve `compileSdk 36`, `targetSdk 36`, AndroidX Core 1.17+, and `POST_PROMOTED_NOTIFICATIONS`.
- Preserve the service-level hook beside widget/overlay presentation logic.
- Stable signing key and recovery material remain private and never enter git or public artifacts.
- Stable public certificate SHA-256: `321c34014d346541ab46637242d19fd1b18279b88fc2daa602fd47baa81dc815`.
- Pinned upstream PAM 1.3.5 APK SHA-256: `11e3f12439d1b00e93174c6d2d9d6ff9000daec4e0bd03711d52cb25310223c1`.
- Release assets: `AmbientMusicMod-OneUI.apk`, `PixelAmbientMusic-1.3.5-paired.apk`, `SHA256SUMS.txt`.
- First release: `v0.1.0`, marked **Experimental / prerelease**.
- CI fails closed on missing signing inputs, PAM source hash mismatch, signer mismatch, package mismatch, test failure, or APK verification failure.

---

## File Structure

**Validated implementation:**

- `app/src/main/java/com/kieronquinn/app/ambientmusicmod/components/nowplayingsurface/AndroidLiveUpdateNowPlayingSurfacePublisher.kt` — public Android Live Update notification publisher and capability checks.
- `app/src/main/java/com/kieronquinn/app/ambientmusicmod/components/nowplayingsurface/NowPlayingSurfacePolicy.kt` — promotion decision.
- `app/src/main/java/com/kieronquinn/app/ambientmusicmod/components/nowplayingsurface/NowPlayingSurfaceLifecycle.kt` — recognition-state lifecycle.
- `app/src/main/java/com/kieronquinn/app/ambientmusicmod/components/nowplayingsurface/RecognitionNowPlayingSurfaceMapper.kt` — title/artist/timeout mapping.
- `app/src/main/java/com/kieronquinn/app/ambientmusicmod/service/AmbientMusicModForegroundService.kt` — central production hook, using `EXPERIMENTAL_PROMOTED`.
- `app/src/main/AndroidManifest.xml` — Android notification permissions.

**Tests:**

- `app/src/test/java/com/kieronquinn/app/ambientmusicmod/components/nowplayingsurface/NowPlayingSurfacePolicyTest.kt`
- `app/src/test/java/com/kieronquinn/app/ambientmusicmod/components/nowplayingsurface/NowPlayingSurfaceLifecycleTest.kt`
- `app/src/test/java/com/kieronquinn/app/ambientmusicmod/components/nowplayingsurface/RecognitionNowPlayingSurfaceMapperTest.kt`

**Project documentation:**

- `README.md`
- `LICENSE`
- `CHANGELOG.md`
- `CONTRIBUTING.md`
- `SECURITY.md`
- `docs/ARCHITECTURE.md`
- `docs/INSTALLATION.md`
- `docs/COMPATIBILITY.md`
- `docs/UPSTREAM.md`
- `docs/VERIFICATION.md`
- `docs/assets/nowbar-s25-dream-on.jpg`

**CI / release:**

- `.github/workflows/ci.yml`
- `.github/workflows/paired-release.yml`
- `.github/workflows/release.yml`

---

### Task 1: Bootstrap the new public repository from the validated branch

**Files:**
- Source: complete tree from `Jorgeprdz/AmbientMusicMod` branch `feature/nowbar-m1m2`
- Destination repository: `Jorgeprdz/AmbientMusic-OneUI-LiveNotifications`

**Interfaces:**
- Consumes: current validated branch containing the Now Bar implementation and stable signing workflows.
- Produces: a public `main` branch with the same source/history baseline.

- [ ] **Step 1: Verify branch identity and implementation before publishing**

```bash
git fetch origin
git checkout feature/nowbar-m1m2
git status --short --branch
git log -1 --format='%H %s'

grep -n "NowPlayingSurfacePolicy.EXPERIMENTAL_PROMOTED" \
  app/src/main/java/com/kieronquinn/app/ambientmusicmod/service/AmbientMusicModForegroundService.kt

grep -R "setRequestPromotedOngoing" -n \
  app/src/main/java/com/kieronquinn/app/ambientmusicmod/components/nowplayingsurface

grep -n "POST_PROMOTED_NOTIFICATIONS" app/src/main/AndroidManifest.xml
```

Expected: clean branch, production service uses `EXPERIMENTAL_PROMOTED`, AndroidX promoted notification call exists, and manifest permission exists.

- [ ] **Step 2: Create the empty public repository with GitHub CLI**

```bash
gh repo create Jorgeprdz/AmbientMusic-OneUI-LiveNotifications \
  --public \
  --description "Now Playing recognition in Samsung's Now Bar using Android Live Updates." \
  --disable-wiki
```

Expected: repository creation succeeds and no README/license is auto-generated separately.

- [ ] **Step 3: Publish the validated branch as the new repository's `main`**

```bash
git remote remove oneui 2>/dev/null || true
git remote add oneui https://github.com/Jorgeprdz/AmbientMusic-OneUI-LiveNotifications.git
git push oneui feature/nowbar-m1m2:main
```

- [ ] **Step 4: Verify local and remote HEAD are identical**

```bash
LOCAL_SHA="$(git rev-parse feature/nowbar-m1m2)"
REMOTE_SHA="$(git ls-remote oneui refs/heads/main | awk '{print $1}')"
printf 'LOCAL=%s\nREMOTE=%s\n' "$LOCAL_SHA" "$REMOTE_SHA"
test "$LOCAL_SHA" = "$REMOTE_SHA"
```

Expected: exit 0.

---

### Task 2: Lock the always-on promoted integration with tests

**Files:**
- Verify: `app/src/main/java/com/kieronquinn/app/ambientmusicmod/service/AmbientMusicModForegroundService.kt`
- Verify: `app/src/main/java/com/kieronquinn/app/ambientmusicmod/components/nowplayingsurface/NowPlayingSurfacePolicy.kt`
- Test: `app/src/test/java/com/kieronquinn/app/ambientmusicmod/components/nowplayingsurface/NowPlayingSurfacePolicyTest.kt`
- Test: `app/src/test/java/com/kieronquinn/app/ambientmusicmod/components/nowplayingsurface/NowPlayingSurfaceLifecycleTest.kt`

**Interfaces:**
- Consumes: `NowPlayingSurfacePolicy.EXPERIMENTAL_PROMOTED`, `NowPlayingSurfaceCapabilities`, `NowPlayingSurfaceLifecycle`.
- Produces: regression coverage proving promotion is capability-gated and transient recognition states do not erase a successful surface.

- [ ] **Step 1: Add/confirm the policy regression for incomplete capabilities**

```kotlin
@Test
fun `experimental promoted requires promotion capability`() {
    val capabilities = NowPlayingSurfaceCapabilities(
        notificationsEnabled = true,
        channelBlocked = false,
        hasPromotableCharacteristics = true,
        canPostPromotedNotifications = false,
        postNotificationsGranted = true
    )

    assertFalse(
        NowPlayingSurfacePolicy.EXPERIMENTAL_PROMOTED
            .shouldRequestPromotion(capabilities)
    )
}
```

- [ ] **Step 2: Run focused policy tests**

```bash
./gradlew :app:testDebugUnitTest --tests '*NowPlayingSurfacePolicyTest*' --stacktrace
```

Expected: PASS.

- [ ] **Step 3: Add/confirm lifecycle tests for clear and transient behavior**

Use the existing test fakes in `NowPlayingSurfaceLifecycleTest.kt`; the assertions must prove:

```kotlin
assertEquals(1, publisher.clearCount) // Failed/Error
assertEquals(0, publisher.clearCount) // Recording/Recognising
assertTrue(publisher.events.isEmpty()) // Recording/Recognising
```

- [ ] **Step 4: Run focused lifecycle tests**

```bash
./gradlew :app:testDebugUnitTest --tests '*NowPlayingSurfaceLifecycleTest*' --stacktrace
```

Expected: PASS.

- [ ] **Step 5: Verify there is no settings toggle in the production publisher construction**

```bash
grep -n -A8 -B4 "nowPlayingSurfacePublisher" \
  app/src/main/java/com/kieronquinn/app/ambientmusicmod/service/AmbientMusicModForegroundService.kt
```

Expected: direct construction with `NowPlayingSurfacePolicy.EXPERIMENTAL_PROMOTED` and no `SettingsRepository` flag controlling the integration.

- [ ] **Step 6: Commit only if test/source changes were required**

```bash
git add app/src/main/java/com/kieronquinn/app/ambientmusicmod/components/nowplayingsurface \
        app/src/main/java/com/kieronquinn/app/ambientmusicmod/service/AmbientMusicModForegroundService.kt \
        app/src/test/java/com/kieronquinn/app/ambientmusicmod/components/nowplayingsurface
git diff --cached --quiet || git commit -m "test: lock One UI promoted Live Update behavior"
git push oneui HEAD:main
```

---

### Task 3: Create the professional project identity, attribution, and policy docs

**Files:**
- Replace/Create: `README.md`
- Preserve: `LICENSE`
- Create: `CHANGELOG.md`
- Create: `CONTRIBUTING.md`
- Create: `SECURITY.md`
- Create: `docs/UPSTREAM.md`
- Create: `docs/ARCHITECTURE.md`
- Create: `docs/INSTALLATION.md`
- Create: `docs/COMPATIBILITY.md`
- Create: `docs/VERIFICATION.md`

**Interfaces:**
- Consumes: approved design and upstream GPLv3 lineage.
- Produces: authoritative public documentation used by the release.

- [ ] **Step 1: Replace README introduction with exact derivative identity**

```markdown
# Ambient Music for One UI

> Now Playing recognition in Samsung's Now Bar using Android Live Updates.

**Experimental · Unofficial community project**

Ambient Music for One UI is a GPLv3 derivative of Kieron Quinn's Ambient
Music Mod that publishes successful Now Playing recognitions as Android Live
Updates so supported Samsung One UI devices can present them in the Now Bar.

This project is not affiliated with or endorsed by Samsung, Google, or
Kieron Quinn.
```

README section order:

```text
Proof on real hardware
What it does
Tested compatibility
Installation
How it works
Limitations
Building from source
Paired signing
Credits & Acknowledgements
License
```

- [ ] **Step 2: Add exact tested compatibility wording**

```markdown
## Tested compatibility

| Device | Model | Android | One UI | Status |
|---|---|---:|---:|---|
| Samsung Galaxy S25 | SM-S931B | 16 | 8.x | Physically validated |

The initial validation recognised **Dream On — Aerosmith** and rendered the
title and artist in Samsung's real lock-screen Now Bar. Devices not listed
here are not claimed as confirmed compatible.
```

- [ ] **Step 3: Create `docs/UPSTREAM.md` with explicit provenance**

It must state:

```markdown
This project is a modified GPLv3 derivative of:

- KieronQuinn/AmbientMusicMod — primary application and recognition integration.
- KieronQuinn/NowPlaying — Pixel Ambient Music / Now Playing component used by Ambient Music Mod.

Jorge Palacios (`Jorgeprdz`) maintains this derivative and contributed the
Android Live Update / Samsung Now Bar integration, paired-signing workflow,
Galaxy S25 physical validation, packaging, and derivative documentation.

The recognition engine and original Ambient Music Mod application are
upstream work and are not claimed as original work of this derivative.
```

Also record the upstream source commit pins used for `v0.1.0`.

- [ ] **Step 4: Create `docs/ARCHITECTURE.md` with the exact data path**

```text
Pixel Ambient Music / Now Playing
        ↓
RecognitionRepository
        ↓
AmbientMusicModForegroundService.recognitionState
        ↓
NowPlayingSurfaceLifecycle
        ↓
RecognitionNowPlayingSurfaceMapper
        ↓
AndroidLiveUpdateNowPlayingSurfacePublisher
        ↓
Android promoted ongoing notification
        ↓
Samsung SystemUI / Now Bar (platform decision)
```

State explicitly that Samsung SystemUI decides whether a valid promoted notification appears in Now Bar.

- [ ] **Step 5: Create `docs/INSTALLATION.md`**

Document package IDs:

```text
PAM: com.kieronquinn.app.pixelambientmusic
AMM: com.kieronquinn.app.ambientmusicmod
```

Document installation order: PAM first, AMM second. Warn that upstream-signed builds are not signature-compatible with this derivative pair and that users should back up AMM data before uninstalling.

- [ ] **Step 6: Create `docs/COMPATIBILITY.md`**

The only confirmed device at `v0.1.0` is Samsung Galaxy S25 `SM-S931B`, Android 16, One UI 8.x. Put other devices under an explicitly unvalidated section.

- [ ] **Step 7: Create `SECURITY.md` and `CONTRIBUTING.md`**

`SECURITY.md` must explicitly prohibit committing:

```text
*.p12
*.jks
*.keystore
local.properties
NOWBAR-RECOVERY-KEEP-PRIVATE.txt
signing passwords
```

`CONTRIBUTING.md` must require tests, public Android APIs only, no signing secrets, and physical evidence before adding a device to confirmed compatibility.

- [ ] **Step 8: Create `CHANGELOG.md`**

```markdown
# Changelog

## [0.1.0] - 2026-09-15

### Added
- Experimental Android Live Update integration for Samsung One UI Now Bar.
- Service-level publication of successful Ambient Music Mod recognitions.
- Capability-gated promoted ongoing notifications using public Android APIs.
- Stable paired-signing release workflow for AMM and Pixel Ambient Music.
- Physical validation on Samsung Galaxy S25 SM-S931B.
```

- [ ] **Step 9: Verify docs and license**

```bash
grep -q "GNU GENERAL PUBLIC LICENSE" LICENSE
grep -qi "Kieron Quinn" README.md
grep -qi "not affiliated" README.md
grep -qi "GPLv3" README.md
grep -qi "Jorgeprdz" docs/UPSTREAM.md
! grep -RniE '\b(TODO|TBD|FIXME)\b' \
  README.md CHANGELOG.md CONTRIBUTING.md SECURITY.md docs/ARCHITECTURE.md \
  docs/INSTALLATION.md docs/COMPATIBILITY.md docs/UPSTREAM.md docs/VERIFICATION.md
```

Expected: all checks exit 0.

- [ ] **Step 10: Commit documentation**

```bash
git add README.md LICENSE CHANGELOG.md CONTRIBUTING.md SECURITY.md docs
git commit -m "docs: establish Ambient Music for One UI project"
git push oneui HEAD:main
```

---

### Task 4: Add sanitized real-device proof media

**Files:**
- Create: `docs/assets/nowbar-s25-dream-on.jpg`
- Modify: `README.md`

**Interfaces:**
- Consumes: the real Galaxy S25 screenshot from the successful `Dream On — Aerosmith` validation.
- Produces: privacy-reviewed visual proof referenced by README.

- [ ] **Step 1: Use the lock-screen screenshot that shows the Now Bar result**

Before adding it, verify the image contains no email address, phone number, account identifier, location detail, unrelated notification content, or other private data. Crop only enough to remove private data while retaining lock-screen context proving this is Samsung's actual Now Bar.

- [ ] **Step 2: Save the reviewed image at the canonical repository path**

```bash
mkdir -p docs/assets
cp /sdcard/Download/nowbar-s25-dream-on.jpg docs/assets/nowbar-s25-dream-on.jpg
```

The execution workflow must first copy/export the approved screenshot to `/sdcard/Download/nowbar-s25-dream-on.jpg`; do not substitute a generated mockup.

- [ ] **Step 3: Add the README image reference**

```markdown
![Ambient Music for One UI showing Dream On by Aerosmith in Samsung Now Bar](docs/assets/nowbar-s25-dream-on.jpg)
```

- [ ] **Step 4: Commit**

```bash
git add README.md docs/assets/nowbar-s25-dream-on.jpg
git commit -m "docs: add Galaxy S25 Now Bar validation image"
git push oneui HEAD:main
```

---

### Task 5: Replace prototype workflows with production CI and stable paired packaging

**Files:**
- Create: `.github/workflows/ci.yml`
- Create: `.github/workflows/paired-release.yml`
- Remove after parity: `.github/workflows/m0-live-update.yml`
- Remove after parity: `.github/workflows/m1m2-nowbar.yml`
- Remove after parity: `.github/workflows/nowbar-paired-bundle.yml`

**Interfaces:**
- Consumes: Gradle project, stable signing secrets, pinned official PAM 1.3.5 artifact.
- Produces: unsigned validation CI and a signed paired bundle verified against the stable certificate.

- [ ] **Step 1: Create `.github/workflows/ci.yml` using non-signing Gradle tasks**

```yaml
name: CI

on:
  push:
    branches: [main]
  pull_request:
  workflow_dispatch:

permissions:
  contents: read

jobs:
  validate:
    runs-on: ubuntu-latest
    timeout-minutes: 35
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v5
        with:
          distribution: temurin
          java-version: '17'
      - uses: gradle/actions/setup-gradle@v4
      - uses: android-actions/setup-android@v4
        with:
          packages: ''
          log-accepted-android-sdk-licenses: false
      - name: Install Android API 36
        run: sdkmanager "platform-tools" "platforms;android-36" "build-tools;35.0.0"
      - name: Unit tests
        run: ./gradlew :app:testDebugUnitTest --stacktrace
      - name: Compile instrumentation APK
        run: ./gradlew :app:assembleDebugAndroidTest --stacktrace
      - name: Compile production release sources
        run: ./gradlew :app:compileReleaseKotlin --stacktrace
```

- [ ] **Step 2: Create `.github/workflows/paired-release.yml` from the validated stable workflow**

Use JDK 17, Android API 36, build-tools 35.0.0, and these secrets:

```text
NOWBAR_KEYSTORE_B64
NOWBAR_STORE_PASSWORD
NOWBAR_KEY_ALIAS
NOWBAR_KEY_PASSWORD
NOWBAR_CERT_SHA256
```

Before decoding the key, fail if any value is empty.

- [ ] **Step 3: Pin and verify upstream PAM 1.3.5 before signing**

```bash
curl -fL --retry 3 \
  'https://github.com/KieronQuinn/NowPlaying/releases/download/1.3.5/NowPlaying-v1.3.5.apk' \
  -o pair/PixelAmbientMusic-1.3.5-official.apk

echo '11e3f12439d1b00e93174c6d2d9d6ff9000daec4e0bd03711d52cb25310223c1  pair/PixelAmbientMusic-1.3.5-official.apk' \
  | sha256sum -c -
```

Expected: `OK`; otherwise workflow stops.

- [ ] **Step 4: Build AMM release using the stable secret-backed signing config**

```bash
./gradlew :app:testDebugUnitTest --stacktrace
./gradlew :app:assembleDebugAndroidTest --stacktrace
./gradlew :app:assembleRelease --stacktrace
```

- [ ] **Step 5: Produce canonical filenames and sign PAM with the same key**

```bash
mkdir -p pair
AMM_SOURCE="$(find app/build/outputs/apk/release -maxdepth 1 -type f -name '*.apk' | head -n1)"
test -n "$AMM_SOURCE"
cp "$AMM_SOURCE" pair/AmbientMusicMod-OneUI.apk

BUILD_TOOLS="$ANDROID_SDK_ROOT/build-tools/35.0.0"
"$BUILD_TOOLS/zipalign" -f -p 4 \
  pair/PixelAmbientMusic-1.3.5-official.apk \
  pair/PixelAmbientMusic-1.3.5-aligned.apk

"$BUILD_TOOLS/apksigner" sign \
  --ks "$KEYSTORE" \
  --ks-type PKCS12 \
  --ks-key-alias "$NOWBAR_KEY_ALIAS" \
  --ks-pass "pass:$NOWBAR_STORE_PASSWORD" \
  --key-pass "pass:$NOWBAR_KEY_PASSWORD" \
  --out pair/PixelAmbientMusic-1.3.5-paired.apk \
  pair/PixelAmbientMusic-1.3.5-aligned.apk
```

- [ ] **Step 6: Fail closed on signature and package mismatch**

```bash
APK_SIGNER="$ANDROID_SDK_ROOT/build-tools/35.0.0/apksigner"
AAPT="$ANDROID_SDK_ROOT/build-tools/35.0.0/aapt"

"$APK_SIGNER" verify --verbose pair/AmbientMusicMod-OneUI.apk
"$APK_SIGNER" verify --verbose pair/PixelAmbientMusic-1.3.5-paired.apk

AMM_CERT="$($APK_SIGNER verify --print-certs pair/AmbientMusicMod-OneUI.apk \
  | sed -n 's/^Signer #1 certificate SHA-256 digest: //p' | head -n1 | tr '[:upper:]' '[:lower:]')"
PAM_CERT="$($APK_SIGNER verify --print-certs pair/PixelAmbientMusic-1.3.5-paired.apk \
  | sed -n 's/^Signer #1 certificate SHA-256 digest: //p' | head -n1 | tr '[:upper:]' '[:lower:]')"
EXPECTED="$(printf '%s' "$NOWBAR_CERT_SHA256" | tr '[:upper:]' '[:lower:]')"

test -n "$AMM_CERT"
test -n "$PAM_CERT"
test "$AMM_CERT" = "$PAM_CERT"
test "$AMM_CERT" = "$EXPECTED"

AMM_PACKAGE="$($AAPT dump badging pair/AmbientMusicMod-OneUI.apk \
  | sed -n "s/^package: name='\([^']*\)'.*/\1/p" | head -n1)"
PAM_PACKAGE="$($AAPT dump badging pair/PixelAmbientMusic-1.3.5-paired.apk \
  | sed -n "s/^package: name='\([^']*\)'.*/\1/p" | head -n1)"

test "$AMM_PACKAGE" = 'com.kieronquinn.app.ambientmusicmod'
test "$PAM_PACKAGE" = 'com.kieronquinn.app.pixelambientmusic'
```

- [ ] **Step 7: Generate release checksums and upload only public release files**

```bash
cd pair
sha256sum AmbientMusicMod-OneUI.apk PixelAmbientMusic-1.3.5-paired.apk > SHA256SUMS.txt
```

`actions/upload-artifact@v4` must upload exactly those three files. Remove temporary official/aligned PAM copies before upload.

- [ ] **Step 8: Remove prototype workflows only after the new workflows contain equivalent checks**

```bash
git rm .github/workflows/m0-live-update.yml \
       .github/workflows/m1m2-nowbar.yml \
       .github/workflows/nowbar-paired-bundle.yml
```

- [ ] **Step 9: Commit**

```bash
git add .github/workflows
git commit -m "ci: add production One UI validation and paired packaging"
git push oneui HEAD:main
```

---

### Task 6: Configure the stable signer in the new repository without exposing secrets

**Files:**
- Modify if needed: `.gitignore`
- No tracked secret-value file.

**Interfaces:**
- Consumes: private stable PKCS12/recovery material already held by the owner.
- Produces: five GitHub Actions repository secrets.

- [ ] **Step 1: Harden `.gitignore`**

Ensure these entries exist:

```gitignore
*.jks
*.keystore
*.p12
local.properties
*RECOVERY*PRIVATE*
```

- [ ] **Step 2: Confirm no private signing files are tracked**

```bash
if git ls-files | grep -Ei '(\.p12$|\.jks$|\.keystore$|NOWBAR-RECOVERY|local\.properties$)'; then
  echo 'SECRET_FILE_TRACKED=FAIL'
  exit 1
fi
```

- [ ] **Step 3: Set the five secrets from private local values without printing them**

Required names:

```text
NOWBAR_KEYSTORE_B64
NOWBAR_STORE_PASSWORD
NOWBAR_KEY_ALIAS
NOWBAR_KEY_PASSWORD
NOWBAR_CERT_SHA256
```

Use `gh secret set NAME --repo Jorgeprdz/AmbientMusic-OneUI-LiveNotifications` with value supplied through stdin or the interactive hidden-value prompt. Never put secret literals in a committed script.

- [ ] **Step 4: Verify secret names only**

```bash
gh secret list --repo Jorgeprdz/AmbientMusic-OneUI-LiveNotifications
```

Expected: all five names are listed; values remain hidden.

- [ ] **Step 5: Commit `.gitignore` only if changed**

```bash
git add .gitignore
git diff --cached --quiet || git commit -m "chore: harden signing secret exclusions"
git push oneui HEAD:main
```

---

### Task 7: Run CI, paired build, clean-device verification, and preserve evidence

**Files:**
- Modify: `docs/VERIFICATION.md`
- Optional sanitized evidence: `docs/evidence/s25-nowbar-notification.txt`

**Interfaces:**
- Consumes: green CI workflow, signed paired artifact, Samsung Galaxy S25.
- Produces: reproducible CI evidence plus physical recognition/Now Bar evidence.

- [ ] **Step 1: Trigger and watch CI**

```bash
gh workflow run ci.yml --repo Jorgeprdz/AmbientMusic-OneUI-LiveNotifications
sleep 3
CI_RUN="$(gh run list --repo Jorgeprdz/AmbientMusic-OneUI-LiveNotifications --workflow ci.yml --limit 1 --json databaseId --jq '.[0].databaseId')"
gh run watch "$CI_RUN" --repo Jorgeprdz/AmbientMusic-OneUI-LiveNotifications --exit-status
```

Expected: PASS.

- [ ] **Step 2: Trigger and watch stable paired packaging**

```bash
gh workflow run paired-release.yml --repo Jorgeprdz/AmbientMusic-OneUI-LiveNotifications
sleep 3
PAIR_RUN="$(gh run list --repo Jorgeprdz/AmbientMusic-OneUI-LiveNotifications --workflow paired-release.yml --limit 1 --json databaseId --jq '.[0].databaseId')"
gh run watch "$PAIR_RUN" --repo Jorgeprdz/AmbientMusic-OneUI-LiveNotifications --exit-status
```

Expected: PASS.

- [ ] **Step 3: Download and checksum the exact paired artifact**

```bash
rm -rf /tmp/ambient-oneui-pair
mkdir -p /tmp/ambient-oneui-pair
gh run download "$PAIR_RUN" \
  --repo Jorgeprdz/AmbientMusic-OneUI-LiveNotifications \
  --dir /tmp/ambient-oneui-pair
cd /tmp/ambient-oneui-pair
CHECKSUM_FILE="$(find . -name SHA256SUMS.txt -print -quit)"
test -n "$CHECKSUM_FILE"
cd "$(dirname "$CHECKSUM_FILE")"
sha256sum -c SHA256SUMS.txt
```

Expected: both APKs `OK`.

- [ ] **Step 4: Gate ADB operations on the exact physical model**

```bash
SERIAL="$(adb devices | awk 'NR>1 && $2=="device" {print $1; exit}')"
test -n "$SERIAL"
MODEL="$(adb -s "$SERIAL" shell getprop ro.product.model | tr -d '\r')"
CODENAME="$(adb -s "$SERIAL" shell getprop ro.product.device | tr -d '\r')"
printf 'SERIAL=%s\nMODEL=%s\nCODENAME=%s\n' "$SERIAL" "$MODEL" "$CODENAME"
test "$MODEL" = 'SM-S931B'
```

Expected: model gate passes before install/uninstall actions.

- [ ] **Step 5: Preserve AMM data if needed, then install the paired candidate PAM first and AMM second**

Use AMM's native backup before destructive uninstall if current settings/history must be preserved. Verify installation with exact package paths:

```bash
adb -s "$SERIAL" shell pm path com.kieronquinn.app.pixelambientmusic
adb -s "$SERIAL" shell pm path com.kieronquinn.app.ambientmusicmod
```

- [ ] **Step 6: Complete normal upstream AMM/Shizuku setup and run a real recognition**

Do not bypass setup state or Shizuku privilege checks through private preference edits. Play a known track and obtain a real `RecognitionState.Recognised` result.

- [ ] **Step 7: Verify the real Samsung lock-screen Now Bar visually**

Record the song, title/artist visibility, device model, Android version, and One UI version. Do not infer visual rendering from notification state alone.

- [ ] **Step 8: Capture notification evidence and sanitize it before committing**

```bash
mkdir -p docs/evidence
adb -s "$SERIAL" shell dumpsys notification --noredact \
  | grep -i -A40 -B10 -E \
    'now_playing_surface_v1|ambientmusicmod|promoted|FLAG_PROMOTED_ONGOING' \
  > docs/evidence/s25-nowbar-notification.txt
```

Review the file and remove unrelated notification/account data. Mark `Promoted ongoing evidence` as PASS only if the dump explicitly exposes it; otherwise write `NOT EXPOSED BY DUMPSYS`.

- [ ] **Step 9: Record run IDs and physical verdict in `docs/VERIFICATION.md`**

Use this result block with real run IDs substituted as numeric values during execution:

```text
Device: Samsung Galaxy S25
Model: SM-S931B
Android: 16
One UI: 8.x
Recognition: PASS
Now Bar visual: PASS
Notification channel: now_playing_surface_v1
Promoted ongoing evidence: PASS or NOT EXPOSED BY DUMPSYS
CI run: numeric GitHub Actions run ID
Paired build run: numeric GitHub Actions run ID
Stable certificate SHA-256: 321c34014d346541ab46637242d19fd1b18279b88fc2daa602fd47baa81dc815
```

- [ ] **Step 10: Commit sanitized verification evidence**

```bash
git add docs/VERIFICATION.md docs/evidence/s25-nowbar-notification.txt
git commit -m "test: record Galaxy S25 Now Bar verification"
git push oneui HEAD:main
```

---

### Task 8: Publish the verified experimental `v0.1.0` release and audit the repo

**Files:**
- Create: `.github/workflows/release.yml`
- Modify: `README.md`
- Modify: `CHANGELOG.md`

**Interfaces:**
- Consumes: green Task 7 evidence and stable paired packaging logic.
- Produces: prerelease `v0.1.0` with exactly three assets and a final provenance/security verdict.

- [ ] **Step 1: Create tag-gated `.github/workflows/release.yml`**

Trigger on:

```yaml
on:
  push:
    tags:
      - 'v*'
```

Set:

```yaml
permissions:
  contents: write
```

The workflow must rebuild the paired bundle from the tagged commit using the same stable-signing, PAM-hash, signer, package-ID, and checksum checks from `paired-release.yml` before creating a release.

- [ ] **Step 2: Make release creation upload exactly three files**

```text
AmbientMusicMod-OneUI.apk
PixelAmbientMusic-1.3.5-paired.apk
SHA256SUMS.txt
```

Use GitHub CLI or a maintained release action only after all verification commands exit 0. Set the release as prerelease/experimental.

- [ ] **Step 3: Use exact release notes**

```markdown
## Ambient Music for One UI v0.1.0 — Experimental

Initial experimental release of the Samsung One UI Now Bar integration for
Ambient Music Mod using Android public Live Update APIs.

### Physically validated
- Samsung Galaxy S25 (SM-S931B)
- Android 16
- One UI 8.x

### Install
Install both paired APKs from this release. Pixel Ambient Music and Ambient
Music Mod must share the same signer; do not mix these APKs with upstream-
signed builds.

### Upstream
This project is a modified GPLv3 derivative of Kieron Quinn's Ambient Music
Mod and related Now Playing work. See `docs/UPSTREAM.md` for attribution.
```

- [ ] **Step 4: Run the complete pre-tag audit**

```bash
./gradlew :app:testDebugUnitTest --stacktrace
./gradlew :app:assembleDebugAndroidTest --stacktrace
./gradlew :app:compileReleaseKotlin --stacktrace

grep -q "GNU GENERAL PUBLIC LICENSE" LICENSE
grep -qi "Kieron Quinn" README.md
grep -qi "not affiliated" README.md
grep -qi "GPLv3" README.md

if git ls-files | grep -Ei '(\.p12$|\.jks$|\.keystore$|NOWBAR-RECOVERY|local\.properties$)'; then
  echo 'SECRET_FILE_TRACKED=FAIL'
  exit 1
fi

if git grep -nEi '(BEGIN PRIVATE KEY|NOWBAR_STORE_PASSWORD=[^$]|NOWBAR_KEY_PASSWORD=[^$])' -- . ':!docs/superpowers'; then
  echo 'SECRET_LITERAL_SCAN=FAIL'
  exit 1
fi

git status --short
```

Expected: tests compile/pass, attribution checks pass, secret scans find nothing, and working tree is clean.

- [ ] **Step 5: Commit release workflow before tagging**

```bash
git add .github/workflows/release.yml README.md CHANGELOG.md
git commit -m "ci: add verified experimental release workflow"
git push oneui HEAD:main
```

- [ ] **Step 6: Tag the exact audited commit**

```bash
git tag -a v0.1.0 -m "Ambient Music for One UI v0.1.0"
git push oneui v0.1.0
```

- [ ] **Step 7: Watch the tag workflow and verify the published release**

```bash
RELEASE_RUN="$(gh run list --repo Jorgeprdz/AmbientMusic-OneUI-LiveNotifications --workflow release.yml --limit 1 --json databaseId --jq '.[0].databaseId')"
gh run watch "$RELEASE_RUN" --repo Jorgeprdz/AmbientMusic-OneUI-LiveNotifications --exit-status

gh release view v0.1.0 \
  --repo Jorgeprdz/AmbientMusic-OneUI-LiveNotifications \
  --json tagName,isPrerelease,assets
```

Expected: `tagName=v0.1.0`, prerelease true, exactly three assets.

- [ ] **Step 8: Download the public release and verify checksums one final time**

```bash
rm -rf /tmp/ambient-oneui-v0.1.0
mkdir -p /tmp/ambient-oneui-v0.1.0
gh release download v0.1.0 \
  --repo Jorgeprdz/AmbientMusic-OneUI-LiveNotifications \
  --dir /tmp/ambient-oneui-v0.1.0
cd /tmp/ambient-oneui-v0.1.0
sha256sum -c SHA256SUMS.txt
```

Expected: both APKs `OK`.

- [ ] **Step 9: Final completion verdict**

Only after the corresponding evidence exists, report:

```text
REPOSITORY=PASS
GPL_ATTRIBUTION=PASS
CI=PASS
PAM_SOURCE_HASH=PASS
PAIRED_SIGNER=PASS
PACKAGE_IDS=PASS
S25_RECOGNITION=PASS
S25_NOW_BAR=PASS
SECRET_SCAN=PASS
RELEASE_V0_1_0=PASS
```
