# Release and Self-Update Finalization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the first derivative release self-consistent and publicly downloadable by versioning the app as `v0.1.0`, selecting the correct AMM APK from a paired release, and publishing the already verified paired bundle as a GitHub prerelease.

**Architecture:** Keep the existing GitHub-release updater but make AMM asset selection deterministic by filename. Keep PAM update behaviour unchanged. Extend the paired release workflow so validation still gates artifact publication, then create/update the matching GitHub prerelease only after all checks pass.

**Tech Stack:** Kotlin, Gson, JUnit 4, Gradle, GitHub Actions, GitHub CLI, Android build tools.

**Spec:** `docs/superpowers/specs/2026-09-16-upstream-sync-and-update-routing-design.md`

## Global Constraints

- Package ID remains `com.kieronquinn.app.ambientmusicmod`.
- `versionName` and `BuildConfig.TAG_NAME` are exactly `v0.1.0`.
- `versionCode` is exactly `241` for the first derivative release.
- AMM release asset is exactly `AmbientMusicMod-OneUI.apk`.
- PAM update source remains `KieronQuinn/NowPlaying`.
- Public release assets contain no private signing material.
- GitHub Release is created or updated only after all existing paired-bundle validation succeeds.

---

### Task 1: Lock release identity and public app label

**Files:**
- Modify: `app/build.gradle`
- Modify: `app/src/main/res/values/strings.xml`

- [ ] **Step 1: Change derivative release identity**

Set:

```groovy
def tagName = 'v0.1.0'
def version = 241
```

- [ ] **Step 2: Change default app label**

Set:

```xml
<string name="app_name">Ambient Music for One UI</string>
```

Preserve package/application ID and upstream credit strings.

- [ ] **Step 3: Verify source identity**

Run:

```bash
grep -F "def tagName = 'v0.1.0'" app/build.gradle
grep -F "def version = 241" app/build.gradle
grep -F '<string name="app_name">Ambient Music for One UI</string>' app/src/main/res/values/strings.xml
```

Expected: all three commands succeed.

---

### Task 2: Make AMM release asset selection deterministic

**Files:**
- Modify: `app/src/main/java/com/kieronquinn/app/ambientmusicmod/model/update/Release.kt`
- Modify: `app/src/main/java/com/kieronquinn/app/ambientmusicmod/ui/screens/updates/UpdatesViewModel.kt`
- Create: `app/src/test/java/com/kieronquinn/app/ambientmusicmod/model/update/ReleaseTest.kt`

- [ ] **Step 1: Write the failing paired-release asset test**

Use Gson to parse a synthetic GitHub Release containing these two APK assets in the wrong order:

```json
{
  "html_url":"https://github.com/Jorgeprdz/AmbientMusic-OneUI-LiveNotifications/releases/tag/v0.1.0",
  "tag_name":"v0.1.0",
  "name":"v0.1.0 Experimental",
  "body":"test",
  "assets":[
    {
      "browser_download_url":"https://example.invalid/PixelAmbientMusic-1.3.5-paired.apk",
      "content_type":"application/vnd.android.package-archive",
      "name":"PixelAmbientMusic-1.3.5-paired.apk"
    },
    {
      "browser_download_url":"https://example.invalid/AmbientMusicMod-OneUI.apk",
      "content_type":"application/vnd.android.package-archive",
      "name":"AmbientMusicMod-OneUI.apk"
    }
  ]
}
```

Assert:

```kotlin
val release = Gson().fromJson(json, GitHubRelease::class.java)
val mapped = release.toRelease("Ambient Music for One UI", "2.4", "AmbientMusicMod-OneUI.apk")
assertEquals("AmbientMusicMod-OneUI.apk", mapped?.fileName)
```

- [ ] **Step 2: Run focused test and verify RED**

```bash
./gradlew :app:testDebugUnitTest --tests '*ReleaseTest' --stacktrace
```

Expected: FAIL because `toRelease` does not yet accept a preferred filename.

- [ ] **Step 3: Implement preferred APK selection**

Change the mapper signature to:

```kotlin
fun GitHubRelease.toRelease(
    title: String,
    localVersion: String?,
    preferredFileName: String? = null
): Release?
```

Build the APK candidate list using either the Android APK MIME type or a case-insensitive `.apk` filename fallback, then select `preferredFileName` when supplied; otherwise select the first APK candidate.

- [ ] **Step 4: Wire AMM only to the preferred filename**

In `UpdatesViewModelImpl`, set:

```kotlin
internal const val AMM_RELEASE_ASSET = "AmbientMusicMod-OneUI.apk"
```

Call the mapper with that filename from `onAMMUpdateClicked`. Leave `onPAMUpdateClicked` using the default selection behaviour.

- [ ] **Step 5: Run focused and full tests**

```bash
./gradlew :app:testDebugUnitTest --stacktrace
```

Expected: PASS.

---

### Task 3: Publish the verified paired bundle as a GitHub prerelease

**Files:**
- Modify: `.github/workflows/paired-release.yml`

- [ ] **Step 1: Grant release publication permission**

Change workflow permissions to:

```yaml
permissions:
  contents: write
```

- [ ] **Step 2: Derive and validate release tag from source**

Before building, derive `RELEASE_TAG` from `app/build.gradle` and require it to match the expected `v`-prefixed semantic version form:

```bash
RELEASE_TAG="$(sed -n "s/^def tagName = '\([^']*\)'/\1/p" app/build.gradle | head -n1)"
VERSION_CODE="$(sed -n 's/^def version = \([0-9][0-9]*\)/\1/p' app/build.gradle | head -n1)"
test "$RELEASE_TAG" = "v0.1.0"
test "$VERSION_CODE" = "241"
echo "RELEASE_TAG=$RELEASE_TAG" >> "$GITHUB_ENV"
```

- [ ] **Step 3: Keep all existing fail-closed validation unchanged**

Unit tests, instrumentation compilation, AMM release build, pinned PAM download/checksum, APK verification, package checks, matching certificate digest, and `SHA256SUMS.txt` creation all run before release publication.

- [ ] **Step 4: Create/update prerelease after artifact upload**

Use `GH_TOKEN: ${{ github.token }}` and:

```bash
NOTES="$RUNNER_TEMP/release-notes.md"
cat > "$NOTES" <<'EOF'
## Ambient Music for One UI — v0.1.0 Experimental

Experimental Samsung One UI derivative of Ambient Music Mod.

### Highlights
- Recognised songs can surface in Samsung's real Now Bar through Android 16 promoted Live Update notifications.
- Promoted Now Bar publishing is always enabled in this experimental derivative.
- Includes a paired Pixel Ambient Music 1.3.5 APK signed with the same release identity required by the upstream signature-permission architecture.

### Verified device
- Samsung Galaxy S25 (SM-S931B), Android 16 / One UI 8.x.

### Install
Install both APKs from this release together. `SHA256SUMS.txt` contains the public file hashes.

Unofficial community project. Not affiliated with Samsung, Google, or Kieron Quinn.
EOF

if gh release view "$RELEASE_TAG" >/dev/null 2>&1; then
  gh release upload "$RELEASE_TAG" \
    pair/AmbientMusicMod-OneUI.apk \
    pair/PixelAmbientMusic-1.3.5-paired.apk \
    pair/SHA256SUMS.txt \
    --clobber
  gh release edit "$RELEASE_TAG" \
    --title "$RELEASE_TAG Experimental" \
    --notes-file "$NOTES" \
    --prerelease
else
  gh release create "$RELEASE_TAG" \
    pair/AmbientMusicMod-OneUI.apk \
    pair/PixelAmbientMusic-1.3.5-paired.apk \
    pair/SHA256SUMS.txt \
    --target "$GITHUB_SHA" \
    --title "$RELEASE_TAG Experimental" \
    --notes-file "$NOTES" \
    --prerelease
fi
```

- [ ] **Step 5: Verify release assets through GitHub API/CLI**

Require exactly the three expected asset names and fail the workflow if any is absent.

---

### Task 4: Final verification

- [ ] **Step 1: Run CI-equivalent checks**

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebugAndroidTest :app:compileReleaseKotlin --stacktrace
```

Expected: PASS.

- [ ] **Step 2: Run paired release workflow**

Expected: PASS with release publication after the verified artifact upload.

- [ ] **Step 3: Verify public release contract**

Confirm GitHub release `v0.1.0` is a prerelease and exposes exactly:

```text
AmbientMusicMod-OneUI.apk
PixelAmbientMusic-1.3.5-paired.apk
SHA256SUMS.txt
```

- [ ] **Step 4: Verify self-update consistency**

Confirm source contains local tag `v0.1.0`, derivative repository routing, and preferred AMM asset filename so the freshly installed release does not immediately report itself as a different version and cannot select the PAM APK by asset order.
