#!/usr/bin/env python3
"""Patch Pixel Ambient Music's decoded manifest for Android 16 compatibility.

The upstream 1.3.5 APK contains prebuilt native libraries from Android System
Intelligence that are 4 KiB ELF-aligned. Android 16 provides an official
compatibility mode for these binaries. This patch opts into that mode explicitly
and ensures the repackaged release is not marked debuggable.
"""

from __future__ import annotations

import re
import sys
from pathlib import Path


def set_android_attr(text: str, name: str, value: str) -> str:
    attr = rf'android:{re.escape(name)}\s*=\s*"[^"]*"'
    replacement = f'android:{name}="{value}"'
    if re.search(attr, text):
        return re.sub(attr, replacement, text, count=1)

    match = re.search(r"<application\b", text)
    if not match:
        raise RuntimeError("<application> element not found in AndroidManifest.xml")

    return text[: match.end()] + f' android:{name}="{value}"' + text[match.end() :]


def main() -> int:
    if len(sys.argv) != 2:
        print(f"usage: {sys.argv[0]} AndroidManifest.xml", file=sys.stderr)
        return 2

    manifest = Path(sys.argv[1])
    text = manifest.read_text(encoding="utf-8")

    text = set_android_attr(text, "debuggable", "false")
    text = set_android_attr(text, "pageSizeCompat", "true")

    manifest.write_text(text, encoding="utf-8")

    patched = manifest.read_text(encoding="utf-8")
    for expected in (
        'android:debuggable="false"',
        'android:pageSizeCompat="true"',
    ):
        if expected not in patched:
            raise RuntimeError(f"manifest patch verification failed: {expected}")

    print("Patched manifest: debuggable=false, pageSizeCompat=true")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
