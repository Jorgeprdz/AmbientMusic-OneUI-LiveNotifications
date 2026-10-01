#!/usr/bin/env python3
"""Patch Pixel Ambient Music's compiled AndroidManifest.xml in place.

This intentionally edits Android binary XML (AXML) without decoding or rebuilding
PAM's resources or DEX files. The upstream Android System Intelligence payload
contains private framework resource references that cannot be safely round-tripped
through a full Apktool resource build.

Changes on <application>:
  * android:debuggable = false
  * android:pageSizeCompat = enabled (enum value 32, API 36)

The pageSizeCompat resource ID is android.R.attr.pageSizeCompat (0x010106ab).
"""

from __future__ import annotations

import argparse
import struct
from pathlib import Path

RES_XML_TYPE = 0x0003
RES_STRING_POOL_TYPE = 0x0001
RES_XML_RESOURCE_MAP_TYPE = 0x0180
RES_XML_START_ELEMENT_TYPE = 0x0102

UTF8_FLAG = 0x00000100
SORTED_FLAG = 0x00000001
NO_INDEX = 0xFFFFFFFF

TYPE_INT_DEC = 0x10
TYPE_INT_BOOLEAN = 0x12

ANDROID_URI = "http://schemas.android.com/apk/res/android"

ANDROID_ATTR_DEBUGGABLE = 0x0101000F
ANDROID_ATTR_PAGE_SIZE_COMPAT = 0x010106AB
PAGE_SIZE_COMPAT_ENABLED = 32


class AxmlError(RuntimeError):
    pass


def u16(data: bytes | bytearray, off: int) -> int:
    return struct.unpack_from("<H", data, off)[0]


def u32(data: bytes | bytearray, off: int) -> int:
    return struct.unpack_from("<I", data, off)[0]


def p16(data: bytearray, off: int, value: int) -> None:
    struct.pack_into("<H", data, off, value)


def p32(data: bytearray, off: int, value: int) -> None:
    struct.pack_into("<I", data, off, value)


def chunk_header(data: bytes | bytearray, off: int) -> tuple[int, int, int]:
    if off + 8 > len(data):
        raise AxmlError(f"truncated chunk header at 0x{off:x}")
    ctype, hsize, size = struct.unpack_from("<HHI", data, off)
    if hsize < 8 or size < hsize or off + size > len(data):
        raise AxmlError(
            f"invalid chunk at 0x{off:x}: type=0x{ctype:04x} "
            f"header={hsize} size={size}"
        )
    return ctype, hsize, size


def read_len8(blob: bytes, off: int) -> tuple[int, int]:
    first = blob[off]
    if first & 0x80:
        return ((first & 0x7F) << 8) | blob[off + 1], off + 2
    return first, off + 1


def read_len16(blob: bytes, off: int) -> tuple[int, int]:
    first = u16(blob, off)
    if first & 0x8000:
        second = u16(blob, off + 2)
        return ((first & 0x7FFF) << 16) | second, off + 4
    return first, off + 2


def decode_pool_string(blob: bytes, rel_off: int, utf8: bool) -> str:
    if rel_off < 0 or rel_off >= len(blob):
        raise AxmlError(f"invalid string offset {rel_off}")
    if utf8:
        _, pos = read_len8(blob, rel_off)  # UTF-16 code-unit length
        byte_len, pos = read_len8(blob, pos)
        end = pos + byte_len
        if end > len(blob):
            raise AxmlError("truncated UTF-8 string")
        return blob[pos:end].decode("utf-8")
    char_len, pos = read_len16(blob, rel_off)
    end = pos + char_len * 2
    if end > len(blob):
        raise AxmlError("truncated UTF-16 string")
    return blob[pos:end].decode("utf-16le")


def encode_len8(value: int) -> bytes:
    if value > 0x7FFF:
        raise AxmlError("UTF-8 string length exceeds AXML limit")
    if value > 0x7F:
        return bytes(((value >> 8) | 0x80, value & 0xFF))
    return bytes((value,))


def encode_len16(value: int) -> bytes:
    if value > 0x7FFFFFFF:
        raise AxmlError("UTF-16 string length exceeds AXML limit")
    if value > 0x7FFF:
        return struct.pack("<HH", ((value >> 16) | 0x8000), value & 0xFFFF)
    return struct.pack("<H", value)


def encode_pool_string(value: str, utf8: bool) -> bytes:
    if utf8:
        raw = value.encode("utf-8")
        utf16_units = len(value.encode("utf-16le")) // 2
        return encode_len8(utf16_units) + encode_len8(len(raw)) + raw + b"\x00"
    raw = value.encode("utf-16le")
    units = len(raw) // 2
    return encode_len16(units) + raw + b"\x00\x00"


class StringPool:
    def __init__(self, chunk: bytes):
        ctype, hsize, size = chunk_header(chunk, 0)
        if ctype != RES_STRING_POOL_TYPE or size != len(chunk):
            raise AxmlError("not a complete string-pool chunk")
        if hsize < 28:
            raise AxmlError("unexpected string-pool header")

        self.original = chunk
        self.header_size = hsize
        self.string_count = u32(chunk, 8)
        self.style_count = u32(chunk, 12)
        self.flags = u32(chunk, 16)
        self.strings_start = u32(chunk, 20)
        self.styles_start = u32(chunk, 24)
        self.utf8 = bool(self.flags & UTF8_FLAG)

        offsets_pos = hsize
        self.string_offsets = [
            u32(chunk, offsets_pos + i * 4) for i in range(self.string_count)
        ]
        style_pos = offsets_pos + self.string_count * 4
        self.style_offsets = [
            u32(chunk, style_pos + i * 4) for i in range(self.style_count)
        ]

        strings_end = self.styles_start if self.styles_start else len(chunk)
        if not (self.strings_start <= strings_end <= len(chunk)):
            raise AxmlError("invalid string-pool data bounds")

        self.strings_blob = bytes(chunk[self.strings_start:strings_end])
        self.styles_blob = (
            bytes(chunk[self.styles_start:]) if self.styles_start else b""
        )
        self.strings = [
            decode_pool_string(self.strings_blob, off, self.utf8)
            for off in self.string_offsets
        ]

    def ensure(self, value: str) -> int:
        try:
            return self.strings.index(value)
        except ValueError:
            pass

        new_offset = len(self.strings_blob)
        self.strings_blob += encode_pool_string(value, self.utf8)
        self.string_offsets.append(new_offset)
        self.strings.append(value)
        self.string_count += 1
        self.flags &= ~SORTED_FLAG
        return self.string_count - 1

    def build(self) -> bytes:
        strings_blob = self.strings_blob
        while len(strings_blob) % 4:
            strings_blob += b"\x00"

        new_strings_start = (
            self.header_size + 4 * (self.string_count + self.style_count)
        )
        new_styles_start = (
            new_strings_start + len(strings_blob) if self.style_count else 0
        )
        total_size = (
            new_strings_start
            + len(strings_blob)
            + (len(self.styles_blob) if self.style_count else 0)
        )

        header = bytearray(self.original[: self.header_size])
        p32(header, 4, total_size)
        p32(header, 8, self.string_count)
        p32(header, 12, self.style_count)
        p32(header, 16, self.flags)
        p32(header, 20, new_strings_start)
        p32(header, 24, new_styles_start)

        offsets = b"".join(struct.pack("<I", x) for x in self.string_offsets)
        style_offsets = b"".join(struct.pack("<I", x) for x in self.style_offsets)

        out = bytes(header) + offsets + style_offsets + strings_blob
        if self.style_count:
            out += self.styles_blob
        if len(out) != total_size:
            raise AxmlError("rebuilt string-pool size mismatch")
        return out


def split_xml_chunks(data: bytes) -> tuple[bytearray, list[bytes]]:
    ctype, hsize, total_size = chunk_header(data, 0)
    if ctype != RES_XML_TYPE:
        raise AxmlError("file is not Android binary XML")
    if total_size != len(data):
        raise AxmlError(
            f"AXML top-level size mismatch: header={total_size}, actual={len(data)}"
        )

    top_header = bytearray(data[:hsize])
    chunks: list[bytes] = []
    pos = hsize
    while pos < len(data):
        _, _, size = chunk_header(data, pos)
        chunks.append(bytes(data[pos : pos + size]))
        pos += size
    if pos != len(data):
        raise AxmlError("AXML chunk walk did not end at EOF")
    return top_header, chunks


def resource_map_ids(chunk: bytes) -> tuple[int, bytearray, list[int]]:
    ctype, hsize, size = chunk_header(chunk, 0)
    if ctype != RES_XML_RESOURCE_MAP_TYPE:
        raise AxmlError("not a resource-map chunk")
    if (size - hsize) % 4:
        raise AxmlError("invalid resource-map size")
    ids = [u32(chunk, hsize + i * 4) for i in range((size - hsize) // 4)]
    return hsize, bytearray(chunk[:hsize]), ids


def build_resource_map(
    original: bytes | None, mappings: dict[int, int]
) -> bytes:
    if original is None:
        hsize = 8
        header = bytearray(struct.pack("<HHI", RES_XML_RESOURCE_MAP_TYPE, 8, 8))
        ids: list[int] = []
    else:
        hsize, header, ids = resource_map_ids(original)

    max_index = max(mappings)
    if len(ids) <= max_index:
        ids.extend([0] * (max_index + 1 - len(ids)))
    for string_index, resource_id in mappings.items():
        ids[string_index] = resource_id

    size = hsize + len(ids) * 4
    p32(header, 4, size)
    return bytes(header) + b"".join(struct.pack("<I", x) for x in ids)


def attr_name(strings: list[str], attr: bytes | bytearray) -> str:
    name_idx = u32(attr, 4)
    if name_idx >= len(strings):
        return f"<bad-string-{name_idx}>"
    return strings[name_idx]


def attr_ns(strings: list[str], attr: bytes | bytearray) -> str | None:
    ns_idx = u32(attr, 0)
    if ns_idx == NO_INDEX:
        return None
    if ns_idx >= len(strings):
        return f"<bad-string-{ns_idx}>"
    return strings[ns_idx]


def patch_application_chunk(
    chunk: bytes,
    strings: list[str],
    android_uri_index: int,
    debuggable_index: int,
    page_size_index: int,
) -> bytes:
    ctype, hsize, size = chunk_header(chunk, 0)
    if ctype != RES_XML_START_ELEMENT_TYPE:
        raise AxmlError("not a start-element chunk")
    if hsize < 16 or size != len(chunk):
        raise AxmlError("unexpected start-element header")

    element_name_index = u32(chunk, 20)
    if element_name_index >= len(strings) or strings[element_name_index] != "application":
        return chunk

    out = bytearray(chunk)

    attr_start = u16(out, 24)
    attr_size = u16(out, 26)
    attr_count = u16(out, 28)
    if attr_size < 20:
        raise AxmlError(f"unsupported AXML attribute size: {attr_size}")

    attrs_base = 16 + attr_start
    attrs_end = attrs_base + attr_count * attr_size
    if attrs_end > len(out):
        raise AxmlError("application attribute array exceeds chunk")

    debug_off: int | None = None
    page_off: int | None = None

    for i in range(attr_count):
        off = attrs_base + i * attr_size
        a = out[off : off + attr_size]
        if attr_ns(strings, a) != ANDROID_URI:
            continue
        name = attr_name(strings, a)
        if name == "debuggable":
            debug_off = off
        elif name == "pageSizeCompat":
            page_off = off

    def set_typed_value(off: int, name_index: int, data_type: int, value: int) -> None:
        p32(out, off + 0, android_uri_index)
        p32(out, off + 4, name_index)
        p32(out, off + 8, NO_INDEX)
        p16(out, off + 12, 8)
        out[off + 14] = 0
        out[off + 15] = data_type
        p32(out, off + 16, value)

    if debug_off is None:
        record = bytearray(attr_size)
        p32(record, 0, android_uri_index)
        p32(record, 4, debuggable_index)
        p32(record, 8, NO_INDEX)
        p16(record, 12, 8)
        record[14] = 0
        record[15] = TYPE_INT_BOOLEAN
        p32(record, 16, 0)
        out[attrs_end:attrs_end] = record
        debug_off = attrs_end
        attrs_end += attr_size
        attr_count += 1
        p16(out, 28, attr_count)
        p32(out, 4, len(out))
    else:
        set_typed_value(debug_off, debuggable_index, TYPE_INT_BOOLEAN, 0)

    # If insertion above shifted a pre-existing pageSizeCompat located after it,
    # recompute its location. In practice debuggable is normally already present,
    # but this keeps the patcher structurally correct.
    page_off = None
    for i in range(attr_count):
        off = attrs_base + i * attr_size
        a = out[off : off + attr_size]
        if attr_ns(strings, a) == ANDROID_URI and attr_name(strings, a) == "pageSizeCompat":
            page_off = off
            break

    if page_off is None:
        record = bytearray(attr_size)
        p32(record, 0, android_uri_index)
        p32(record, 4, page_size_index)
        p32(record, 8, NO_INDEX)
        p16(record, 12, 8)
        record[14] = 0
        record[15] = TYPE_INT_DEC
        p32(record, 16, PAGE_SIZE_COMPAT_ENABLED)
        out[attrs_end:attrs_end] = record
        attr_count += 1
        p16(out, 28, attr_count)
        p32(out, 4, len(out))
    else:
        set_typed_value(
            page_off,
            page_size_index,
            TYPE_INT_DEC,
            PAGE_SIZE_COMPAT_ENABLED,
        )

    return bytes(out)


def inspect_application(data: bytes) -> dict[str, tuple[int, int]]:
    _, chunks = split_xml_chunks(data)
    pool_chunk = next(
        (c for c in chunks if u16(c, 0) == RES_STRING_POOL_TYPE),
        None,
    )
    if pool_chunk is None:
        raise AxmlError("AXML string pool not found")
    pool = StringPool(pool_chunk)

    result: dict[str, tuple[int, int]] = {}
    for chunk in chunks:
        if u16(chunk, 0) != RES_XML_START_ELEMENT_TYPE:
            continue
        if len(chunk) < 36:
            continue
        name_idx = u32(chunk, 20)
        if name_idx >= len(pool.strings) or pool.strings[name_idx] != "application":
            continue

        attr_start = u16(chunk, 24)
        attr_size = u16(chunk, 26)
        attr_count = u16(chunk, 28)
        attrs_base = 16 + attr_start
        for i in range(attr_count):
            off = attrs_base + i * attr_size
            attr = chunk[off : off + attr_size]
            if attr_ns(pool.strings, attr) != ANDROID_URI:
                continue
            name = attr_name(pool.strings, attr)
            data_type = attr[15]
            value = u32(attr, 16)
            result[name] = (data_type, value)
        break
    return result


def patch_axml(data: bytes) -> bytes:
    top_header, chunks = split_xml_chunks(data)

    pool_i = next(
        (i for i, c in enumerate(chunks) if u16(c, 0) == RES_STRING_POOL_TYPE),
        None,
    )
    if pool_i is None:
        raise AxmlError("AXML string pool not found")

    pool = StringPool(chunks[pool_i])
    android_uri_index = pool.ensure(ANDROID_URI)
    debuggable_index = pool.ensure("debuggable")
    page_size_index = pool.ensure("pageSizeCompat")
    application_index = pool.ensure("application")

    # application_index is intentionally resolved before rebuilding the pool;
    # existing element string indexes are unchanged because we only append.
    del application_index

    chunks[pool_i] = pool.build()

    resource_i = next(
        (i for i, c in enumerate(chunks) if u16(c, 0) == RES_XML_RESOURCE_MAP_TYPE),
        None,
    )
    mappings = {
        debuggable_index: ANDROID_ATTR_DEBUGGABLE,
        page_size_index: ANDROID_ATTR_PAGE_SIZE_COMPAT,
    }
    if resource_i is None:
        resource_chunk = build_resource_map(None, mappings)
        chunks.insert(pool_i + 1, resource_chunk)
    else:
        chunks[resource_i] = build_resource_map(chunks[resource_i], mappings)

    found_application = False
    for i, chunk in enumerate(chunks):
        if u16(chunk, 0) != RES_XML_START_ELEMENT_TYPE:
            continue
        if len(chunk) < 36:
            continue
        name_idx = u32(chunk, 20)
        if name_idx < len(pool.strings) and pool.strings[name_idx] == "application":
            chunks[i] = patch_application_chunk(
                chunk,
                pool.strings,
                android_uri_index,
                debuggable_index,
                page_size_index,
            )
            found_application = True
            break

    if not found_application:
        raise AxmlError("<application> element not found")

    body = b"".join(chunks)
    p32(top_header, 4, len(top_header) + len(body))
    patched = bytes(top_header) + body

    attrs = inspect_application(patched)
    if attrs.get("debuggable") != (TYPE_INT_BOOLEAN, 0):
        raise AxmlError(f"debuggable verification failed: {attrs.get('debuggable')}")
    if attrs.get("pageSizeCompat") != (TYPE_INT_DEC, PAGE_SIZE_COMPAT_ENABLED):
        raise AxmlError(
            f"pageSizeCompat verification failed: {attrs.get('pageSizeCompat')}"
        )

    return patched


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("manifest", type=Path)
    parser.add_argument(
        "--verify",
        action="store_true",
        help="verify expected application attributes without modifying the file",
    )
    args = parser.parse_args()

    data = args.manifest.read_bytes()

    if args.verify:
        attrs = inspect_application(data)
        debug = attrs.get("debuggable")
        compat = attrs.get("pageSizeCompat")
        if debug != (TYPE_INT_BOOLEAN, 0):
            raise AxmlError(f"debuggable is not false: {debug}")
        if compat != (TYPE_INT_DEC, PAGE_SIZE_COMPAT_ENABLED):
            raise AxmlError(f"pageSizeCompat is not enabled: {compat}")
        print(
            "Verified binary manifest: "
            "debuggable=false, pageSizeCompat=enabled (32)"
        )
        return 0

    patched = patch_axml(data)
    args.manifest.write_bytes(patched)
    print(
        "Patched binary manifest: "
        "debuggable=false, pageSizeCompat=enabled (32)"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
