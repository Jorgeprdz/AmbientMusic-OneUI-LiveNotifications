# Compatibility

## Confirmed devices

The initial public project has one physically validated target:

| Device | Model | Android | One UI | Result |
|---|---|---:|---:|---|
| Samsung Galaxy S25 | SM-S931B | 16 | 8.x | Recognition and real Now Bar rendering validated |

Validation included a successful recognition of **Dream On — Aerosmith**, followed by Samsung SystemUI rendering the title and artist in the real lock-screen Now Bar.

## What "confirmed" means

A device is listed as confirmed only when all of the following are available:

1. Ambient Music Mod recognition succeeds on the physical device.
2. The derivative publisher posts its dedicated Live Update notification.
3. Android reports the promoted ongoing state when promotion is available.
4. Samsung SystemUI visibly renders the result in Now Bar.
5. The Android version and One UI version are recorded.

A successful build, emulator test, matching Samsung model family or shared One UI major version is not sufficient by itself.

## Expected but unvalidated compatibility

Other Samsung devices running Android versions that support promoted ongoing Live Updates may be technically capable of using the same public notification path. They remain **unvalidated** until tested on physical hardware.

No untested Galaxy S-series, Fold, Flip, A-series or tablet model is currently claimed as confirmed compatible.

## Non-Samsung Android devices

The publisher uses public Android notification APIs, so the notification path can remain valid outside Samsung devices. However, this project specifically targets Samsung One UI Now Bar presentation and does not claim an equivalent OEM surface elsewhere.

## Reporting a new device

Compatibility reports should include:

- exact device model;
- Android version;
- One UI version;
- app release/commit;
- whether recognition itself succeeded;
- `dumpsys notification` evidence for the derivative notification;
- a screenshot or screen recording showing the real Now Bar result when possible.

Remove personal information from screenshots and logs before publishing them.
