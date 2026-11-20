---
name: sdk-review
description: >-
  Code review for dd-sdk-kotlin-multiplatform. Use when reviewing a feature branch
  or pull request for platform parity, binary compatibility (JVM, KLIB and Swift
  export), threading bugs, main-thread blocking, and other defects.
when_to_use: >-
  Reviewing a Kotlin Multiplatform SDK branch or PR, checking platform parity,
  concurrency and API compatibility, or requesting another review pass.
---

# sdk-review — dd-sdk-kotlin-multiplatform Code Review

Run the steps below **in order**. Use `AskUserQuestion` for every decision that can be expressed as a closed choice — only fall back to free-form prompts when the user must type something (e.g. a branch name).

When `AskUserQuestion` or `Read` is unavailable, use the current agent's equivalent tools. If no user-input tool is available, ask in chat and wait for the answer before the dependent step.

Follow the repository's `AGENTS.md` for branch naming and review conventions.

All user-facing text (questions, findings, summary) must match the language the user used when invoking the skill. Default to **English** when unsure. Keep the SKILL itself and internal reasoning in English regardless.

## Step 0 — Resolve the working branch

```bash
git branch --show-current
```

### 0a. Accepted pattern

A valid feature branch matches:

```
^[a-zA-Z0-9._-]+/[a-zA-Z][a-zA-Z0-9]*-\d+/[^/]+$
```

(i.e. `<username>/<ticket>/<short-description>`, for example `username/rum-1234/short-description`, following `AGENTS.md`.)

### 0b. If on a protected branch (`main`, `master`, `develop`)

Exception — uncommitted changes: if `git status --porcelain` is not empty, review them in place without switching. Use `git diff HEAD` as the diff. `git diff HEAD` omits untracked files, so for each path from `git ls-files --others --exclude-standard` also add its full content as a new-file patch with `git diff --no-index /dev/null <path>` (exit code 1 is expected). Skip Step 1 and go to Step 2.

Otherwise, do NOT review the protected branch. Offer to switch:

1. Collect up to 3 candidate feature branches the user has recently worked on:
   ```bash
   git for-each-ref --sort=-committerdate --count=20 \
     --format='%(refname:short)|%(committerdate:iso8601)' refs/heads/ \
     | grep -Ev '^(main|master|develop)\|' \
     | head -3
   ```
   Prefer branches that match the accepted pattern; if fewer than 3 match, fall back to the most recently updated non-protected branches.

2. Ask via `AskUserQuestion`:
   > Question: `You are on '<protected-branch>'. Which branch would you like to review?`
   > Options (exactly four):
   > - `<candidate 1>` — last commit `<date>`
   > - `<candidate 2>` — last commit `<date>`
   > - `<candidate 3>` — last commit `<date>`
   > - `Enter another branch name` — prompt the user for free-form input

   If fewer than 3 candidates exist, keep the remaining options but mark missing ones as `— (none)` and still offer `Enter another branch name`.

3. If the user picks one of the candidates, run `git switch <branch>` and continue.

4. If the user picks `Enter another branch name`, ask for the name in a follow-up free-form prompt. If the user submits an empty response or cancels, **exit the skill silently**. Otherwise `git switch <branch>` (abort if the switch fails — surface the git error and stop).

### 0c. If on any other branch that does not match the pattern

Surface a single warning line:

> `Branch '<name>' does not match username/ticket/short-description — review will proceed anyway.`

Do not block. Proceed to Step 1.

## Step 1 — Determine the diff to review

1. Detect the base branch:
   - `git config --get "branch.$(git branch --show-current).merge"` → strip `refs/heads/` prefix.
   - Fallback order if unset: `develop` if it exists locally or on `origin`, else `main`, else `master`.
   - If the result is the current branch itself (it tracks its own remote branch), ask for the base via `AskUserQuestion` with options `develop` (Recommended) and `main`, plus "Other" for a custom branch.

2. Compute the merge base and the changed files:
   ```bash
   BASE="<detected base>"
   git fetch origin "$BASE" --quiet 2>/dev/null || true
   MB=$(git merge-base HEAD "origin/$BASE" 2>/dev/null || git merge-base HEAD "$BASE")
   git diff --name-only "$MB"..HEAD
   ```

3. If the diff is empty, tell the user `No changes to review against <base>.` and exit.

4. For the review itself, read the full diff via:
   ```bash
   git diff "$MB"..HEAD
   ```
   And, when a finding needs surrounding context, `Read` the affected file at HEAD rather than re-running `git show`.

## Step 2 — Analyze the diff

Build a working list of **candidate findings**, checking every changed file against the categories below. Report only real findings, even if there are none, and do not cap the list.

In parallel, run your own built-in code review (e.g. the `code-review` skill in Claude Code, or `/review` in Codex) on the same diff. Merge its findings into the candidate list, dropping duplicates.

Verify every candidate against the code before keeping it, including findings from other review bots on the PR. Common false positives: ABI breaks for declarations customers can't reach, "a Kotlin `List` can be cast to `MutableList`" as a reason to wrap collections, and treating an existing SDK-owned call site as a precedent for a customer-code boundary.

Check every changed file against the following categories. Findings may belong to multiple categories; record the most specific one.

### 2.1 Platform parity and source sets

The SDK exposes one API on Android, iOS and tvOS, backed by `dd-sdk-android` and `dd-sdk-ios`. Flag:

- A change to an `expect` declaration or common behavior that updates only one platform's `actual` (e.g. `androidMain`
  changed, `appleMain` not), or an `actual` whose semantics differ between platforms (defaults, units, nullability,
  thrown exceptions, ignored arguments).
- A `commonMain` API that is silently a no-op on one platform; platform-only capabilities belong in that platform's
  source set.
- Code put in `iosMain` that should also apply to tvOS (`appleMain`), or new code in `appleMain` that doesn't compile
  for tvOS. Session Replay, WebView and Coil 3 have no tvOS targets.
- Mappings between common and native types (enums, models, configuration) that drop a value, map it to the wrong
  native constant, or use an `else` branch that hides new values.
- Behavior re-implemented in Kotlin that the native SDKs already provide, or that should be added to
  `dd-sdk-android` / `dd-sdk-ios` first.
- A `dd-sdk-android` / `dd-sdk-ios` version bump that changes behavior without a matching change in the mapping layer.

Also flag hot-path costs in code that runs for every event or request: generated native-to-common model mapping
used by event mappers, the Ktor plugins and the Coil request listener. Look for unneeded allocations, copies of large
maps, regex compilation, or reflection on these paths.

### 2.2 Binary backwards compatibility (library constraint)

This SDK is published as a library for Android (JVM bytecode), as KLIBs for Apple targets, and its API is exported
to Swift by the apps' frameworks. **Breaking binary compatibility forces downstream apps into dependency hell.** Flag
any change that is not ABI-compatible:

- Removing or renaming a `public` / `protected` class, method, field, property, or top-level function that is part of
  the public API.
- Changing a method signature: return type, parameter types/order, `suspend` modifier, default values exposed to Swift.
- Changing visibility (`public` → `internal`/`private`), `open` → `final`, `abstract` flag, or `companion` modifiers.
- Reordering or removing enum constants.
- Changing a class from `class` to `data class` (or vice versa) — alters `componentN`/`copy`.
- Kotlin-specific: changing `val` to `var` or vice versa on a public property, changing nullability (`T` ↔ `T?`),
  changing generic variance (`in`/`out`).
- Moving a declaration between `commonMain` and a platform source set, or between `expect`/`actual` and a regular
  declaration, without checking that every platform keeps the same API.

Check the `api/<sourceSet>/apiSurface` and `api/*/compiler-meta.txt` diffs: they show what changed for each platform.

Models generated from JSON schemas (see Generated Models in `AGENTS.md`) are an exception: adding a new property
changes their constructor signature, and this is expected — do not flag it.

Declarations in `internal` packages (commonly `…/internal/…`, e.g. `com.datadog.kmp.internal.InternalProxy`) are
**not** customer API even when `public` — do not flag them. The same goes for public declarations that customers can't
reach from any public entry point: removing them or making them `internal` is not a breaking change worth keeping
them for.

Also flag public API shape problems listed under Public API in `AGENTS.md`, notably native SDK types leaking into
`commonMain` signatures and public constructors on types the SDK creates for customers.

### 2.3 Multithreading correctness

Flag potential deadlocks, races, and visibility bugs:

- Nested `synchronized`/`ReentrantLock` acquisitions where lock order depends on runtime state.
- Shared mutable state (`var` field, `MutableList`, `HashMap`) read/written from more than one thread without `@Volatile`, `AtomicReference`, or a lock.
- Check-then-act patterns (`if (cache == null) cache = …`) that are not idempotent under concurrent entry.
- `Handler`/`Executor`/coroutine `launch` callbacks that capture `this` and race with teardown (`onDestroy`, `stop()`).
- Callbacks fired while holding a lock that may re-enter the same lock (lock re-entrance via listener graph).
- Coroutine code that assumes a specific dispatcher without `withContext`, or switches context while holding a non-reentrant lock.
- New threads, executors, coroutine scopes or dispatch queues in SDK code, where the native SDK already handles the work asynchronously.
- Kotlin/Native specifics: Kotlin lambdas passed to Objective-C APIs as callbacks that run on another queue and touch state that isn't thread-safe.
- State set asynchronously and read synchronously elsewhere (a race at startup), where a synchronous read of immutable state would do.

### 2.4 Redundant or wrong synchronization

Flag when concurrency primitives are stacked without purpose:

- `@Volatile` on a field that is **only** read/written inside `synchronized` blocks covering the same monitor (the `synchronized` already provides happens-before; the `@Volatile` is dead weight).
- `AtomicInteger`/`AtomicReference` wrapped in an outer lock that guards every access.
- `AtomicXXX` (or other thread-safe holder) declared as `var` instead of `val`: reassigning the reference bypasses the atomic and is itself an unsynchronized write.
- `synchronized` on a non-final reference (`synchronized(this.lock)` where `lock` can be reassigned).
- Double-checked locking without `@Volatile` on the guarded field (the opposite mistake — flag this too).
- `ConcurrentHashMap` used read-only inside a mutex that already serializes all access.

### 2.5 Main-thread blocking / heavy work

The SDK must never stall the host app's UI thread (Android main thread, iOS/tvOS main queue). Flag:

- Disk or network I/O on the calling thread in SDK code, `Thread.sleep`, or waiting on a latch or semaphore.
- `runBlocking { ... }` anywhere reachable from a public API that a host might call from the UI thread.
- Synchronous native calls known to block (e.g. waiting for a native SDK result) added to APIs that are usually called
  from the UI thread.
- Heavy CPU work (JSON parsing of large payloads, deep copies of attributes, reflection) on the calling thread.
- Immutable configuration re-read on every call instead of once.

### 2.6 PR scope

Flag changes unrelated to the PR's stated purpose (drive-by refactors, unrelated bug fixes, dependency bumps, formatting churn, build-script cleanups) and suggest a separate PR for each. `AGENTS.md` requires focused PRs; mixed changes are harder to review and to revert.

### 2.7 Schemas, models and build logic

- Edits to `features/rum/src/commonMain/json/rum`, which is cloned from `rum-events-format` (see Generated Models in
  `AGENTS.md`): the change must be made upstream first.
- Edits to generated code, or hand-written mapping that duplicates what `json-schema-generator` generates. After a
  schema sync, check that the hand-written common-to-native mapping of mutable fields was updated.
- Build scripts and `tools/build-plugins`: eager task APIs where lazy ones exist (`tasks.withType<T> { }` or
  `tasks.matching { }` instead of `tasks.withType<T>().configureEach { }` / `tasks.named { }`), dependency resolution
  or `.get()` on providers at configuration time, dependencies declared in the wrong source set, and hard-coded worker
  counts or memory settings without evidence they help.
- A new or renamed module that is not registered everywhere it must be (`settings.gradle.kts`, `publishableProjects`
  in the root `build.gradle.kts`, the `publish:release-*` jobs in `.gitlab-ci.yml`).
- Generated files (`apiSurface`, `compiler-meta.txt`, `android-transitiveDependencies`, `LICENSE-3rdparty.csv`) that
  should have changed with the PR but didn't, or changed without a reason in the PR.

### 2.8 Billing impact

Datadog is a paid product, and customers are billed for the data the SDK sends:

- Logs are billed by the number of logs.
- RUM is billed by the number of sessions. A session is created by the first view event with a new session ID.
- Session Replay is billed by the number of RUM sessions that include Session Replay data.
- The Ktor integrations propagate tracing headers with a sampling decision, which controls which backend traces are kept.

Flag any change that can change what customers are billed for, unless the PR already acknowledges it (e.g. in the PR description). Examples: more logs sent, changed default sampling rates (including defaults that differ from the native SDKs), new or split sessions (e.g. a view event that starts a new session ID), more sessions carrying Session Replay data, or a changed tracing sampling decision.

### 2.9 Other defects (open-ended)

Also flag anything else you would catch in a normal review: null-deref risks, incorrect `equals`/`hashCode`, resource leaks (streams or native resources not closed / no `use {}`), swallowed exceptions, catching `Error` (`OutOfMemoryError`, `AssertionError`) to continue, logging of PII, non-exhaustive `when` over sealed types or enums (an `else` branch that silently absorbs new subtypes), misuse of `apply`/`also`, off-by-one, Kotlin exceptions that can reach Objective-C/Swift callers, unit or type mismatches between Kotlin and native values (`Long` vs `NSNumber`, milliseconds vs seconds `NSTimeInterval`), wrong unit-test assertions, tests added for one platform only when both changed, a new copy of a helper that already exists in the module, time values whose name doesn't state the clock base and unit, etc.

Also check that the project's agent docs (`AGENTS.md`, `ai/skills/*`) are still accurate after the change — especially when Gradle tasks, build scripts, or module layout change. Flag any skill or doc that references a renamed, removed, or changed task, command, file, or path.

## Step 3 — Present all findings

Do **not** ask the user about each finding individually. Instead, present the full list of findings in one pass:

1. Print a **detailed description of every finding** to the terminal, in order. Each finding gets its own numbered section with:
   - **Title** — short, category-tagged, e.g. `[Platform parity] appleMain ignores the sample rate`.
   - **Location** — `path/to/File.kt:line` (use the current HEAD line, not the diff index).
   - **Category** — one of `Platform parity`, `Binary compatibility`, `Concurrency`, `Redundant sync`, `Main-thread blocking`, `PR scope`, `Schemas and build`, `Billing`, `Other`.
   - **Explanation** — 3–6 sentences. Explain *what* the problem is, *why* it matters (concrete user-visible impact: different behavior on Android and iOS, crash on update, ANR or UI freeze, data corruption, etc.), and *how* a reader can verify it. Quote the offending snippet.
   - **Suggested fix** — one concrete suggestion.

2. After all detailed descriptions, print a **short summary table** at the end with sequential numbers, one row per finding:

   | # | Title | Location | Category |
   |---|-------|----------|----------|
   | 1 | appleMain ignores the sample rate | `path/to/File.kt:42` | Platform parity |
   | 2 | … | … | … |

The sequential numbers (1, 2, 3, …) are stable references — the user can cite a number when discussing a finding or asking for another round.
