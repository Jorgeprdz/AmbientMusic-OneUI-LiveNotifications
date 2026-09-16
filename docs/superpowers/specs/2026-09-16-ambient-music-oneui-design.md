# Ambient Music for One UI — Design Specification

Date: 2026-09-16
Status: Approved design, pending implementation plan
Repository: `Jorgeprdz/AmbientMusic-OneUI-LiveNotifications`

## 1. Purpose

Create a professional, public, experimental derivative of Ambient Music Mod focused on Samsung One UI devices where recognised songs can surface in Samsung's real Now Bar through Android 16 promoted Live Update notifications.

The project is intended to be installable and understandable by end users, while remaining technically transparent for developers. It must preserve upstream attribution and GPLv3 obligations, avoid Samsung private APIs, and keep all signing secrets private.

Public project name: **Ambient Music for One UI**.

Repository slug: `AmbientMusic-OneUI-LiveNotifications`.

Suggested short description:

> Now Playing recognition in Samsung's Now Bar using Android Live Updates.

## 2. Product Scope

### In scope

- Preserve Ambient Music Mod's existing recognition pipeline and Pixel Ambient Music integration.
- Publish successful recognitions as Android 16 promoted ongoing Live Update notifications.
- Allow Samsung One UI/SystemUI to render those notifications in the real Now Bar.
- Keep promoted Now Bar publishing always enabled in this experimental derivative; there is no user-facing toggle for disabling promoted mode.
- Preserve ordinary notification behaviour when the platform does not surface the promoted notification in Now Bar.
- Point the app's own update check to this repository's GitHub Releases.
- Keep Pixel Ambient Music update checks pointed at upstream `KieronQuinn/NowPlaying`.
- Point **About → GitHub** to this repository.
- Publish professional documentation, credits, compatibility notes, build instructions, release notes, and troubleshooting information.
- Distribute paired AMM/PAM builds using the same private signing identity where required by the upstream signature-permission architecture.

### Out of scope

- Samsung private APIs.
- SystemUI modification.
- Root-only hooks or modules.
- Package spoofing.
- Fake MediaSession integration solely to force Now Bar rendering.
- A separate companion application.
- Reworking the music-recognition engine.
- Hiding or replacing upstream authorship.
- Publishing signing keystores, passwords, recovery material, or other secrets.

## 3. Architecture

The derivative keeps the working Ambient Music Mod architecture rather than introducing a second app or an IPC bridge.

Recognition data continues to originate from the central Ambient Music Mod recognition state. A dedicated Now Playing surface lifecycle maps `RecognitionState.Recognised` into a presentation event and forwards it to the Android Live Update publisher. Failure/error states and service destruction clear the published surface.

The Android publisher uses public Android 16 notification APIs and requests promoted ongoing treatment. Samsung One UI remains responsible for deciding whether and how the notification is displayed in Now Bar.

This preserves the already validated physical path:

`Pixel Ambient Music → Ambient Music Mod recognition state → Live Update publisher → Android notification framework → Samsung Now Bar`

No component should claim that Samsung provides a project-specific or private Now Bar API.

## 4. Promoted Mode Behaviour

This repository is explicitly an experimental One UI derivative, so promoted mode is **always enabled**.

There will be no settings toggle for choosing between standard-only and promoted modes. The service wiring should use the promoted policy directly. Existing internal policy abstractions may remain if they simplify testing or preserve upstream-compatible structure, but users should not be presented with a switch that changes this behaviour.

If Android or One UI declines promotion, the app should fail gracefully to the normal notification surface rather than treating that as an application error.

## 5. Update Architecture

The existing update system already has separate AMM and PAM update concepts. The implementation must preserve that separation.

### AMM / this derivative

The AMM update provider must resolve GitHub Releases from:

- Owner: `Jorgeprdz`
- Repository: `AmbientMusic-OneUI-LiveNotifications`

This is the source used by **Check for updates** for the application itself.

### PAM

Pixel Ambient Music must continue to resolve releases from:

- Owner: `KieronQuinn`
- Repository: `NowPlaying`

Changing the provider owner globally would be incorrect because it would redirect PAM to a non-existent or unrelated repository.

### Provider design

`GitHubProvider` should accept both an owner and repository rather than hardcoding `KieronQuinn` into its base URL. Each consumer then constructs the provider with an explicit owner/repository pair.

The update repository should maintain distinct cache keys for AMM and PAM so the existing cache behaviour remains independent.

The current release-selection behaviour may remain unchanged for the first release unless implementation review uncovers a correctness issue. The initial project release can therefore be a normal GitHub Release such as `v0.1.0`.

## 6. About and Project Links

**About → GitHub** must open:

`https://github.com/Jorgeprdz/AmbientMusic-OneUI-LiveNotifications`

Upstream links must remain accessible through credits/documentation rather than being silently removed.

The project should not route users through an upstream redirect URL for its own GitHub entry.

## 7. Identity and Compatibility

The public display name is **Ambient Music for One UI**.

The existing Android application/package identity should remain unchanged for the initial derivative unless a later migration is intentionally designed. This avoids unnecessary compatibility risk with the existing Ambient Music Mod / Pixel Ambient Music signature-permission relationship and allows the new build to remain structurally close to upstream.

Branding changes should therefore focus first on app-visible name, README, release metadata, and repository presentation rather than package renaming.

## 8. Signing Model

Ambient Music Mod and Pixel Ambient Music depend on a signature-permission relationship. Release artifacts intended to work together must therefore use the same signing identity where required by the upstream architecture.

Signing material must remain outside the public repository and be supplied to CI through GitHub Actions secrets or equivalent private secret storage.

Never commit:

- PKCS12/keystore files.
- Keystore passwords.
- Key aliases/passwords.
- Recovery text.
- Base64-encoded private signing material.

Public releases may include cryptographic checksums of APK artifacts, but not secret material.

## 9. Release Model

Initial release target: **v0.1.0 Experimental**.

Recommended public assets:

- `AmbientMusicMod-OneUI.apk`
- `PixelAmbientMusic-1.3.5-paired.apk`
- `SHA256SUMS.txt`

Release notes should clearly state:

- Experimental status.
- Android/One UI compatibility tested so far.
- That the Now Bar path relies on Android public Live Update notification APIs and Samsung rendering behaviour.
- That both paired APKs must remain signature-compatible when installed together.
- Known limitations.

## 10. Documentation and Repository Presentation

The repository should look like a maintained open-source project rather than a disposable test dump.

The README should contain:

1. Project title and concise purpose.
2. A real-device screenshot showing the feature on Samsung Now Bar, where available and appropriate for redistribution.
3. Badges for Android 16, One UI focus, CI status, and GPLv3.
4. Features.
5. Compatibility.
6. Installation.
7. How it works / architecture.
8. Update behaviour.
9. Limitations.
10. Build instructions.
11. Troubleshooting.
12. Credits and upstream projects.
13. License.
14. Non-affiliation notice.

Suggested non-affiliation text:

> Unofficial community project. Not affiliated with Samsung, Google, or Kieron Quinn.

The repository may be described as experimental or a proof of concept, but should not be presented publicly as low-quality or disposable.

## 11. Credits and Licensing

The derivative must preserve GPLv3 licensing requirements for covered upstream code and retain relevant copyright/license notices.

README credits should prominently acknowledge:

- **Kieron Quinn** — creator of Ambient Music Mod and the upstream porting work this derivative builds on.
- **KieronQuinn/AmbientMusicMod** — primary upstream application foundation.
- **KieronQuinn/NowPlaying** — Pixel Ambient Music / Now Playing component used by Ambient Music Mod.
- **Google / AOSP** — Android platform and Live Update notification APIs; acknowledgement does not imply affiliation.
- **Samsung** — One UI / Now Bar target platform; acknowledgement does not imply affiliation.
- **Jorge Palacios (`Jorgeprdz`)** — One UI Live Update / Now Bar integration, physical validation, derivative maintenance, and release packaging.

Third-party components with their own licenses must retain those licenses as applicable.

## 12. Error Handling

- Recognition failure or error clears the Now Bar/live-update surface.
- Service destruction clears the surface.
- Missing notification permission or inability to post promoted notifications must not crash recognition.
- Failure to fetch GitHub Releases should retain the existing update-state error semantics.
- Failure to fetch the derivative's AMM release must not alter PAM update-source selection.
- Network/update failures should remain recoverable by retry/reload.

## 13. Testing Strategy

Implementation should preserve existing tests and add focused coverage for the new repository-routing behaviour.

Minimum coverage:

- AMM provider targets `Jorgeprdz/AmbientMusic-OneUI-LiveNotifications`.
- PAM provider targets `KieronQuinn/NowPlaying`.
- The two update sources are independent.
- AMM update state still maps a newer GitHub Release to `UpdateAvailable`.
- PAM update behaviour remains unchanged.
- Existing Now Playing mapper/lifecycle/publisher tests remain green.
- Production build compiles with Android 16 APIs.
- Release APKs pass signature verification and paired certificate checks.
- Physical Samsung S25 smoke test confirms recognition appears in Samsung Now Bar.

Where practical, URL construction should be testable without making live GitHub network calls.

## 14. Success Criteria

The first public experimental release is successful when all of the following are true:

1. A supported Samsung device can recognise a song through the existing AMM/PAM pipeline.
2. A successful recognition can appear in Samsung's real Now Bar through the public Android Live Update path.
3. The app's **Check for updates** checks this repository's releases.
4. PAM update checks still use `KieronQuinn/NowPlaying`.
5. **About → GitHub** opens this repository.
6. CI builds successfully without exposing signing secrets.
7. Paired release APKs share the required signing identity.
8. README, credits, license information, limitations, and installation instructions are present and professional.
9. No Samsung private API, SystemUI modification, package spoofing, or root dependency is introduced for Now Bar publishing.

## 15. Implementation Boundaries

The implementation plan should focus only on work required to turn the already working derivative into a polished public project:

- Update-provider ownership/repository routing.
- About/GitHub link.
- Public display branding and repository documentation.
- CI/release packaging with existing private signing model.
- Tests for routing and regression protection.
- First experimental release preparation.

Unrelated refactors are explicitly excluded from the first release.