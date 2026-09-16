# Security Policy

## Reporting a security issue

Please use GitHub's private vulnerability reporting / Security Advisory flow for this repository when available.

If private reporting is unavailable, open a public issue containing only a minimal description and request a private contact channel. Do not publish exploit details, credentials, signing material, private device information or proof-of-concept code that would expose users unnecessarily.

## Release signing material

Release signing material is intentionally excluded from source control.

The following must never be committed or attached to public issues, pull requests, Actions artifacts or releases:

```text
*.p12
*.jks
*.keystore
local.properties
NOWBAR-RECOVERY-KEEP-PRIVATE.txt
signing passwords
raw GitHub Actions secret exports
```

Public CI uses an ephemeral disposable key for compile/test validation. Official paired release artifacts use a stable signer supplied to GitHub Actions through encrypted repository secrets.

## Scope

Security reports are particularly useful for issues involving:

- signature-protected AMM/PAM communication;
- unintended exposure of signing material;
- exported Android components or privilege boundaries;
- unsafe file/provider behavior;
- notification or PendingIntent privilege mistakes;
- vulnerabilities introduced by the One UI Live Update integration.

## Upstream issues

This repository is a derivative of Ambient Music Mod and NowPlaying. If a vulnerability clearly exists unchanged in upstream code, please also consider coordinating with the relevant upstream maintainer. Do not publicly disclose sensitive details before maintainers have had a reasonable opportunity to respond.
