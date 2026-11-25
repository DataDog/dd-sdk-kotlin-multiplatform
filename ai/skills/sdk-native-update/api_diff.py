#!/usr/bin/env python3
#
# Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
# This product includes software developed at Datadog (https://www.datadoghq.com/).
# Copyright 2016-Present Datadog, Inc.
#

"""
Analyzes the public API changes of the native Datadog SDKs between two versions and writes a Markdown report.

Sections:
  1. dd-sdk-android: diff of every `**/api/apiSurface` file between the two tags.
  2. dd-sdk-ios: diff of `api-surface-objc` between the two tags.
  3. RUM schema gaps: RUM event model properties available in both native SDKs (dd-sdk-android vendored schema,
     rum-events-format commit dd-sdk-ios generated its models from) but missing from the KMP schema in
     features/rum/src/commonMain/json/rum.

The script doesn't decide which Android and iOS API changes are the same feature: names often differ between
the platforms, so that pairing is left to the reader (see SKILL.md).

The native repositories default to sibling clones (../dd-sdk-android, ../dd-sdk-ios, ../rum-events-format) and
fall back to blobless clones in build/native-sdk-update/. In existing clones the script only runs `git fetch`
(tags and the needed rum-events-format commits); it never checks out or changes a working tree.

Run from the repository root. Python 3 standard library only.
"""

import argparse
import json
import os
import posixpath
import re
import subprocess
import sys
from collections import Counter, defaultdict

ANDROID_URL = "https://github.com/DataDog/dd-sdk-android.git"
IOS_URL = "https://github.com/DataDog/dd-sdk-ios.git"
RUM_FORMAT_URL = "https://github.com/DataDog/rum-events-format.git"
WORK_DIR = os.path.join("build", "native-sdk-update")
LIBRARY_DIRS = ("core", "features", "integrations")
RELEASE_TAG = re.compile(r"^\d+\.\d+\.\d+$")

KMP_RUM_SCHEMA_DIR = "features/rum/src/commonMain/json/rum"
ANDROID_RUM_SCHEMA_DIR = "features/dd-sdk-android-rum/src/main/json/rum"
IOS_RUM_MODELS = "DatadogInternal/Sources/Models/RUM/RUMDataModels.swift"
# KMP schema file -> event type, as in jsonSchemaGenerator.inputNameMapping of features/rum/build.gradle.kts.
RUM_EVENTS = {
    "view-schema.json": "View",
    "action-schema.json": "Action",
    "error-schema.json": "Error",
    "resource-schema.json": "Resource",
    "long_task-schema.json": "LongTask",
}
# Event types that have a KMP model (features/rum, features/logs). Other native models are not mirrored.
KMP_EVENTS = set(RUM_EVENTS.values()) | {"Log"}

IOS_MODEL = re.compile(r"^objc_(RUM\w*?Event|Telemetry\w*?Event|LogEvent)")
IOS_EVENT = re.compile(r"^objc_(?:RUM)?(\w+?)Event")
ANDROID_EVENT = re.compile(r"\.model\.(\w+?)Event\b")


# ---------------------------------------------------------------- git helpers


def git(repo, *args, check=True):
    result = subprocess.run(["git", "-C", repo, *args], capture_output=True, text=True)
    if check and result.returncode != 0:
        sys.exit(f"git {' '.join(args)} failed in {repo}:\n{result.stderr}")
    return result.stdout


def ensure_repo(path, url, fetch=("--tags",)):
    name = os.path.basename(url)[:-len(".git")]
    if path is None and os.path.exists(os.path.join("..", name, ".git")):
        path = os.path.join("..", name)
    if path is None:
        path = os.path.join(WORK_DIR, name)
        if not os.path.isdir(path):
            print(f"Cloning {url} into {path} (blobless, no checkout)...", file=sys.stderr)
            subprocess.run(["git", "clone", "-q", "--filter=blob:none", "--no-checkout", url, path], check=True)
    print(f"Fetching {' '.join(fetch)} in {path}...", file=sys.stderr)
    git(path, "fetch", "-q", "origin", *fetch)
    return path


def latest_tag(repo):
    tags = [t for t in git(repo, "tag", "-l").split() if RELEASE_TAG.match(t)]
    return max(tags, key=lambda t: tuple(int(p) for p in t.split(".")))


def show(repo, ref, path):
    result = subprocess.run(["git", "-C", repo, "show", f"{ref}:{path}"], capture_output=True, text=True)
    return result.stdout if result.returncode == 0 else ""


# ---------------------------------------------------------------- KMP project


def catalog():
    with open(os.path.join("gradle", "libs.versions.toml")) as f:
        text = f.read()
    versions = dict(re.findall(r'^(datadog-(?:android|ios))\s*=\s*"([^"]+)"', text, re.M))
    aliases = dict(re.findall(r'^(datadog-android-[\w-]+)\s*=\s*\{\s*module\s*=\s*"com\.datadoghq:([\w-]+)"', text, re.M))
    return versions, aliases


def wrapped_modules(aliases):
    """Native module -> KMP library modules using it, read from the library build files."""
    android, ios = defaultdict(set), defaultdict(set)
    accessors = {"libs." + alias.replace("-", "."): module for alias, module in aliases.items()}
    for top in LIBRARY_DIRS:
        for root, dirs, files in os.walk(top):
            dirs[:] = [d for d in dirs if d not in ("build", "src")]
            if "build.gradle.kts" not in files:
                continue
            with open(os.path.join(root, "build.gradle.kts")) as f:
                text = f.read()
            for accessor, module in accessors.items():
                if re.search(re.escape(accessor) + r"(?![\w.])", text):
                    android[module].add(root)
            for framework in re.findall(r'framework\("(\w+)"\)', text):
                ios[framework].add(root)
    return android, ios


def normalize(identifier):
    return re.sub(r"[^a-z0-9]", "", identifier.lower()) if identifier else None


def split_params(signature):
    """Splits `name(a, b<c, d>)` into its top-level parameters."""
    start, end = signature.find("("), signature.rfind(")")
    if start < 0 or end < start:
        return []
    params, depth, current = [], 0, ""
    for ch in signature[start + 1:end]:
        if ch in "<([":
            depth += 1
        elif ch in ">)]":
            depth -= 1
        if ch == "," and depth == 0:
            params.append(current.strip())
            current = ""
        else:
            current += ch
    if current.strip():
        params.append(current.strip())
    return params


def describe_param_change(old, new):
    before, after = Counter(split_params(old)), Counter(split_params(new))
    parts = [f"{sum(before.values())} → {sum(after.values())} params"]
    if after - before:
        parts.append("added: " + ", ".join(f"`{p}`" for p in (after - before).elements()))
    if before - after:
        parts.append("removed: " + ", ".join(f"`{p}`" for p in (before - after).elements()))
    return "; ".join(parts)


# ---------------------------------------------------------------- dd-sdk-android apiSurface


def android_entries(text):
    """Returns {(ancestor lines..., line)} for an indentation-based apiSurface file."""
    entries, stack = set(), []
    for raw in text.splitlines():
        if not raw.strip():
            continue
        indent = len(raw) - len(raw.lstrip())
        while stack and stack[-1][0] >= indent:
            stack.pop()
        line = raw.strip()
        entries.add(tuple(s for _, s in stack) + (line,))
        stack.append((indent, line))
    return entries


def android_key(line):
    """Stable key of a declaration, used to pair a removed line with its changed version."""
    if line.startswith("constructor"):
        return "constructor"
    m = re.match(r"- (\w+)", line)
    if m:
        return "- " + m.group(1)
    m = re.search(r"\b(class|interface|object|fun|val|var|enum|typealias)\s+(?:<[^>]*>\s*)?([\w.]+)", line)
    return f"{m.group(1)} {m.group(2)}" if m else line


def android_event(entry):
    m = ANDROID_EVENT.search(entry[0])
    return m.group(1) if m else None


def collapse(entries):
    """Drops entries whose parent is listed too: an added class implies its members."""
    present = set(entries)
    return [e for e in entries if not any(e[:i] in present for i in range(1, len(e)))]


def pair_changes(added, removed, parent, key):
    """Pairs added/removed entries that are the same declaration with a new signature."""
    by_key = defaultdict(list)
    for r in removed:
        by_key[(parent(r), key(r))].append(r)
    changed, still_added = [], []
    for a in added:
        candidates = by_key.get((parent(a), key(a)))
        if candidates:
            changed.append((candidates.pop(0), a))
        else:
            still_added.append(a)
    paired = {id(r) for r, _ in changed}
    still_removed = [r for r in removed if id(r) not in paired]
    return still_added, still_removed, changed


def android_surface(repo, old, new):
    """Returns {module: (added, removed, changed)}."""
    paths = git(repo, "ls-tree", "-r", "--name-only", new).splitlines()
    paths = [p for p in paths if p.endswith("/api/apiSurface")]
    changed_paths = set(git(repo, "diff", "--name-only", old, new, "--", ":(glob)**/api/apiSurface").split())
    changes = {}
    for path in paths:
        module = path.split("/")[-3]
        if path in changed_paths:
            after = android_entries(show(repo, new, path))
            before = android_entries(show(repo, old, path))
            added, removed = collapse(sorted(after - before)), collapse(sorted(before - after))
            changes[module] = pair_changes(added, removed, lambda e: e[:-1], lambda e: android_key(e[-1]))
    for path in changed_paths - set(paths):
        changes[path.split("/")[-3] + " (module removed)"] = ([], [], [])
    return changes


# ---------------------------------------------------------------- dd-sdk-ios api-surface-objc


def ios_entries(text):
    """Returns {(module, owner, member)}; member is "" for the owner declaration itself."""
    entries, module, owner = set(), "?", None
    for raw in text.splitlines():
        if not raw.strip() or raw.startswith("# ---"):
            continue
        m = re.match(r"# API surface for (\w+):", raw)
        if m:
            module, owner = m.group(1), None
        elif raw[0] != " ":
            owner = raw.strip()
            entries.add((module, owner, ""))
        else:
            entries.add((module, owner or "?", raw.strip()))
    return entries


def ios_owner_name(owner):
    m = re.search(r"(objc_\w+)", owner)
    return m.group(1) if m else owner


def ios_event(owner):
    name = ios_owner_name(owner)
    if not IOS_MODEL.match(name):
        return None
    return IOS_EVENT.match(name).group(1)


def ios_identifier(owner, member):
    if not member:
        name = ios_owner_name(owner)
        prefix = IOS_EVENT.match(name) if IOS_MODEL.match(name) else None
        return name[prefix.end():] if prefix else name.replace("objc_", "")
    m = re.match(r"(?:case\s+|.*\b(?:var|let|func)\s+)`?(\w+)`?", member)
    return m.group(1) if m else None


def ios_key(entry):
    _, owner, member = entry
    m = re.match(r".*\b(var|let|case)\s+`?(\w+)`?", member)
    return f"{m.group(1)} {m.group(2)}" if m else member


def ios_surface(repo, old, new):
    """Returns ({module: (added, removed, changed)}, {(event, name): entries} of the new event models)."""
    before = ios_entries(show(repo, old, "api-surface-objc"))
    after = ios_entries(show(repo, new, "api-surface-objc"))
    index = defaultdict(list)
    for e in after:
        index[(ios_event(e[1]) or "", normalize(ios_identifier(e[1], e[2])))].append(e)

    def without_members_of_new_owners(entries):
        owners = {(m, o) for m, o, mem in entries if not mem}
        return sorted(e for e in entries if not e[2] or (e[0], e[1]) not in owners)

    added = without_members_of_new_owners(after - before)
    removed = without_members_of_new_owners(before - after)
    changes = {}
    for module in sorted({e[0] for e in added + removed}):
        changes[module] = pair_changes(
            [e for e in added if e[0] == module], [e for e in removed if e[0] == module],
            lambda e: e[1], ios_key
        )
    return changes, index


# ---------------------------------------------------------------- RUM schemas


def flatten_schema(read, name):
    """Flattens a JSON schema file into {property path: (readOnly, deprecated)}; enum values are `path=value`."""
    result, loaded = {}, {}

    def load(file):
        if file not in loaded:
            text = read(file)
            loaded[file] = json.loads(text) if text else {}
        return loaded[file]

    def walk(node, path, file, seen):
        if not isinstance(node, dict):
            return
        ref = node.get("$ref")
        if ref and not ref.startswith("#"):
            target = posixpath.normpath(posixpath.join(posixpath.dirname(file), ref))
            if target not in seen:
                walk(load(target), path, target, seen | {target})
        for combinator in ("allOf", "oneOf", "anyOf"):
            for child in node.get(combinator, []):
                walk(child, path, file, seen)
        for prop, child in node.get("properties", {}).items():
            child_path = f"{path}.{prop}" if path else prop
            child = child if isinstance(child, dict) else {}
            read_only, deprecated = result.get(child_path, (True, False))
            result[child_path] = (read_only and child.get("readOnly", True),
                                  deprecated or child.get("deprecated", False))
            walk(child, child_path, file, seen)
        if "items" in node:
            walk(node["items"], path + "[]", file, seen)
        if isinstance(node.get("additionalProperties"), dict):
            walk(node["additionalProperties"], path + ".*", file, seen)
        for value in node.get("enum", []):
            result[f"{path}={value}"] = (True, False)

    walk(load(name), "", name, {name})
    return result


def ios_schema_sha(ios_repo, tag):
    m = re.search(r"rum-events-format/tree/([0-9a-f]{40})", show(ios_repo, tag, IOS_RUM_MODELS))
    return m.group(1) if m else None


def top_level(paths):
    """Keeps only the topmost paths: a missing object implies its properties and enum values."""
    path_set = set(paths)
    return sorted(p for p in path_set
                  if not any(p != q and p.startswith(q) and p[len(q)] in ".[=" for q in path_set))


def schema_gaps(android_repo, android_old, android_new, ios_repo, ios_old, ios_new, rum_format_path, ios_index,
                kmp_ref):
    """Per KMP RUM event: properties available in both native SDKs but missing in the KMP schema."""
    sha_old, sha_new = ios_schema_sha(ios_repo, ios_old), ios_schema_sha(ios_repo, ios_new)
    if not sha_new:
        return None, f"Could not find the rum-events-format commit in `{IOS_RUM_MODELS}` at {ios_new}."
    rum_format = ensure_repo(rum_format_path, RUM_FORMAT_URL, fetch=tuple(sorted({s for s in (sha_old, sha_new) if s})))

    def kmp_read(file):
        if kmp_ref:
            return show(".", kmp_ref, f"{KMP_RUM_SCHEMA_DIR}/{file}")
        path = os.path.join(KMP_RUM_SCHEMA_DIR, file)
        return open(path).read() if os.path.isfile(path) else ""

    def android_schema(tag, file):
        return flatten_schema(lambda f: show(android_repo, tag, f"{ANDROID_RUM_SCHEMA_DIR}/{f}"), file)

    def ios_schema(sha, file):
        return flatten_schema(lambda f: show(rum_format, sha, f"schemas/rum/{f}"), file) if sha else {}

    gaps = {}
    for file, event in RUM_EVENTS.items():
        kmp = flatten_schema(kmp_read, file)
        android, ios = android_schema(android_new, file), ios_schema(sha_new, file)
        both_old = android_schema(android_old, file).keys() & ios_schema(sha_old, file).keys()
        both = {p: (android[p][0] and ios[p][0], android[p][1] or ios[p][1]) for p in android.keys() & ios.keys()}
        missing = [p for p in both if p not in kmp]

        def ios_objc(path):
            leaf = normalize(re.split(r"[.=\[\]]+", path.rstrip("[]"))[-1])
            return bool(ios_index.get((event, leaf)))

        missing_top = set(top_level(missing))
        def mutable_children(path):
            return sorted(q[len(path) + 1:] for q, (read_only, _) in both.items()
                          if q.startswith(path) and q[len(path):len(path) + 1] in ".[" and not read_only)

        new = sorted(p for p in missing_top if p not in both_old)
        old = sorted(p for p in missing_top if p in both_old)
        not_in_kmp = set(top_level(missing + list(android.keys() ^ ios.keys())))
        gaps[event] = {
            "new": [(p, *both[p], ios_objc(p), mutable_children(p)) for p in new
                    if not p.startswith("_dd") and "=" not in p],
            "new_enum_values": [p for p in new if not p.startswith("_dd") and "=" in p],
            "new_internal": [p for p in new if p.startswith("_dd")],
            "old": old,
            "android_only": sorted(p for p in not_in_kmp if p in android.keys() - ios.keys() - kmp.keys()),
            "ios_only": sorted(p for p in not_in_kmp if p in ios.keys() - android.keys() - kmp.keys()),
        }
    return gaps, sha_new


# ---------------------------------------------------------------- report


def wrapped_label(module, wrapped):
    users = sorted(wrapped.get(module, []))
    return "wrapped by " + ", ".join(f"`{u}`" for u in users) if users else "not wrapped by KMP"


def render_platform(out, title, changes, wrapped, fmt, line_of, event_of):
    out.append(f"# {title}\n")
    if not changes:
        out.append("No changes.\n")
        return
    for module in sorted(changes, key=lambda m: (m not in wrapped, m)):
        added, removed, changed = changes[module]
        out.append(f"## {module} ({wrapped_label(module, wrapped)})\n")
        for kind, is_model in (("API", False), ("Event models", True)):
            sections = (
                ("Added", [e for e in added if bool(event_of(e)) == is_model], lambda e: f"- `+ {fmt(e)}`"),
                ("Removed", [e for e in removed if bool(event_of(e)) == is_model], lambda e: f"- `- {fmt(e)}`"),
                ("Changed", [c for c in changed if bool(event_of(c[1])) == is_model], None),
            )
            for name, items, render in sections:
                if not items:
                    continue
                out.append(f"### {name} — {kind}\n")
                for item in items:
                    if render:
                        out.append(render(item))
                    else:
                        old, new = item
                        line_old, line_new = line_of(old), line_of(new)
                        out.append(f"- `~ {fmt(new)}`")
                        if "(" in line_old and "(" in line_new:
                            out.append(f"  - {describe_param_change(line_old, line_new)}")
                        else:
                            out.append(f"  - was `{line_old}`")
                out.append("")


def render_schema(out, gaps, sha, android_tag, ios_tag, kmp_ref):
    out.append("# 3. RUM schema gaps\n")
    if gaps is None:
        out.append(sha + "\n")
        return
    out.append(f"Compares `{KMP_RUM_SCHEMA_DIR}`{' at ' + kmp_ref if kmp_ref else ''} with dd-sdk-android {android_tag} `{ANDROID_RUM_SCHEMA_DIR}` and "
               f"rum-events-format@{sha[:10]} (the commit dd-sdk-ios {ios_tag} generated its models from). "
               "Only the topmost missing path is listed: a missing object implies its children, `path=value` is an "
               "enum value. \"iOS objc ✗\" means no same-named property, class or case was found in the iOS objc "
               "surface of the event: check `RUMDataModels.swift` before relying on it. `readOnly: false` "
               "properties also need the hand-written common → native mapping.\n")
    for event, gap in gaps.items():
        out.append(f"## {event}Event\n")
        out.append(f"### Properties that became available in both native SDKs with this update, missing in KMP ({len(gap['new'])})\n")
        for path, read_only, deprecated, in_objc, mutable in gap["new"]:
            notes = [] if in_objc else ["iOS objc ✗"]
            if deprecated:
                notes.append("**deprecated**")
            if not read_only:
                notes.append("**mutable (readOnly: false)**")
            if mutable:
                notes.append("**mutable children:** " + ", ".join(f"`{m}`" for m in mutable))
            out.append(f"- `{path}`" + (f" — {', '.join(notes)}" if notes else ""))
        if not gap["new"]:
            out.append("None.")
        details = (("new_enum_values", "New enum values in both native SDKs, missing in KMP (unknown native values "
                                       "map to the default from `defaultEnumValues` in features/rum/build.gradle.kts)"),
                   ("new_internal", "New `_dd` internals in both native SDKs, missing in KMP"),
                   ("old", "Older gaps (already in both native SDKs before this update; may be deliberate)"),
                   ("android_only", "Android only, not in KMP"),
                   ("ios_only", "iOS only, not in KMP"))
        for key, label in details:
            if gap[key]:
                out.append(f"\n<details><summary>{label} ({len(gap[key])})</summary>\n")
                out.extend(f"- `{p}`" for p in gap[key])
                out.append("\n</details>")
        out.append("")


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--android-to", help="target dd-sdk-android tag (default: latest release tag)")
    parser.add_argument("--ios-to", help="target dd-sdk-ios tag (default: latest release tag)")
    parser.add_argument("--android-from", help="current dd-sdk-android tag (default: gradle/libs.versions.toml)")
    parser.add_argument("--ios-from", help="current dd-sdk-ios tag (default: gradle/libs.versions.toml)")
    parser.add_argument("--android-repo", help="dd-sdk-android clone (default: ../dd-sdk-android, else build/)")
    parser.add_argument("--ios-repo", help="dd-sdk-ios clone (default: ../dd-sdk-ios, else build/)")
    parser.add_argument("--rum-events-format-repo", help="rum-events-format clone (default: ../rum-events-format, "
                                                         "else build/)")
    parser.add_argument("--kmp-ref", help="git ref to read the KMP RUM schema from (default: working tree); "
                                          "use it to replay a past update")
    parser.add_argument("--output", default=os.path.join(WORK_DIR, "api-diff.md"))
    args = parser.parse_args()

    if not os.path.isfile(os.path.join("gradle", "libs.versions.toml")):
        sys.exit("Run from the dd-sdk-kotlin-multiplatform repository root.")
    os.makedirs(WORK_DIR, exist_ok=True)
    versions, aliases = catalog()
    wrapped_android, wrapped_ios = wrapped_modules(aliases)

    android_repo = ensure_repo(args.android_repo, ANDROID_URL)
    ios_repo = ensure_repo(args.ios_repo, IOS_URL)
    a_old, a_new = args.android_from or versions["datadog-android"], args.android_to or latest_tag(android_repo)
    i_old, i_new = args.ios_from or versions["datadog-ios"], args.ios_to or latest_tag(ios_repo)
    print(f"dd-sdk-android {a_old} -> {a_new}, dd-sdk-ios {i_old} -> {i_new}", file=sys.stderr)

    android = android_surface(android_repo, a_old, a_new)
    ios = ios_surface(ios_repo, i_old, i_new)
    gaps, sha = schema_gaps(android_repo, a_old, a_new, ios_repo, i_old, i_new, args.rum_events_format_repo, ios[1],
                            args.kmp_ref)

    out = [f"<!-- dd-sdk-android {a_old} → {a_new}; dd-sdk-ios {i_old} → {i_new} -->\n"]
    render_platform(out, f"1. dd-sdk-android {a_old} → {a_new} (`**/api/apiSurface`)", android, wrapped_android,
                    lambda e: " › ".join(e), lambda e: e[-1], android_event)
    render_platform(out, f"2. dd-sdk-ios {i_old} → {i_new} (`api-surface-objc`)", ios[0], wrapped_ios,
                    lambda e: f"{e[1]}{' › ' + e[2] if e[2] else ''}", lambda e: e[2] or e[1],
                    lambda e: ios_event(e[1]))
    render_schema(out, gaps, sha, a_new, i_new, args.kmp_ref)

    report = "\n".join(out)
    os.makedirs(os.path.dirname(args.output) or ".", exist_ok=True)
    with open(args.output, "w") as f:
        f.write(report)
    print(report)
    print(f"Report written to {args.output}", file=sys.stderr)


if __name__ == "__main__":
    main()
