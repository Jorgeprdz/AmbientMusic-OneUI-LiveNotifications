# Ambient Music for One UI — Live Notifications Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Publish a professional GPLv3 derivative of Ambient Music Mod named **Ambient Music for One UI**, with the validated Samsung Now Bar Live Update integration always enabled, stable paired signing, reproducible CI, documentation, and an experimental `v0.1.0` release.

**Architecture:** Import the validated `feature/nowbar-m1m2` source as the new repository baseline, preserve Ambient Music Mod's recognition architecture, and keep the One UI presentation layer attached only to the central `RecognitionState` flow in `AmbientMusicModForegroundService`. Successful recognitions are mapped to `NowPlayingSurfaceEvent` and published through Android public Live Update APIs; promotion is always requested when capability checks pass, while unsupported devices safely fall back to standard notification behavior.

**Tech Stack:** Android/Kotlin, Gradle, Android 16 / compileSdk 36, AndroidX Core 1.17+, Shizuku where upstream AMM requires it, GitHub Actions, JDK 17, Android SDK build-tools/apksigner, Bash, GitHub CLI, GPLv3.

**Spec:** `docs/superpowers/specs/2026-09-15-ambient-music-oneui-live-notifications-design.md`

## Global Constraints

- Repository name: `AmbientMusic-OneUI-LiveNotifications`.
- Display name: **Ambient Music for One UI**.
- Tagline: *Now Playing recognition in Samsung's Now Bar using Android Live Updates.*
- Project is an unofficial derivative; do not imply affiliation with Samsung, Google, or Kieron Quinn.
- Preserve GPLv3 licensing, upstream notices, and corresponding source availability.
- Keep One UI Live Update behavior always enabled; do not add a user-facing toggle.
- Use public Android notification APIs only; no Samsung private APIs, package spoofing, fake MediaSession behavior, root SystemUI changes, or privileged Samsung framework hooks.
- Initial validated target: Samsung Galaxy S25 `SM-S931B`, Android 16, One UI 8.x.
- Keep `compileSdk 36`, `targetSdk 36`, AndroidX Core 1.17+, and `POST_PROMOTED_NOTIFICATIONS`.
- Preserve the service-level recognition hook beside existing widget / overlay presentation paths.
- Stable paired signer is private; never commit keystores, signing passwords, recovery files, `local.properties`, or exported secrets.
- Release assets are `AmbientMusicMod-OneUI.apk`, `PixelAmbientMusic-1.3.5-paired.apk`, and `SHA256SUMS.txt`.
- First public release is `v0.1.0`, marked **Experimental**.
- CI must fail closed on missing signing material, signer mismatch, package mismatch, test failure, or APK verification failure.

---

## File Structure

The new repository keeps the upstream source tree and adds focused project-level documentation and CI.

**Core implementation already validated and imported unchanged unless verification reveals drift:**

- `app/src/main/java/com/kieronquinn/app/ambientmusicmod/components/nowplayingsurface/AndroidLiveUpdateNowPlayingSurfacePublisher.kt` — creates the channel, builds the public Live Update notification, checks platform capabilities, and publishes/clears the surface.
- `app/src/main/java/com/kieronquinn/app/ambientmusicmod/components/nowplayingsurface/NowPlayingSurfacePolicy.kt` — defines standard vs promoted policy and capability gating.
- `app/src/main/java/com/kieronquinn/app/ambientmusicmod/components/nowplayingsurface/NowPlayingSurfaceLifecycle.kt` — reacts to recognition states.
- `app/src/main/java/com/kieronquinn/app/ambientmusicmod/components/nowplayingsurface/RecognitionNowPlayingSurfaceMapper.kt` — maps AMM recognition results into title/artist/timeout events.
- `app/src/main/java/com/kieronquinn/app/ambientmusicmod/service/AmbientMusicModForegroundService.kt` — owns the central integration hook and always uses `EXPERIMENTAL_PROMOTED` in this derivative.
- `app/src/main/AndroidManifest.xml` — Android 16 permissions including `POST_PROMOTED_NOTIFICATIONS`.

**Tests:**

- `app/src/test/java/com/kieronquinn/app/ambientmusicmod/components/nowplayingsurface/NowPlayingSurfacePolicyTest.kt`
- `app/src/test/java/com/kieronquinn/app/ambientmusicmod/components/nowplayingsurface/NowPlayingSurfaceLifecycleTest.kt`
- `app/src/test/java/com/kieronquinn/app/ambientmusicmod/components/nowplayingsurface/RecognitionNowPlayingSurfaceMapperTest.kt`

**Project presentation / policy files to create or replace:**

- `README.md` — public project landing page, tested compatibility, installation, architecture summary, credits, non-affiliation, release model.
- `LICENSE` — GPLv3 text retained from upstream.
- `CHANGELOG.md` — release history beginning with `0.1.0`.
- `CONTRIBUTING.md` — contribution and verification rules.
- `SECURITY.md` — reporting guidance and signing-material policy.
- `docs/ARCHITECTURE.md` — recognition-to-Now-Bar data flow and explicit non-goals.
- `docs/INSTALLATION.md` — clean migration and paired-install instructions.
- `docs/COMPATIBILITY.md` — tested-vs-expected device matrix.
- `docs/UPSTREAM.md` — upstream lineage, pinned source commits, licensing, attribution.
- `docs/VERIFICATION.md` — repeatable physical/ADB verification procedure.
- `docs/assets/nowbar-s25-dream-on.jpg` — sanitized real-device proof image.

**CI / release files:**

- `.github/workflows/ci.yml` — unsigned validation: unit tests, instrumentation-source compile, release-source compile.
- `.github/workflows/paired-release.yml` — stable signer load, AMM build, pinned PAM download, paired signing, certificate/package verification, checksums, artifact upload.
- `.github/workflows/release.yml` — tag-gated release packaging using the verified paired bundle.

---

### Task 1: Create the public repository from the validated source state

**Files:**
- Source: full tree from `Jorgeprdz/AmbientMusicMod` at `feature/nowbar-m1m2`
- Create repository: `Jorgeprdz/AmbientMusic-OneUI-LiveNotifications`
- Preserve: `.gitignore`, `LICENSE`, Gradle wrapper, source modules, upstream source notices

**Interfaces:**
- Consumes: validated source at commit containing `28c6bf5df1010c4a4a8bf406d00b34b697cc881f` plus later stable-signing workflow commits.
- Produces: a new public repository whose `main` branch contains the validated source and full upstream history or a clearly documented derivative import.

- [ ] **Step 1: Verify the source branch is clean and points at the expected implementation**

Run in an authenticated local clone:

```bash
git fetch origin
git checkout feature/nowbar-m1m2
git status --short --branch
git log -1 --oneline
```

Expected: clean working tree on `feature/nowbar-m1m2`, with the current head containing the validated Now Bar integration and stable-signing changes.

- [ ] **Step 2: Verify the implementation identity before import**

```bash
grep -R "NowPlayingSurfacePolicy.EXPERIMENTAL_PROMOTED" -n \
  app/src/main/java/com/kieronquinn/app/ambientmusicmod/service/AmbientMusicModForegroundService.kt

grep -R "setRequestPromotedOngoing" -n \
  app/src/main/java/com/kieronquinn/app/ambientmusicmod/components/nowplayingsurface

grep -R "POST_PROMOTED_NOTIFICATIONS" -n app/src/main/AndroidManifest.xml
```

Expected: exactly one production service policy reference, the public AndroidX promoted-notification call, and the manifest permission.

- [ ] **Step 3: Create the new public GitHub repository without initializing it with unrelated files**

```bash
gh repo create Jorgeprdz/AmbientMusic-OneUI-LiveNotifications \
  --public \
  --description "Now Playing recognition in Samsung's Now Bar using Android Live Updates." \
  --disable-wiki
```

Expected: repository created successfully and empty.

- [ ] **Step 4: Add the new repository as a remote and publish the validated branch as `main`**

```bash
git remote add oneui git@github.com:Jorgeprdz/AmbientMusic-OneUI-LiveNotifications.git
git push oneui feature/nowbar-m1m2:main
```

Expected: new repository `main` contains the complete validated source tree.

- [ ] **Step 5: Verify the published repository HEAD matches the local source HEAD**

```bash
LOCAL_SHA="$(git rev-parse feature/nowbar-m1m2)"
REMOTE_SHA="$(git ls-remote oneui refs/heads/main | awk '{print $1}')"
printf 'LOCAL=%s\nREMOTE=%s\n' "$LOCAL_SHA" "$REMOTE_SHA"
test "$LOCAL_SHA" = "$REMOTE_SHA"
```

Expected: command exits 0 and both SHAs match.

- [ ] **Step 6: Commit only if repository metadata files were added locally during bootstrap**

If no files changed, do not create an empty commit. If metadata changed:

```bash
git add <changed-metadata-files>
git commit -m "chore: bootstrap Ambient Music for One UI repository"
git push oneui HEAD:main
```

---

### Task 2: Lock the always-on promoted Live Update behavior with regression tests

**Files:**
- Verify/Modify: `app/src/main/java/com/kieronquinn/app/ambientmusicmod/service/AmbientMusicModForegroundService.kt`
- Verify/Modify: `app/src/main/java/com/kieronquinn/app/ambientmusicmod/components/nowplayingsurface/NowPlayingSurfacePolicy.kt`
- Test: `app/src/test/java/com/kieronquinn/app/ambientmusicmod/components/nowplayingsurface/NowPlayingSurfacePolicyTest.kt`
- Test: `app/src/test/java/com/kieronquinn/app/ambientmusicmod/components/nowplayingsurface/NowPlayingSurfaceLifecycleTest.kt`

**Interfaces:**
- Consumes: `NowPlayingSurfacePolicy.EXPERIMENTAL_PROMOTED`, `NowPlayingSurfaceCapabilities`, `NowPlayingSurfaceLifecycle`.
- Produces: a tested invariant that this derivative requests promotion only when all required capabilities are true and that no user setting can disable the derivative-specific integration.

- [ ] **Step 1: Add a regression test that promoted policy rejects incomplete capability sets**

Add to `NowPlayingSurfacePolicyTest.kt` a parameterized or explicit test equivalent to:

```kotlin
@Test
fun `experimental promoted requires every promotion capability`() {
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

- [ ] **Step 2: Run the focused policy test**

```bash
./gradlew :app:testReleaseUnitTest \
  --tests '*NowPlayingSurfacePolicyTest*'
```

Expected: PASS. If the exact Gradle variant task differs, use the existing unit-test task from the working CI workflow and record that exact command in `docs/VERIFICATION.md`.

- [ ] **Step 3: Add a lifecycle regression test for clear-on-failure and preserve-during-transient behavior**

Add tests equivalent to:

```kotlin
@Test
fun `failed recognition clears published surface`() {
    val publisher = RecordingPublisher()
    val lifecycle = NowPlayingSurfaceLifecycle(publisher)

    lifecycle.onRecognitionState(failedRecognitionState())

    assertEquals(1, publisher.clearCount)
}

@Test
fun `recording state does not clear last successful surface`() {
    val publisher = RecordingPublisher()
    val lifecycle = NowPlayingSurfaceLifecycle(publisher)

    lifecycle.onRecognitionState(recordingRecognitionState())

    assertEquals(0, publisher.clearCount)
    assertTrue(publisher.events.isEmpty())
}
```

Reuse the test fixtures/types already present in this test file instead of creating a second parallel fake hierarchy.

- [ ] **Step 4: Run the lifecycle tests**

```bash
./gradlew :app:testReleaseUnitTest \
  --tests '*NowPlayingSurfaceLifecycleTest*'
```

Expected: PASS.

- [ ] **Step 5: Verify the production service has no setting/toggle dependency for Now Bar**

```bash
grep -n -A8 -B4 "nowPlayingSurfacePublisher" \
  app/src/main/java/com/kieronquinn/app/ambientmusicmod/service/AmbientMusicModForegroundService.kt
```

Expected constructor policy is `NowPlayingSurfacePolicy.EXPERIMENTAL_PROMOTED` directly, not derived from a `SettingsRepository` flag.

- [ ] **Step 6: Commit the regression lock**

```bash
git add app/src/test/java/com/kieronquinn/app/ambientmusicmod/components/nowplayingsurface
git commit -m "test: lock One UI promoted Live Update behavior"
git push oneui HEAD:main
```

---

### Task 3: Replace project-facing README and add professional upstream attribution

**Files:**
- Create/Replace: `README.md`
- Create: `docs/UPSTREAM.md`
- Preserve: `LICENSE`

**Interfaces:**
- Consumes: validated device facts, GPLv3 requirements, upstream repositories.
- Produces: public-facing identity and attribution text referenced by installation, compatibility, and release docs.

- [ ] **Step 1: Write `README.md` with the exact project identity**

The first section must contain:

```markdown
# Ambient Music for One UI

> Now Playing recognition in Samsung's Now Bar using Android Live Updates.

**Experimental · Unofficial community project**

Ambient Music for One UI is a GPLv3 derivative of Kieron Quinn's
Ambient Music Mod that publishes successful Now Playing recognitions as
Android Live Updates so supported Samsung One UI devices can present them
in the Now Bar.

This project is not affiliated with or endorsed by Samsung, Google, or
Kieron Quinn.
```

Follow with sections in this order: `Proof on real hardware`, `What it does`, `Tested compatibility`, `Installation`, `How it works`, `Limitations`, `Building`, `Paired signing`, `Credits & Acknowledgements`, `License`.

- [ ] **Step 2: Add exact tested-compatibility wording**

Use:

```markdown
## Tested compatibility

| Device | Model | Android | One UI | Status |
|---|---|---:|---:|---|
| Samsung Galaxy S25 | SM-S931B | 16 | 8.x | Physically validated |

The initial validation recognised **Dream On — Aerosmith** and rendered
the title and artist in Samsung's real lock-screen Now Bar. Devices not
listed here are not claimed as confirmed compatible.
```

- [ ] **Step 3: Add `docs/UPSTREAM.md` with lineage and credits**

Include:

```markdown
# Upstream and attribution

This project is a modified GPLv3 derivative of:

- KieronQuinn/AmbientMusicMod — primary application and recognition integration.
- KieronQuinn/NowPlaying — Pixel Ambient Music / Now Playing component used by Ambient Music Mod.

## One UI derivative work

Jorge Palacios (`Jorgeprdz`) maintains this derivative and contributed the
Android Live Update / Samsung Now Bar integration, paired-signing workflow,
Galaxy S25 physical validation, packaging, and derivative documentation.

The recognition engine and the original Ambient Music Mod application are
upstream work and are not claimed as original work of this derivative.
```

Document upstream commit pins used to create the initial public release.

- [ ] **Step 4: Verify GPLv3 remains present and no attribution was removed**

```bash
grep -q "GNU GENERAL PUBLIC LICENSE" LICENSE
grep -qi "Kieron Quinn" README.md
grep -qi "KieronQuinn/AmbientMusicMod" docs/UPSTREAM.md
grep -qi "GPLv3" README.md
```

Expected: all commands exit 0.

- [ ] **Step 5: Commit**

```bash
git add README.md LICENSE docs/UPSTREAM.md
git commit -m "docs: establish Ambient Music for One UI identity"
git push oneui HEAD:main
```

---

### Task 4: Add installation, compatibility, architecture, security, and contribution documentation

**Files:**
- Create: `docs/INSTALLATION.md`
- Create: `docs/COMPATIBILITY.md`
- Create: `docs/ARCHITECTURE.md`
- Create: `docs/VERIFICATION.md`
- Create: `SECURITY.md`
- Create: `CONTRIBUTING.md`
- Create: `CHANGELOG.md`

**Interfaces:**
- Consumes: release filenames and package IDs.
- Produces: authoritative docs referenced by README and release notes.

- [ ] **Step 1: Write `docs/INSTALLATION.md` with paired-install rules**

Document these package IDs and order:

```text
com.kieronquinn.app.pixelambientmusic
com.kieronquinn.app.ambientmusicmod
```

Install PAM first, then AMM. State that an upstream-signed installation may need to be uninstalled because the derivative pair uses its own stable signer. State that app data may be lost on uninstall and users should back up AMM first.

- [ ] **Step 2: Write `docs/COMPATIBILITY.md` with a strict tested/unknown distinction**

Start with:

```markdown
# Compatibility

## Physically validated

- Samsung Galaxy S25 (`SM-S931B`)
- Android 16
- One UI 8.x

## Not yet validated

Other One UI devices may expose the same Android Live Update capability,
but they are not considered supported until a physical recognition has
been observed in the real Samsung Now Bar.
```

- [ ] **Step 3: Write `docs/ARCHITECTURE.md` with the exact data flow**

Include:

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

Explicitly document that Samsung—not the app—decides whether a valid promoted notification is rendered in Now Bar.

- [ ] **Step 4: Write `docs/VERIFICATION.md` with an ADB physical test**

Use package-aware commands that avoid substring/pipefail false positives:

```bash
SERIAL="$(adb devices | awk 'NR>1 && $2=="device" {print $1; exit}')"
adb -s "$SERIAL" shell getprop ro.product.model
adb -s "$SERIAL" shell pm path com.kieronquinn.app.ambientmusicmod
adb -s "$SERIAL" shell pm path com.kieronquinn.app.pixelambientmusic
adb -s "$SERIAL" shell dumpsys notification --noredact > nowbar-notification.txt
```

Then instruct the tester to confirm a successful recognition visually in the real lock-screen Now Bar and inspect the dump for `now_playing_surface_v1` plus promoted-ongoing evidence when exposed by the build.

- [ ] **Step 5: Write `SECURITY.md` with signing-material exclusions**

State that reports involving signing leakage, release tampering, or privilege boundaries should be reported privately through GitHub's security-reporting mechanism when enabled. Explicitly list the keystore, passwords, recovery text, and local properties as never-to-commit material.

- [ ] **Step 6: Write `CONTRIBUTING.md`**

Require a clean build, unit tests, no private Samsung APIs, no signer material, and evidence for newly claimed device compatibility.

- [ ] **Step 7: Write `CHANGELOG.md`**

Start with:

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

- [ ] **Step 8: Verify docs contain no placeholders**

```bash
! grep -RniE '\b(TODO|TBD|FIXME)\b' \
  README.md CHANGELOG.md CONTRIBUTING.md SECURITY.md docs
```

Expected: exit 0.

- [ ] **Step 9: Commit**

```bash
git add README.md CHANGELOG.md CONTRIBUTING.md SECURITY.md docs
git commit -m "docs: add installation architecture and project policy"
git push oneui HEAD:main
```

---

### Task 5: Add sanitized real-device proof media

**Files:**
- Create: `docs/assets/nowbar-s25-dream-on.jpg`
- Modify: `README.md`

**Interfaces:**
- Consumes: real Galaxy S25 screenshot from physical validation.
- Produces: a privacy-reviewed project proof image referenced by README.

- [ ] **Step 1: Review the candidate screenshot for personal information**

Confirm the crop contains the lock-screen Now Bar result and no notifications, account identifiers, phone numbers, email addresses, location details, or unrelated private content.

- [ ] **Step 2: Crop only if necessary**

Keep enough Samsung lock-screen context to demonstrate that the result is in the actual Now Bar rather than an in-app mockup.

- [ ] **Step 3: Add the image to the repository**

```bash
mkdir -p docs/assets
cp <sanitized-screenshot> docs/assets/nowbar-s25-dream-on.jpg
```

- [ ] **Step 4: Reference it from README**

Add directly below the introduction:

```markdown
![Ambient Music for One UI showing Dream On by Aerosmith in Samsung Now Bar](docs/assets/nowbar-s25-dream-on.jpg)
```

- [ ] **Step 5: Commit**

```bash
git add README.md docs/assets/nowbar-s25-dream-on.jpg
git commit -m "docs: add Galaxy S25 Now Bar validation image"
git push oneui HEAD:main
```

---

### Task 6: Replace prototype workflows with production CI and paired-release workflows

**Files:**
- Remove from public baseline after equivalent checks are preserved: `.github/workflows/m0-live-update.yml`
- Replace/Consolidate: `.github/workflows/m1m2-nowbar.yml`
- Replace/Consolidate: `.github/workflows/nowbar-paired-bundle.yml`
- Create: `.github/workflows/ci.yml`
- Create: `.github/workflows/paired-release.yml`

**Interfaces:**
- Consumes: Gradle project, stable signing secrets, pinned PAM 1.3.5 source artifact, expected certificate SHA-256.
- Produces: unsigned PR/push CI plus manually runnable signed paired bundle.

- [ ] **Step 1: Create `.github/workflows/ci.yml` without any signing secrets**

Required jobs/steps:

```yaml
name: CI

on:
  push:
    branches: [main]
  pull_request:
  workflow_dispatch:

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'
      - uses: gradle/actions/setup-gradle@v4
      - name: Unit tests
        run: ./gradlew :app:testReleaseUnitTest
      - name: Compile instrumentation tests
        run: ./gradlew :app:compileReleaseAndroidTestKotlin
      - name: Compile release sources
        run: ./gradlew :app:assembleRelease
```

If upstream release signing configuration requires local signing properties even for `assembleRelease`, use the validated non-secret compile task from the existing `m1m2-nowbar.yml` instead of injecting production secrets into CI.

- [ ] **Step 2: Create `.github/workflows/paired-release.yml` by hardening the validated stable workflow**

Keep the existing known-good stable-signing logic, but rename artifacts to:

```text
AmbientMusicMod-OneUI.apk
PixelAmbientMusic-1.3.5-paired.apk
SHA256SUMS.txt
```

Require these secrets by explicit non-empty checks before decoding anything:

```text
NOWBAR_KEYSTORE_B64
NOWBAR_STORE_PASSWORD
NOWBAR_KEY_ALIAS
NOWBAR_KEY_PASSWORD
NOWBAR_CERT_SHA256
```

- [ ] **Step 3: Make signer verification fail closed**

After signing both APKs:

```bash
apksigner verify --verbose AmbientMusicMod-OneUI.apk
apksigner verify --verbose PixelAmbientMusic-1.3.5-paired.apk

AMM_CERT="$(apksigner verify --print-certs AmbientMusicMod-OneUI.apk \
  | sed -n 's/^Signer #1 certificate SHA-256 digest: //p' | tr '[:upper:]' '[:lower:]')"
PAM_CERT="$(apksigner verify --print-certs PixelAmbientMusic-1.3.5-paired.apk \
  | sed -n 's/^Signer #1 certificate SHA-256 digest: //p' | tr '[:upper:]' '[:lower:]')"
EXPECTED="$(printf '%s' "$NOWBAR_CERT_SHA256" | tr '[:upper:]' '[:lower:]')"

test -n "$AMM_CERT"
test "$AMM_CERT" = "$PAM_CERT"
test "$AMM_CERT" = "$EXPECTED"
```

- [ ] **Step 4: Verify package IDs from APK metadata**

Use `aapt2 dump badging` or `apkanalyzer manifest application-id` and assert:

```text
AMM: com.kieronquinn.app.ambientmusicmod
PAM: com.kieronquinn.app.pixelambientmusic
```

- [ ] **Step 5: Generate checksums**

```bash
sha256sum \
  AmbientMusicMod-OneUI.apk \
  PixelAmbientMusic-1.3.5-paired.apk \
  > SHA256SUMS.txt
```

- [ ] **Step 6: Upload exactly the release bundle**

Use `actions/upload-artifact@v4` with the three release files only. Do not upload decoded keystore files, `local.properties`, or temporary secret material.

- [ ] **Step 7: Remove prototype-only workflow exposure after parity is verified**

Delete `m0-live-update.yml` from the new public repository. Retire `m1m2-nowbar.yml` and `nowbar-paired-bundle.yml` only after `ci.yml` and `paired-release.yml` reproduce their required validation/signing checks.

- [ ] **Step 8: Commit**

```bash
git add .github/workflows
git commit -m "ci: add production validation and paired release workflows"
git push oneui HEAD:main
```

---

### Task 7: Configure signing secrets in the new repository without exposing recovery material

**Files:**
- No tracked files containing secret values.
- Verify: `.gitignore`

**Interfaces:**
- Consumes: existing private stable PKCS12 backup and recovery data from the owner's secure storage.
- Produces: GitHub Actions repository secrets only.

- [ ] **Step 1: Confirm secret files are ignored before touching them**

Ensure `.gitignore` contains patterns equivalent to:

```gitignore
*.jks
*.keystore
*.p12
local.properties
*RECOVERY*PRIVATE*
```

- [ ] **Step 2: Scan the repository history/tree for accidental secret file names**

```bash
git ls-files | grep -Ei '(\.p12$|\.jks$|\.keystore$|recovery|local\.properties$)' && exit 1 || true
```

Expected: no tracked private signing files.

- [ ] **Step 3: Configure the five required secrets using GitHub CLI**

Use the existing private values locally. Never echo passwords to terminal output or commit them. Configure:

```text
NOWBAR_KEYSTORE_B64
NOWBAR_STORE_PASSWORD
NOWBAR_KEY_ALIAS
NOWBAR_KEY_PASSWORD
NOWBAR_CERT_SHA256
```

Use `gh secret set ...` with stdin or a secure local environment variable; do not place secret literals in shell history.

- [ ] **Step 4: Verify only secret names, not values**

```bash
gh secret list --repo Jorgeprdz/AmbientMusic-OneUI-LiveNotifications
```

Expected: all five required names exist.

- [ ] **Step 5: Commit only `.gitignore` if it changed**

```bash
git add .gitignore
git commit -m "chore: harden signing secret exclusions" || true
git push oneui HEAD:main
```

---

### Task 8: Run CI and paired-build verification in the new repository

**Files:**
- No source change required unless verification finds a specific defect.

**Interfaces:**
- Consumes: Task 6 workflows and Task 7 secrets.
- Produces: green CI and a signer-verified paired artifact bundle.

- [ ] **Step 1: Run the normal CI workflow**

```bash
gh workflow run ci.yml \
  --repo Jorgeprdz/AmbientMusic-OneUI-LiveNotifications
```

- [ ] **Step 2: Watch CI to completion**

```bash
gh run watch \
  --repo Jorgeprdz/AmbientMusic-OneUI-LiveNotifications \
  --exit-status
```

Expected: unit tests, Android-test source compile, and release-source compile all pass.

- [ ] **Step 3: Trigger the signed paired-release workflow**

```bash
gh workflow run paired-release.yml \
  --repo Jorgeprdz/AmbientMusic-OneUI-LiveNotifications
```

- [ ] **Step 4: Watch the paired workflow to completion**

```bash
gh run watch \
  --repo Jorgeprdz/AmbientMusic-OneUI-LiveNotifications \
  --exit-status
```

Expected: build, signing, certificate verification, package verification, checksum generation, and artifact upload all pass.

- [ ] **Step 5: Download and verify the artifact locally**

```bash
mkdir -p /tmp/ambient-oneui-release
gh run download \
  --repo Jorgeprdz/AmbientMusic-OneUI-LiveNotifications \
  --dir /tmp/ambient-oneui-release

cd /tmp/ambient-oneui-release
sha256sum -c SHA256SUMS.txt
```

Expected: both APKs report `OK`.

- [ ] **Step 6: Record the green run IDs and certificate digest in `docs/VERIFICATION.md`**

Do not record secret values. Recording the public certificate SHA-256 digest is acceptable.

- [ ] **Step 7: Commit the verification record**

```bash
git add docs/VERIFICATION.md
git commit -m "docs: record reproducible release verification"
git push oneui HEAD:main
```

---

### Task 9: Perform a clean physical install and capture promoted-notification evidence

**Files:**
- Modify: `docs/VERIFICATION.md`
- Optional non-sensitive evidence: `docs/evidence/s25-nowbar-notification.txt`

**Interfaces:**
- Consumes: exact APKs produced by Task 8.
- Produces: device/package/recognition/Now-Bar evidence tied to the release candidate.

- [ ] **Step 1: Verify the connected device is the intended S25 before any package operation**

```bash
SERIAL="$(adb devices | awk 'NR>1 && $2=="device" {print $1; exit}')"
MODEL="$(adb -s "$SERIAL" shell getprop ro.product.model | tr -d '\r')"
DEVICE="$(adb -s "$SERIAL" shell getprop ro.product.device | tr -d '\r')"
printf 'SERIAL=%s\nMODEL=%s\nDEVICE=%s\n' "$SERIAL" "$MODEL" "$DEVICE"
test "$MODEL" = "SM-S931B"
```

Expected: model gate passes before any uninstall/install step.

- [ ] **Step 2: Back up AMM data using its native backup flow if preserving current settings/history matters**

Do not proceed with destructive uninstall unless a valid backup exists or the owner explicitly accepts resetting the app.

- [ ] **Step 3: Install the exact paired release candidate**

For a clean signature test, uninstall conflicting upstream/previously signed AMM and PAM packages, then install PAM first and AMM second. Verify each with `adb shell pm path <exact-package>` rather than substring matching.

- [ ] **Step 4: Complete upstream-required AMM setup / Shizuku authorization**

Use the normal AMM setup flow; do not bypass its privilege checks with unsupported shell edits.

- [ ] **Step 5: Trigger a real recognition**

Use AMM's recognition UI or verified exported recognition path. Play a known song near the device and wait for a successful recognition.

- [ ] **Step 6: Verify Samsung Now Bar visually**

Confirm title and artist appear in the actual lock-screen Now Bar after recognition. Record the exact song used and OS/build context.

- [ ] **Step 7: Capture notification evidence**

```bash
adb -s "$SERIAL" shell dumpsys notification --noredact \
  | grep -i -A40 -B10 -E \
    'now_playing_surface_v1|ambientmusicmod|promoted|FLAG_PROMOTED_ONGOING' \
  > s25-nowbar-notification.txt
```

Review the file before committing; remove unrelated notification data if present.

- [ ] **Step 8: Update `docs/VERIFICATION.md` with the physical result**

Record:

```text
Device: Samsung Galaxy S25
Model: SM-S931B
Android: 16
One UI: 8.x
Recognition: PASS
Now Bar visual: PASS
Notification channel: now_playing_surface_v1
Promoted ongoing evidence: PASS / NOT EXPOSED BY DUMPSYS
```

Do not mark promoted evidence PASS unless the dump actually contains it.

- [ ] **Step 9: Commit sanitized evidence**

```bash
git add docs/VERIFICATION.md docs/evidence/s25-nowbar-notification.txt
git commit -m "test: record Galaxy S25 Now Bar verification"
git push oneui HEAD:main
```

---

### Task 10: Add tag-gated release publication and prepare `v0.1.0`

**Files:**
- Create: `.github/workflows/release.yml`
- Modify: `CHANGELOG.md`
- Modify: `README.md` if release links/status need finalization

**Interfaces:**
- Consumes: signer-verified paired bundle from `paired-release.yml` and green physical verification.
- Produces: GitHub release `v0.1.0` with exact release assets and source at the same tag.

- [ ] **Step 1: Create `.github/workflows/release.yml`**

Trigger only on tags matching `v*` and/or manual dispatch with a required tag input. The workflow must rebuild or retrieve a bundle from the same commit, rerun certificate/package/checksum verification, then create a GitHub release containing only:

```text
AmbientMusicMod-OneUI.apk
PixelAmbientMusic-1.3.5-paired.apk
SHA256SUMS.txt
```

- [ ] **Step 2: Make the workflow reject unverified artifacts**

Before release creation, run:

```bash
sha256sum -c SHA256SUMS.txt
apksigner verify --verbose AmbientMusicMod-OneUI.apk
apksigner verify --verbose PixelAmbientMusic-1.3.5-paired.apk
```

and repeat the exact certificate-equality checks from `paired-release.yml`.

- [ ] **Step 3: Add release notes template text**

Release notes must include:

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

- [ ] **Step 4: Run the complete pre-release verification suite**

```bash
./gradlew :app:testReleaseUnitTest
./gradlew :app:compileReleaseAndroidTestKotlin
git status --short
! git grep -nEi '(storePassword|keyPassword|BEGIN PRIVATE KEY|NOWBAR_STORE_PASSWORD=)' -- . ':!docs/superpowers'
```

Expected: tests PASS, working tree clean after intended commits, and no secret literals found.

- [ ] **Step 5: Commit the release workflow**

```bash
git add .github/workflows/release.yml README.md CHANGELOG.md
git commit -m "ci: add verified experimental release workflow"
git push oneui HEAD:main
```

- [ ] **Step 6: Tag the exact verified commit**

```bash
git tag -a v0.1.0 -m "Ambient Music for One UI v0.1.0"
git push oneui v0.1.0
```

- [ ] **Step 7: Verify the release after workflow completion**

```bash
gh release view v0.1.0 \
  --repo Jorgeprdz/AmbientMusic-OneUI-LiveNotifications
```

Expected: release is marked Experimental/pre-release as configured, source tag is correct, and exactly the three intended assets are present.

---

### Task 11: Final repository quality and provenance audit

**Files:**
- All tracked files

**Interfaces:**
- Consumes: completed repository and public release.
- Produces: final publication verdict with no hidden secret/provenance/documentation gaps.

- [ ] **Step 1: Verify required top-level documentation exists**

```bash
for f in \
  README.md LICENSE CHANGELOG.md CONTRIBUTING.md SECURITY.md \
  docs/ARCHITECTURE.md docs/INSTALLATION.md docs/COMPATIBILITY.md \
  docs/UPSTREAM.md docs/VERIFICATION.md; do
  test -s "$f" || { echo "MISSING=$f"; exit 1; }
done
```

- [ ] **Step 2: Scan for placeholders and private signing artifacts**

```bash
! grep -RniE '\b(TODO|TBD|FIXME)\b' README.md CHANGELOG.md CONTRIBUTING.md SECURITY.md docs
! git ls-files | grep -Ei '(\.p12$|\.jks$|\.keystore$|NOWBAR-RECOVERY|local\.properties$)'
```

- [ ] **Step 3: Verify public attribution and non-affiliation**

```bash
grep -qi 'Kieron Quinn' README.md
grep -qi 'not affiliated' README.md
grep -qi 'GPLv3' README.md
grep -qi 'Jorgeprdz' docs/UPSTREAM.md
```

- [ ] **Step 4: Verify the implementation still uses public promoted notification APIs and no Samsung private API dependency was introduced**

```bash
grep -R "setRequestPromotedOngoing" -n app/src/main
grep -R "canPostPromotedNotifications" -n app/src/main
! grep -RniE 'com\.samsung\.android\..*(systemui|nowbar)|Sem.*NowBar' app/src/main
```

Review any Samsung namespace match manually rather than deleting legitimate unrelated upstream code blindly.

- [ ] **Step 5: Verify the release assets against published checksums one final time**

```bash
gh release download v0.1.0 \
  --repo Jorgeprdz/AmbientMusic-OneUI-LiveNotifications \
  --dir /tmp/ambient-oneui-v0.1.0
cd /tmp/ambient-oneui-v0.1.0
sha256sum -c SHA256SUMS.txt
```

Expected: both APKs report `OK`.

- [ ] **Step 6: Record the final verdict**

A successful completion report must include:

```text
REPOSITORY=PASS
GPL_ATTRIBUTION=PASS
CI=PASS
PAIRED_SIGNER=PASS
PACKAGE_IDS=PASS
S25_RECOGNITION=PASS
S25_NOW_BAR=PASS
SECRET_SCAN=PASS
RELEASE_V0_1_0=PASS
```

Do not report any line as PASS without its corresponding evidence from the preceding tasks.
