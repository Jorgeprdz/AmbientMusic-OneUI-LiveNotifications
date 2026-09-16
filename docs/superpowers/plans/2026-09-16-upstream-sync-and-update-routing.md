# Upstream Sync and Update Routing Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Automatically merge compatible `KieronQuinn/AmbientMusicMod` upstream changes while routing this app's self-updates and GitHub link to `Jorgeprdz/AmbientMusic-OneUI-LiveNotifications` without changing PAM's upstream source.

**Architecture:** Keep AMM and PAM update providers independent by parameterizing `GitHubProvider` with owner/repository. Add a fail-closed scheduled GitHub Action that merges upstream locally, validates the candidate before pushing `main`, and opens/updates one tracking issue when compatibility validation fails.

**Tech Stack:** Kotlin, Retrofit, JUnit, Gradle, GitHub Actions, Bash, GitHub CLI.

**Spec:** `docs/superpowers/specs/2026-09-16-upstream-sync-and-update-routing-design.md`

## Global Constraints

- Application upstream is exactly `KieronQuinn/AmbientMusicMod` branch `main`.
- AMM/self updates use `Jorgeprdz/AmbientMusic-OneUI-LiveNotifications`.
- PAM updates remain on `KieronQuinn/NowPlaying`.
- **About → GitHub** opens `https://github.com/Jorgeprdz/AmbientMusic-OneUI-LiveNotifications`.
- Upstream sync auto-merges only after unit tests, instrumentation-test compilation, and release compilation pass.
- Upstream sync never reads or modifies private release-signing secrets.
- No force-push to `main`.
- Existing Now Bar behaviour remains unchanged.

---

### Task 1: Parameterize GitHub release providers

**Files:**
- Modify: `app/src/main/java/com/kieronquinn/app/ambientmusicmod/providers/GitHubProvider.kt`
- Create: `app/src/test/java/com/kieronquinn/app/ambientmusicmod/providers/GitHubProviderTest.kt`

**Interfaces:**
- Produces: `GitHubProvider.getGitHubProvider(owner: String, repository: String): GitHubProvider`
- Produces: `GitHubProvider.getGitHubBaseUrl(owner: String, repository: String): String`

- [ ] **Step 1: Write the failing URL-construction unit test**

```kotlin
package com.kieronquinn.app.ambientmusicmod.providers

import org.junit.Assert.assertEquals
import org.junit.Test

class GitHubProviderTest {

    @Test
    fun `AMM derivative base URL uses Jorgeprdz repo`() {
        assertEquals(
            "https://api.github.com/repos/Jorgeprdz/AmbientMusic-OneUI-LiveNotifications/",
            GitHubProvider.getGitHubBaseUrl(
                "Jorgeprdz",
                "AmbientMusic-OneUI-LiveNotifications"
            )
        )
    }

    @Test
    fun `PAM base URL remains KieronQuinn NowPlaying`() {
        assertEquals(
            "https://api.github.com/repos/KieronQuinn/NowPlaying/",
            GitHubProvider.getGitHubBaseUrl("KieronQuinn", "NowPlaying")
        )
    }
}
```

- [ ] **Step 2: Run the focused test and verify RED**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests '*GitHubProviderTest' --stacktrace
```

Expected: FAIL because `getGitHubBaseUrl(owner, repository)` does not exist.

- [ ] **Step 3: Implement the minimal provider factory**

Replace the hardcoded owner in `GitHubProvider.kt` with:

```kotlin
companion object {
    internal fun getGitHubBaseUrl(owner: String, repository: String): String {
        return "https://api.github.com/repos/$owner/$repository/"
    }

    fun getGitHubProvider(owner: String, repository: String): GitHubProvider =
        Retrofit.Builder()
            .baseUrl(getGitHubBaseUrl(owner, repository))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GitHubProvider::class.java)
}
```

- [ ] **Step 4: Run the focused test and verify GREEN**

Run the command from Step 2.

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/kieronquinn/app/ambientmusicmod/providers/GitHubProvider.kt \
        app/src/test/java/com/kieronquinn/app/ambientmusicmod/providers/GitHubProviderTest.kt
git commit -m "test: parameterize GitHub release providers"
```

---

### Task 2: Route AMM, PAM, and About links to the correct repositories

**Files:**
- Modify: `app/src/main/java/com/kieronquinn/app/ambientmusicmod/repositories/UpdatesRepository.kt`
- Modify: `app/src/main/java/com/kieronquinn/app/ambientmusicmod/ui/screens/updates/UpdatesViewModel.kt`
- Create: `app/src/test/java/com/kieronquinn/app/ambientmusicmod/repositories/UpdateSourceConfigurationTest.kt`
- Create: `app/src/test/java/com/kieronquinn/app/ambientmusicmod/ui/screens/updates/UpdatesLinkConfigurationTest.kt`

**Interfaces:**
- Consumes: `GitHubProvider.getGitHubProvider(owner, repository)` from Task 1.
- Produces configuration constants that keep AMM/PAM routing independently testable.

- [ ] **Step 1: Write failing source-routing tests**

Expose these `internal const val` values from `UpdatesRepositoryImpl`'s companion object:

```kotlin
AMM_GITHUB_OWNER
AMM_GITHUB_REPOSITORY
PAM_GITHUB_OWNER
PAM_GITHUB_REPOSITORY
```

Write:

```kotlin
package com.kieronquinn.app.ambientmusicmod.repositories

import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateSourceConfigurationTest {

    @Test
    fun `AMM self update source is derivative repository`() {
        assertEquals("Jorgeprdz", UpdatesRepositoryImpl.AMM_GITHUB_OWNER)
        assertEquals(
            "AmbientMusic-OneUI-LiveNotifications",
            UpdatesRepositoryImpl.AMM_GITHUB_REPOSITORY
        )
    }

    @Test
    fun `PAM update source remains upstream NowPlaying`() {
        assertEquals("KieronQuinn", UpdatesRepositoryImpl.PAM_GITHUB_OWNER)
        assertEquals("NowPlaying", UpdatesRepositoryImpl.PAM_GITHUB_REPOSITORY)
    }
}
```

- [ ] **Step 2: Write failing About-link test**

Make the GitHub URL an `internal const val LINK_GITHUB` in `UpdatesViewModelImpl`'s companion object and write:

```kotlin
package com.kieronquinn.app.ambientmusicmod.ui.screens.updates

import org.junit.Assert.assertEquals
import org.junit.Test

class UpdatesLinkConfigurationTest {

    @Test
    fun `About GitHub opens derivative repository`() {
        assertEquals(
            "https://github.com/Jorgeprdz/AmbientMusic-OneUI-LiveNotifications",
            UpdatesViewModelImpl.LINK_GITHUB
        )
    }
}
```

- [ ] **Step 3: Run both tests and verify RED**

```bash
./gradlew :app:testDebugUnitTest \
  --tests '*UpdateSourceConfigurationTest' \
  --tests '*UpdatesLinkConfigurationTest' \
  --stacktrace
```

Expected: FAIL because the new constants are absent and the current GitHub link still uses the upstream redirect.

- [ ] **Step 4: Implement explicit AMM/PAM providers**

In `UpdatesRepositoryImpl` use:

```kotlin
companion object {
    private val CACHE_TIMEOUT = Duration.ofHours(12).toMillis()
    private const val MIN_SUMMARY_AND_EDIT_CODE = 120L

    internal const val AMM_GITHUB_OWNER = "Jorgeprdz"
    internal const val AMM_GITHUB_REPOSITORY = "AmbientMusic-OneUI-LiveNotifications"
    internal const val PAM_GITHUB_OWNER = "KieronQuinn"
    internal const val PAM_GITHUB_REPOSITORY = "NowPlaying"
}

private val pamProvider = GitHubProvider.getGitHubProvider(
    PAM_GITHUB_OWNER,
    PAM_GITHUB_REPOSITORY
)
private val ammProvider = GitHubProvider.getGitHubProvider(
    AMM_GITHUB_OWNER,
    AMM_GITHUB_REPOSITORY
)
```

Keep cache keys `pam` and `amm` unchanged.

- [ ] **Step 5: Point About → GitHub to this repository**

Replace the redirect constant with:

```kotlin
internal const val LINK_GITHUB =
    "https://github.com/Jorgeprdz/AmbientMusic-OneUI-LiveNotifications"
```

Do not modify donate/Twitter/XDA links in this task.

- [ ] **Step 6: Run focused tests and full unit suite**

```bash
./gradlew :app:testDebugUnitTest --stacktrace
```

Expected: PASS, including existing Now Bar unit tests.

- [ ] **Step 7: Compile instrumentation and release sources**

```bash
./gradlew :app:assembleDebugAndroidTest :app:compileReleaseKotlin --stacktrace
```

Expected: PASS.

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/kieronquinn/app/ambientmusicmod/repositories/UpdatesRepository.kt \
        app/src/main/java/com/kieronquinn/app/ambientmusicmod/ui/screens/updates/UpdatesViewModel.kt \
        app/src/test/java/com/kieronquinn/app/ambientmusicmod/repositories/UpdateSourceConfigurationTest.kt \
        app/src/test/java/com/kieronquinn/app/ambientmusicmod/ui/screens/updates/UpdatesLinkConfigurationTest.kt
git commit -m "feat: route updates to One UI derivative"
```

---

### Task 3: Add fail-closed upstream synchronization Action

**Files:**
- Create: `.github/workflows/upstream-sync.yml`
- Create: `docs/UPSTREAM.md`
- Modify: `README.md`

**Interfaces:**
- Consumes: upstream `KieronQuinn/AmbientMusicMod@main`.
- Produces: daily/manual synchronization that changes `main` only after validation succeeds.

- [ ] **Step 1: Create the workflow triggers, permissions, and concurrency**

Use:

```yaml
name: Upstream Sync

on:
  schedule:
    - cron: '17 8 * * *'
  workflow_dispatch:

permissions:
  contents: write
  issues: write

concurrency:
  group: upstream-sync
  cancel-in-progress: false
```

- [ ] **Step 2: Checkout full history and fetch the exact upstream**

The workflow must use `actions/checkout@v4` with `fetch-depth: 0`, configure an automation identity, add:

```bash
git remote add upstream https://github.com/KieronQuinn/AmbientMusicMod.git
git fetch --no-tags upstream main
UPSTREAM_SHA="$(git rev-parse upstream/main)"
```

Use `git merge-base --is-ancestor upstream/main HEAD` for the no-update decision.

- [ ] **Step 3: Create the candidate merge without modifying remote main**

For pending changes:

```bash
SHORT_SHA="${UPSTREAM_SHA:0:12}"
SYNC_BRANCH="automation/upstream-sync-$SHORT_SHA"
git switch -c "$SYNC_BRANCH"
```

Attempt:

```bash
git merge --no-ff --no-commit upstream/main
```

On success commit:

```bash
git commit -m "Merge upstream KieronQuinn/AmbientMusicMod@$SHORT_SHA"
```

On conflict, collect `git diff --name-only --diff-filter=U`, abort the merge, record a blocked state, and do not push `main`.

- [ ] **Step 4: Reuse CI-equivalent ephemeral signing and validation**

Generate an ephemeral PKCS12 key exactly for compilation and create `local.properties`; do not access repository signing secrets.

Run:

```bash
./gradlew :app:testDebugUnitTest --stacktrace
./gradlew :app:assembleDebugAndroidTest --stacktrace
./gradlew :app:compileReleaseKotlin --stacktrace
```

Capture failures as a blocked state instead of allowing the workflow to push `main`.

- [ ] **Step 5: Push only a validated candidate to main**

Immediately before push, fetch `origin/main` and verify the original base has not moved. Then use a normal non-force push:

```bash
git push origin HEAD:main
```

If the push is rejected, classify the run as blocked/raced and leave `main` untouched.

- [ ] **Step 6: Upsert one tracking issue on blocked sync**

Use the authenticated GitHub CLI with `GH_TOKEN: ${{ github.token }}`. Search for an open issue with exact title:

`Upstream sync blocked`

If one exists, replace its body with current upstream SHA, reason, conflicting files or failed stage, workflow URL, and candidate branch if one was pushed for investigation. Otherwise create it.

Do not create duplicate issues for repeated runs.

- [ ] **Step 7: Close the tracking issue after resolution**

After a successful merge, or a no-update state proving the pending upstream is already contained, close an open `Upstream sync blocked` issue with a short resolution comment.

- [ ] **Step 8: Document the mechanism**

Create `docs/UPSTREAM.md` describing:

- upstream `KieronQuinn/AmbientMusicMod@main`;
- daily/manual runs;
- validation gate;
- fail-closed semantics;
- issue behaviour;
- no private signing secrets;
- derivative-specific Now Bar integration.

Add a concise README paragraph linking to `docs/UPSTREAM.md`.

- [ ] **Step 9: Commit**

```bash
git add .github/workflows/upstream-sync.yml docs/UPSTREAM.md README.md
git commit -m "ci: sync compatible Ambient Music Mod upstream updates"
```

---

### Task 4: End-to-end verification

**Files:**
- Verify only; modify only if a test exposes a defect in Tasks 1–3.

- [ ] **Step 1: Run complete local/CI-equivalent verification**

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebugAndroidTest :app:compileReleaseKotlin --stacktrace
```

Expected: PASS.

- [ ] **Step 2: Push and require repository CI to pass**

Confirm `.github/workflows/ci.yml` reports success on the final implementation commit.

- [ ] **Step 3: Dispatch Upstream Sync manually**

Run the new workflow with `workflow_dispatch`.

Expected outcomes are both acceptable depending on upstream state:

- `NO_UPDATES` with no repository change; or
- a validated upstream merge into `main` followed by normal CI.

A conflict/regression is acceptable only if the Action leaves `main` unchanged and creates/updates `Upstream sync blocked`.

- [ ] **Step 4: Verify updater routing in source and release prerequisites**

Confirm the app source contains the exact three public destinations:

```text
AMM: Jorgeprdz/AmbientMusic-OneUI-LiveNotifications
PAM: KieronQuinn/NowPlaying
About GitHub: https://github.com/Jorgeprdz/AmbientMusic-OneUI-LiveNotifications
```

- [ ] **Step 5: Review final diff**

```bash
git diff HEAD~3..HEAD --check
git status --short
```

Expected: no whitespace errors and a clean working tree.
