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

If upstream contains commits not yet present locally, the workflow creates or refreshes an automation branch named from the upstream commit, using the form:

`automation/upstream-sync-<short-sha>`

It performs a real Git merge of `upstream/main` into that branch. Upstream history and authorship must remain visible; the workflow must not copy files manually or replace local `main` with an upstream snapshot.

### Validation gate

Before any automated merge reaches `main`, the candidate branch must pass the same compatibility checks used by the project CI, including at minimum:

- unit tests;
- instrumentation-test compilation; and
- release compilation.

The upstream-sync workflow must not require private signing secrets merely to determine whether source changes compile. Existing release-signing workflows remain responsible for paired signed artifacts.

### Automatic merge

If the merge is conflict-free and all validation succeeds, the workflow may fast-forward or merge the validated candidate into `main` and delete the temporary automation branch.

The resulting history must make the upstream merge identifiable. A merge commit such as `Merge upstream KieronQuinn/AmbientMusicMod@<short-sha>` is preferred when a merge commit is required.

### Fail closed

If the source merge conflicts, compilation fails, or tests fail:

- `main` must remain unchanged;
- the automation branch may remain for investigation;
- the workflow must fail rather than force-resolving conflicts;
- it must create or update a single tracking issue describing the pending upstream SHA, failure type, and relevant conflicting files or failed validation step.

Repeated scheduled runs for the same unresolved upstream state must update the existing tracking issue instead of creating unlimited duplicate issues.

Once a later sync succeeds, the tracking issue should be closed automatically if it represents the resolved upstream state.

## 4. Permissions and Safety

The workflow must use the minimum GitHub Actions permissions required for its job. Expected permissions are limited to repository contents and issues.

The workflow must not read, print, copy, rotate, or alter signing secrets. It must never commit keystores, recovery data, credentials, or generated signed APKs.

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

`GitHubProvider` should accept an explicit owner and repository. `UpdatesRepositoryImpl` should construct two providers with their correct owner/repository pairs.

The existing AMM and PAM cache keys remain independent.

## 6. About → GitHub

`UpdatesViewModelImpl` must open this repository directly for **About → GitHub**:

`https://github.com/Jorgeprdz/AmbientMusic-OneUI-LiveNotifications`

The upstream project must remain prominently credited in README/docs; changing this UI link must not erase upstream attribution.

## 7. Testing

Add focused regression coverage proving:

- `GitHubProvider` can build a provider for an arbitrary explicit owner/repository pair;
- AMM self-updates target `Jorgeprdz/AmbientMusic-OneUI-LiveNotifications`;
- PAM updates target `KieronQuinn/NowPlaying`;
- the About GitHub URL is this repository;
- existing Now Bar mapper/lifecycle/publisher tests remain green;
- the upstream-sync workflow has a no-update path, a successful merge path, and a fail-closed path.

Network-dependent unit tests should be avoided where URL construction or injected provider configuration can be tested deterministically.

## 8. Documentation

Create or update `docs/UPSTREAM.md` to document:

- upstream repository and branch;
- scheduled/manual sync behaviour;
- validation requirements;
- fail-closed behaviour;
- how to inspect an unresolved sync issue;
- that local One UI / Now Bar integration remains derivative-specific.

README should mention that upstream application changes are monitored automatically but only merged when project validation passes.

## 9. Success Criteria

The work is complete when:

1. A daily/manual Action detects new `KieronQuinn/AmbientMusicMod/main` commits.
2. No-op runs leave the repository unchanged.
3. Compatible upstream changes automatically merge only after validation.
4. Conflicts or regressions leave `main` unchanged and produce/update a useful tracking issue.
5. The app's **Check for updates** uses this repository's Releases.
6. PAM continues to use `KieronQuinn/NowPlaying`.
7. **About → GitHub** opens this repository.
8. Existing Now Bar behaviour and tests remain intact.
9. Signing secrets are not required or exposed by upstream sync.
