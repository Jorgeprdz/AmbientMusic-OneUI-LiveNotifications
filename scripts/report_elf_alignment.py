#!/usr/bin/env python3
"""Report native ELF LOAD alignment inside an Android APK."""

from __future__ import annotations

import argparse
import struct
import zipfile
from pathlib import Path

PT_LOAD = 1
PAGE_16K = 0x4000


def load_alignments(blob: bytes) -> list[int]:
    if len(blob) < 64 or blob[:4] != b"\x7fELF":
        return []

    elf_class = blob[4]
    data = blob[5]
    if data != 1:
        raise ValueError("Only little-endian ELF is supported")

    if elf_class == 2:  # ELF64
        e_phoff = struct.unpack_from("<Q", blob, 32)[0]
        e_phentsize = struct.unpack_from("<H", blob, 54)[0]
        e_phnum = struct.unpack_from("<H", blob, 56)[0]
        fmt = "<IIQQQQQQ"
    elif elf_class == 1:  # ELF32
        e_phoff = struct.unpack_from("<I", blob, 28)[0]
        e_phentsize = struct.unpack_from("<H", blob, 42)[0]
        e_phnum = struct.unpack_from("<H", blob, 44)[0]
        fmt = "<IIIIIIII"
    else:
        raise ValueError(f"Unknown ELF class: {elf_class}")

    expected = struct.calcsize(fmt)
    if e_phentsize < expected:
        raise ValueError("Unexpected ELF program-header size")

    aligns: list[int] = []
    for index in range(e_phnum):
        off = e_phoff + index * e_phentsize
        fields = struct.unpack_from(fmt, blob, off)
        if fields[0] == PT_LOAD:
            aligns.append(fields[-1])
    return aligns


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("apk", type=Path)
    parser.add_argument("--abi", default="arm64-v8a")
    parser.add_argument("--report", type=Path)
    parser.add_argument("--github-env", type=Path)
    args = parser.parse_args()

    prefix = f"lib/{args.abi}/"
    legacy: list[tuple[str, list[int]]] = []
    checked = 0

    with zipfile.ZipFile(args.apk) as apk:
        for name in sorted(apk.namelist()):
            if not (name.startswith(prefix) and name.endswith(".so")):
                continue
            checked += 1
            aligns = load_alignments(apk.read(name))
            if any(align < PAGE_16K for align in aligns):
                legacy.append((name, aligns))

    lines = [
        f"Checked {checked} native libraries for {args.abi}.",
        f"Legacy <16 KiB ELF LOAD alignment: {len(legacy)}.",
    ]
    lines.extend(
        f"{name}: " + ", ".join(f"0x{align:x}" for align in aligns)
        for name, aligns in legacy
    )
    report = "\n".join(lines) + "\n"
    print(report, end="")

    if args.report:
        args.report.write_text(report, encoding="utf-8")
    if args.github_env:
        with args.github_env.open("a", encoding="utf-8") as env:
            env.write(f"PAM_LEGACY_4K_ELF_COUNT={len(legacy)}\n")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
