# Architecture

## Overview

Ambient Music for One UI preserves Ambient Music Mod's existing recognition architecture and adds a presentation path for Samsung One UI.

The integration point is intentionally high-level: it observes the central `RecognitionState` flow in `AmbientMusicModForegroundService`, next to the application's existing widget and overlay presentation behavior. Recognition internals are not duplicated or bypassed.

## Data flow

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

## Components

### `RecognitionNowPlayingSurfaceMapper`

Maps a successful Ambient Music Mod recognition to a small presentation event containing:

- track title;
- artist;
- bounded timeout derived from recognition metadata where available.

The mapper does not perform recognition or own Android notification state.

### `NowPlayingSurfaceLifecycle`

Owns recognition-state semantics for the surface:

- `Recognised` → publish mapped track data;
- `Failed` → clear stale surface;
- `Error` → clear stale surface;
- `Recording` → leave the existing successful surface unchanged;
- `Recognising` → leave the existing successful surface unchanged.

### `AndroidLiveUpdateNowPlayingSurfacePublisher`

Owns Android notification behavior. It creates a dedicated high-importance silent channel and posts an ongoing public status notification.

When the derivative selects `EXPERIMENTAL_PROMOTED`, promotion is still requested only if runtime capability checks succeed, including notification permission, enabled notifications, channel state, promotable characteristics and `canPostPromotedNotifications()`.

If those checks fail, the publisher does not attempt private Samsung APIs or privileged workarounds.

## Samsung rendering boundary

The application requests a valid Android promoted ongoing notification. **Samsung SystemUI makes the final rendering decision.**

The project therefore distinguishes between:

1. the application successfully publishing a promoted ongoing Live Update; and
2. Samsung One UI choosing to surface that notification in Now Bar.

A device is not added to confirmed compatibility until both application-level evidence and real-device visual evidence are available.

## Always-on derivative policy

This derivative always constructs the publisher with `NowPlayingSurfacePolicy.EXPERIMENTAL_PROMOTED`.

There is intentionally no user-facing toggle for the One UI integration. Runtime capability checks remain mandatory, so "always on" means the app always attempts the supported public path when a successful recognition occurs; it does not mean promotion is forced on unsupported platforms.

## Public API boundary

The One UI integration uses public Android notification APIs only. It does not rely on:

- private Samsung APIs;
- package-name impersonation;
- a fake media session;
- root modifications to SystemUI;
- framework injection;
- privileged Samsung permissions.

## Package relationship

Ambient Music Mod and Pixel Ambient Music communicate through signature-protected interfaces. Release artifacts must therefore be signed as a pair using the same certificate.

That signing requirement is independent of Now Bar rendering, but it is essential for a working release bundle.
