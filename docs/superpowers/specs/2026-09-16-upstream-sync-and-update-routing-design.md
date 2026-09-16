# Upstream Sync and Update Routing — Design Specification

Date: 2026-09-16
Status: Approved design
Repository: `Jorgeprdz/AmbientMusic-OneUI-LiveNotifications`
Parent spec: `docs/superpowers/specs/2026-09-16-ambient-music-oneui-design.md`

## 1. Purpose

Keep Ambient Music for One UI aligned with upstream `KieronQuinn/AmbientMusicMod` without allowing incompatible upstream changes to break the derivative, while making the app's own **Check for updates** and **About → GitHub** point to this repository.

## 2. Upstream Source

The authoritative application upstream is:

- Owner: `KieronQuinn`
- Repository: `AmbientMusicMod`
- Branch: `main`

Pixel Ambient Music remains independently sourced from `KieronQuinn/NowPlaying`.

## 3. Automated Upstream Sync

Create `.github/workflows/upstream-sync.yml` with both:

- a daily scheduled run; and
- `workflow_dispatch` for manual runs.

The workflow must fetch `KieronQuinn/AmbientMusicMod/main` and compare it with this repository's `main`.

### No upstream changes

If the fetched upstream commit is already contained in local `main`, the workflow exits successfully with a clear `NO_UPDATES` summary and makes no commit, branch, issue, or merge.

### Upstream changes available

If upstream contains commits not yet present locally, the workflow creates a local automation branch named from the upstream commit, using the form:

`automation/upstream-sync-<short-sha>`

It performs a real Git merge of `upstream/main` into that branch. Upstream history and authorship must remain visible; the workflow must not copy files manually or replace local `main` with an upstream snapshot.

### Validation gate

Before any automated merge reaches `main`, the candidate must pass the same compatibility checks used by the project CI, including at minimum:

- unit tests;
- instrumentation-test compilation; and
- release compilation.

The upstream-sync workflow must not require private signing secrets merely to determine whether source changes compile. Existing release-signing workflows remain responsible for paired signed artifacts.

### Automatic merge

If the merge is conflict-free and all validation succeeds, the workflow may push the validated merge commit to `main` with a normal non-force push.

Immediately before push, it must verify that `origin/main` is still the exact base commit from which the candidate was created. A moved base is treated as a blocked/raced run rather than being force-resolved.

The resulting history must make the upstream merge identifiable. A merge commit such as `Merge upstream KieronQuinn/AmbientMusicMod@<short-sha>` is required when upstream changes are integrated.

### Fail closed

If the source merge conflicts, compilation fails, tests fail, or `main` moves before push:

- `main` must remain unchanged;
- the workflow must not force-resolve conflicts;
- it must create or update a single tracking issue describing the pending upstream SHA, failure type, and relevant conflicting files or failed validation step.

Repeated scheduled runs for the same unresolved upstream state must update the existing tracking issue instead of creating duplicate issues.

Once a later sync succeeds, or a no-update run proves that the pending upstream state is already contained, the tracking issue should be closed automatically.

## 4. Permissions and Safety

The workflow must use the minimum GitHub Actions permissions required for its job. Expected permissions are limited to repository contents and issues.

The workflow must not read, print, copy, rotate, or alter release-signing secrets. It must never commit keystores, recovery data, credentials, or generated signed APKs.

Automated merge is permitted only after validation passes. There is no `git push --force` to `main`.

## 5. In-App Update Routing

The existing update architecture has two independent sources and must preserve that separation.

### Ambient Music for One UI

The app's AMM/self-update provider must resolve releases from:

- Owner: `Jorgeprdz`
- Repository: `AmbientMusic-OneUI-LiveNotifications`

This is the source used by the app's **Check for updates** flow.

### Pixel Ambient Music

PAM updates must continue to resolve releases from:

- Owner: `KieronQuinn`
- Repository: `NowPlaying`

A global replacement of the GitHub owner is explicitly forbidden because it would break PAM update routing.

### Provider boundary

`GitHubProvider` accepts an explicit owner and repository. `UpdatesRepositoryImpl` constructs two providers with their correct owner/repository pairs.

The existing AMM and PAM cache keys remain independent.

## 6. About → GitHub

`UpdatesViewModelImpl` must open this repository directly for **About → GitHub**:

`https://github.com/Jorgeprdz/AmbientMusic-OneUI-LiveNotifications`

The upstream project must remain prominently credited in README/docs; changing this UI link must not erase upstream attribution.

## 7. Release and Version Contract

The first public derivative release is **`v0.1.0 Experimental`**.

For this release:

- Android `versionName`: `v0.1.0`
- `BuildConfig.TAG_NAME`: `v0.1.0`
- Android `versionCode`: `241`
- GitHub release tag: `v0.1.0`

`versionCode` is greater than the previously built derivative's `240`, allowing an in-place upgrade for users already on the paired derivative signer. The release tag and local `BuildConfig.TAG_NAME` match exactly so the existing update-state logic reports the installed release as up to date.

The public prerelease contains exactly these user-facing bundle assets:

- `AmbientMusicMod-OneUI.apk`
- `PixelAmbientMusic-1.3.5-paired.apk`
- `SHA256SUMS.txt`

Because the release intentionally contains two APK files, the AMM self-updater must select `AmbientMusicMod-OneUI.apk` by filename and must not depend on GitHub asset ordering. PAM continues to use its own upstream release and normal APK selection.

The paired workflow remains fail-closed: unit tests, instrumentation compilation, release build, pinned PAM checksum, APK signature verification, package-name checks, matching certificate digest, and checksum creation all complete before release publishing.

The paired workflow may be rerun for the same tag. A rerun updates/clobbers the three verified assets on that release rather than creating duplicate releases.

No private signing material is attached to a release.

## 8. Branding

The public application label is **Ambient Music for One UI** while the package ID remains `com.kieronquinn.app.ambientmusicmod`.

The About screen continues to credit Kieron Quinn/upstream appropriately, while the GitHub action points to the derivative repository.

## 9. Testing

Add focused regression coverage proving:

- `GitHubProvider` can build a provider for an arbitrary explicit owner/repository pair;
- AMM self-updates target `Jorgeprdz/AmbientMusic-OneUI-LiveNotifications`;
- PAM updates target `KieronQuinn/NowPlaying`;
- the About GitHub URL is this repository;
- AMM release conversion chooses `AmbientMusicMod-OneUI.apk` when both AMM and PAM APK assets are present;
- existing Now Bar mapper/lifecycle/publisher tests remain green;
- the upstream-sync workflow has a no-update path, a successful merge path, and a fail-closed path.

Network-dependent unit tests should be avoided where URL construction or deterministic model conversion can be tested locally.

## 10. Documentation

Create or update `docs/UPSTREAM.md` to document:

- upstream repository and branch;
- scheduled/manual sync behaviour;
- validation requirements;
- fail-closed behaviour;
- how to inspect an unresolved sync issue;
- that local One UI / Now Bar integration remains derivative-specific.

README should mention that upstream application changes are monitored automatically but only merged when project validation passes, and that public releases contain the paired APK bundle required by the signing architecture.

## 11. Success Criteria

The work is complete when:

1. A daily/manual Action detects new `KieronQuinn/AmbientMusicMod/main` commits.
2. No-op runs leave the repository unchanged.
3. Compatible upstream changes automatically merge only after validation.
4. Conflicts or regressions leave `main` unchanged and produce/update a useful tracking issue.
5. The app's **Check for updates** uses this repository's Releases.
6. PAM continues to use `KieronQuinn/NowPlaying`.
7. **About → GitHub** opens this repository.
8. AMM download selection deterministically resolves `AmbientMusicMod-OneUI.apk` from the paired release.
9. `v0.1.0` is a public prerelease containing both paired APKs and `SHA256SUMS.txt`.
10. Existing Now Bar behaviour and tests remain intact.
11. Signing secrets are not required or exposed by upstream sync or public releases.
