# Start here

Read and follow all guidance in [CONTRIBUTING.md](CONTRIBUTING.md), then follow the module
rules and validation tasks below.

Include necessary documentation with each implementation change (KDoc, module `README.md`,
and this file and the skills in `ai/skills/` when tasks, paths or conventions change). Record actual
validation evidence in task output or reports. Include validation in PR descriptions only when
relevant to review, following the [PR preparation skill](ai/skills/sdk-pr-prepare/SKILL.md).

# Project Overview

This repository is the Datadog SDK for Kotlin Multiplatform (KMP). It is a thin layer over the
native Datadog SDKs: the Android target delegates to [`dd-sdk-android`](https://github.com/DataDog/dd-sdk-android)
and the Apple targets (iOS, tvOS) delegate to [`dd-sdk-ios`](https://github.com/DataDog/dd-sdk-ios).

- Native SDK versions are pinned in `gradle/libs.versions.toml` (`datadog-android`, `datadog-ios`).
  `NATIVE_SDK_VERSIONS.md` records which native versions each KMP release ships with; it is updated
  at release time by `tools/release/update_native_sdk_versions.sh`, not in feature PRs.
- Behavior (batching, upload, sampling, session handling, …) lives in the native SDKs. The KMP layer
  maps the common API and models to the native ones. If a feature needs new native behavior, it usually
  has to land in `dd-sdk-android` / `dd-sdk-ios` first, followed by a native SDK version bump here.
- Supported platforms: Android API 23+ and iOS/tvOS 15+ (see `README.md`). Session Replay, WebView
  tracking and the Coil 3 integration have no tvOS targets.

# Module Map

- `core` — SDK entry point (`Datadog`), configuration, tracking consent, user and account info.
- `features/*` — product features: `logs`, `rum`, `session-replay`, `webview`.
- `integrations/*` — third-party library integrations: `ktor` (Ktor 2), `ktor3` (Ktor 3), `coil3` (Coil 3).
- `tools/build-plugins` — included build with the convention plugins (`datadog-build-config`, `api-surface`,
  `transitive-dependencies`, `json-schema-generator`, `datadog-ios-build-spm`, `datadog-ios-frameworks`,
  `detekt-conventions`, `ktlint`).
- `tools/unit` — shared unit test utilities (Elmyr forge factories for JVM tests, random-data helpers for
  common and Apple tests).
- `tools/license`, `tools/lint`, `tools/release` — license header check, SwiftLint configuration, release scripts.
- `sample/shared` — KMP module shared by the sample apps; `sample/androidApp` — Android sample app;
  `sample/appleApp` — Xcode project with the iOS and tvOS sample apps.

New modules must be registered in `settings.gradle.kts`. A new publishable module must also be added to
`publishableProjects` in the root `build.gradle.kts` (used by the aggregate test tasks) and get its own
`publish:release-*` job in `.gitlab-ci.yml`.

# Source Sets and Platform Code

Library modules use the default KMP source set hierarchy:

- `commonMain` — public API shared by all platforms. Platform-specific behavior is declared with
  `expect` and implemented with `actual`, often behind an internal `Platform*` interface (see
  `features/logs/src/commonMain/kotlin/com/datadog/kmp/log/internal/PlatformLogger.kt`).
- `androidMain` — `actual`s delegating to `dd-sdk-android`.
- `appleMain` — `actual`s for iOS and tvOS, delegating to `dd-sdk-ios` through cinterop. `iosMain` and
  `tvosMain` hold code that exists on one Apple platform only.
- Tests: `commonTest`, `androidHostTest` (JVM host tests of the Android target), `appleTest`, `iosTest`,
  `tvosTest`. See the `sdk-tests` skill.

Apple interop:

- The `dd-sdk-ios` frameworks are built with Swift Package Manager by the root `datadog-ios-build-spm`
  plugin (`./gradlew :buildDatadogFrameworks`). Each module declares the frameworks it binds to in a
  `datadogFrameworks { }` block (`linkOnly = true` for frameworks only needed at link time), which
  generates the cinterop definitions.
- Bindings are imported from `cocoapods.<Framework>` packages (e.g. `cocoapods.DatadogRUM.DDRUMActionType`).
  The package prefix is historical; dependencies are no longer managed with CocoaPods.
- Apple targets compile and test only on macOS with Xcode installed.
- Public `commonMain` / `appleMain` declarations are visible from Swift through the framework that an app
  exports (see `sample/shared/build.gradle.kts`). Check how new API looks from Swift; the sample apps in
  `sample/appleApp` consume it.

# Agent Skills

Load the matching project skill before these tasks. The links below point to the
tracked sources in `ai/skills/`. The `.claude/skills/` directory aliases expose those
same sources to Claude Code.

- Writing, editing, or reviewing tests: [`sdk-tests`](ai/skills/sdk-tests/SKILL.md).
- Reviewing a branch or PR: [`sdk-review`](ai/skills/sdk-review/SKILL.md).
- Writing commit messages or PR titles: [`sdk-commit`](ai/skills/sdk-commit/SKILL.md).
- External references and example prompts for feature work: [`sdk-feature-knowledge`](ai/skills/sdk-feature-knowledge/SKILL.md).
- Running pre-PR checks and regenerating generated files: [`sdk-pr-prepare`](ai/skills/sdk-pr-prepare/SKILL.md).
- Creating or updating stacked PRs with Graphite: [`sdk-pr-stack`](ai/skills/sdk-pr-stack/SKILL.md).

# Build Commands

The root `build.gradle.kts` registers aggregate tasks:

```bash
./gradlew jvmUnitTestAll        # Android host tests (incl. commonTest) of all publishable modules + build-plugins tests
./gradlew iosUnitTestAll        # iOS simulator tests (macOS only)
./gradlew tvosUnitTestAll       # tvOS simulator tests (macOS only)
./gradlew unitTestAll           # all of the above
./gradlew ktlintCheckAll        # ktlint, including the build-plugins included build
./gradlew ktlintFormatAll       # ktlint format, including the build-plugins included build
./gradlew detektAll             # detekt, including the build-plugins included build
./gradlew checkGeneratedFiles   # API surface, compiler metadata (Apple only on macOS), transitive dependencies
```

`lintCheckAll` currently fails (the KMP Android library plugin creates no `lint` task in library modules);
CI runs Android Lint with `./gradlew lintRelease`, which only exists in `sample/androidApp`.

`./local_ci.sh` mimics the CI steps (`--clean`, `--analysis`, `--compile`, `--test`); see `CONTRIBUTING.md`.
Note that `--clean` runs `./gradlew --stop`, which also stops Gradle daemons used by other checkouts; don't run it
when other builds may be running.

Prefer per-module tasks, which are much faster than the aggregate ones:

```bash
./gradlew :features:rum:testAndroidHostTest
./gradlew :features:rum:testAndroidHostTest --tests "*RumMonitorAdapterTest*"   # single test class
./gradlew :features:rum:iosSimulatorArm64Test --tests "*RumMonitorAdapterTest*" # single Apple test class (macOS only)
./gradlew :features:rum:allTests                                                # all targets of the module
./gradlew :build-plugins:test                                                   # build-plugins (included build) tests
```

Sample apps:

```bash
./gradlew :sample:androidApp:assembleDebug
./gradlew :buildDatadogFrameworks   # then build sample/appleApp/appleApp.xcodeproj (schemes "Sample iOS", "Sample tvOS")
```

The sample apps read credentials from the gitignored `config/us1.json` (`token`, `rumApplicationId`). Without it
the build still works, but the apps don't send data to Datadog.

# Before You Finish

Run these for every module you changed before considering the work done:

```bash
./gradlew :<module>:ktlintFormat
./gradlew :<module>:detekt
./gradlew :<module>:testAndroidHostTest      # Android host and commonTest tests
./gradlew :<module>:iosSimulatorArm64Test    # if common or Apple code changed (macOS only); add :<module>:tvosSimulatorArm64Test for modules with tvOS targets
./gradlew checkGeneratedFiles                # if public API, dependencies or compiler metadata may have changed; passes only after committing them (see below)
```

The test tasks above exist in the library modules (`core`, `features/*`, `integrations/*`). For other modules:
`tools/unit` uses `:tools:unit:jvmTest` and `:tools:unit:iosSimulatorArm64Test`; `sample/androidApp` uses
`:sample:androidApp:assembleDebug`; for changes in `tools/build-plugins`, use `:build-plugins:ktlintFormat`,
`:build-plugins:detekt` and `:build-plugins:test`.

Kotlin warnings are errors (`allWarningsAsErrors`) in library modules, so a new warning fails the build.

# Checked-in Generated Files

Publishable modules have generated files checked into git. **CI fails if they are stale.**

| File | Update task | Check task |
|---|---|---|
| `api/<sourceSet>/apiSurface` (`commonMain`, `androidMain`, `appleMain`, `iosMain`, `tvosMain`) | `./gradlew :<module>:generateApiSurface` | `checkApiSurfaceChanges` |
| `api/androidMain/compiler-meta.txt` | `./gradlew :<module>:generateCompilerMetadata` | `checkCompilerMetadataChanges` |
| `api/appleMain/compiler-meta.txt` (`api/iosMain/…` for modules without tvOS) | `./gradlew :<module>:generateCompilerMetadata` (macOS only) | `checkKlibCompilerMetadataChanges` |
| `android-transitiveDependencies` | `./gradlew :<module>:listTransitiveDependencies` | `checkTransitiveDependenciesList` |

Root aggregates: `generateApiSurfaceAll`, `generateCompilerMetadataAll`, `listTransitiveDependenciesAll`.
API surface and transitive dependencies files are also regenerated after each Kotlin compilation, so a build may
leave changes in them: review and commit those diffs. The check tasks compare the files with `HEAD`, so intended
changes pass only after they are committed; if you leave the work uncommitted, failures limited to the intended
generated diffs are expected (review those diffs instead). CI runs `checkGeneratedFiles` on Linux, where the Apple
compiler metadata tasks are not registered; that metadata is checked by `checkKlibCompilerMetadataChangesAll`
in a separate macOS job.

`LICENSE-3rdparty.csv` at the repo root is verified by `./gradlew checkDependencyLicenses`. Add an entry for
each new third-party dependency, keeping the file in alphabetical order.

# Generated Models

`features/rum` and `features/logs` generate Kotlin models from JSON schemas at build time
(`json-schema-generator` plugin, `generate<Name>ModelsFromJson` tasks). The generated files land in
`build/generated/json2kotlin/`, together with generated mappings from the native Android
(`com.datadog.android.*.model`) and iOS (`cocoapods.*`) models to the common ones. **Do not edit them directly.**

- `features/rum/src/commonMain/json/rum` is cloned from the upstream
  [`rum-events-format`](https://github.com/DataDog/rum-events-format) repo by `./gradlew :features:rum:cloneRumSchema`
  and is overwritten on sync. Changes must be made upstream first, then synced. The generated mappings are
  compiled against the native SDK models, so a schema sync may require a native SDK version that ships the
  same schema.
- `features/logs/src/commonMain/json/log` is local and can be edited directly.

Only the native-to-common mapping is generated. The common-to-native mapping of mutable fields is written by
hand: after a schema update, check new `readOnly: false` properties and `additionalProperties` and update that
mapping (see the comment in `features/rum/build.gradle.kts`).

# Code Conventions

These are rules that reviewers repeatedly enforce. Follow them when writing code to avoid review round-trips,
and flag violations of them when reviewing a PR.

## Public API

- Keep everything `internal` unless it is meant for customers. Check the `api/*/apiSurface` diff for anything
  leaked by accident.
- Don't expose `Impl` classes or internal types in public signatures. Don't expose native SDK types in
  `commonMain`; they may appear only in platform source sets, in APIs that exist for platform interop (e.g. the
  `NSError` overloads in `features/logs/src/appleMain/kotlin/com/datadog/kmp/log/LoggerPlatformExt.kt`).
- Keep the API consistent across platforms and with the native SDKs: same names, defaults and semantics on
  Android and Apple. If a capability exists on one platform only, put the API in that platform's source set
  (`androidMain`, `appleMain`, `iosMain`) instead of a `commonMain` API that does nothing elsewhere.
  If you can't verify parity with `dd-sdk-android` and `dd-sdk-ios`, say so in the PR description as an open
  question.
- If a class is meant to be built via `Builder`, make its constructor `internal` or `private`. The same applies to
  types the SDK creates and hands to customers (events, results, states).
- Expose collections as Kotlin read-only types (`List`, `Map`).
- Return empty collections instead of nullable collections. Prefer a NoOp implementation over a nullable type.
  Avoid unneeded default arguments.
- Changing a public signature, nullability or a data class shape is a breaking change for both Android (JVM)
  and Apple (KLIB and exported framework) consumers. Flag it explicitly and prefer deprecate-and-alias.
- Avoid `data class` in public API unless needed (`copy` and `componentN` become part of the ABI).
- Drop words the scope already implies (e.g., no `Rum` prefix inside RUM APIs).

## Threading

- Don't create threads, executors or coroutine scopes in SDK code; the native SDKs own the threading model.
- Don't block the caller: SDK APIs may be called from the main thread on both platforms.
- Don't stack concurrency primitives. Declare `AtomicXXX` fields as `val`, not `var`.
- Don't add mutable state to `object`s or companion objects unless it mirrors global state of the native SDK
  (as `Datadog` does); prefer state owned by the instance that needs it.

## Logging and Errors

- Don't use `println`, `NSLog` or `android.util.Log` in SDK code. On Android, log through `InternalLogger` from
  `dd-sdk-android` (see `core/src/androidMain/kotlin/com/datadog/kmp/core/configuration/ConfigurationExt.kt`).
  The iOS SDK's internal logger is not accessible from Kotlin yet (RUM-11618), which is why that file's
  `appleMain` counterpart uses `println`; don't copy that workaround elsewhere without a reason.
- Never throw from SDK code paths that could reach the host app. Catch narrow exception types, not
  `Throwable` or `Exception`. A broad catch is acceptable only around customer-provided or third-party code
  (event mappers, listeners): suppress `TooGenericExceptionCaught` at that site and log the exception. Even
  there, never swallow fatal `Error`s. Don't create a bare `Throwable` or `Exception`; use a specific subclass.
- On Apple targets, a Kotlin exception that reaches Objective-C/Swift code terminates the app unless the function
  is annotated with `@Throws`; keep exceptions inside Kotlin.

## Static Analysis and Compatibility

- `MIN_SDK` is 23 (`tools/build-plugins/src/main/kotlin/com/datadog/build/ProjectConfig.kt`); the iOS and tvOS
  minimum version is 15.
- Place `@Suppress(...)` on the narrowest scope: the call site, not the whole method or class.
- Dependencies go in `gradle/libs.versions.toml`. Prefer `implementation` over `api`. Declare dependencies in
  the matching `kotlin { sourceSets { … } }` block (`commonMain`, `androidMain`, `appleMain`, test source sets).
- Bump native SDKs only through `datadog-android` / `datadog-ios` in `gradle/libs.versions.toml`; don't edit
  `NATIVE_SDK_VERSIONS.md` by hand.

## Code Style

- Use imports instead of fully qualified names.
- Order class members as: properties, overridden/interface methods, then private methods. Put `companion object`
  and nested `Builder` at the bottom. Mark the companion `internal` or `private` when possible.
- Don't add comments or KDoc that restate the code. Keep existing comments accurate when changing code.
- Names must be precise. For example, use `set` rather than `add` for a single value, and a singular noun for a
  holder of one item.
- Names and docs of time values state the clock base and the unit: epoch vs. boot or elapsed time, `Ns` vs. `Ms`.
- Prefer the type system: sealed classes or enums over strings, `T : Any` over `T?`.
- `when` over a sealed type or enum must be exhaustive, without an `else` branch, so that a new subtype fails to
  compile instead of being silently ignored. This includes mappings between common and native enums.
- Use named arguments when the meaning of a value isn't clear at the call site.
- Reuse an existing helper instead of writing another copy; extract a shared utility when a second copy appears.

# Testing

Before creating, editing, or reviewing test code, load and follow the project `sdk-tests` skill from
`ai/skills/sdk-tests/SKILL.md`.

# Branches and Commits

- Default branch for PRs: **`develop`**
- Branch name format: `<username>/<ticket>/<short-description>`, e.g. `nogorodnikov/rum-19824/lazy-test-task-config`.
- Commit messages and PR titles: follow the [`sdk-commit`](ai/skills/sdk-commit/SKILL.md) skill.

# Pull Requests

- Use the project PR template (`.github/PULL_REQUEST_TEMPLATE.md`).
- Keep PRs focused. Don't include unrelated refactors, formatting churn, debug leftovers, stray files or
  "may be needed later" code (unused properties, schemas, files).
- Don't edit `CHANGELOG.md` in feature PRs. It is updated only in release preparation PRs.
- When preparing a release changelog: tags reflect customer impact, not change size. Internal-only changes are
  `[MAINTENANCE]`, not `[IMPROVEMENT]`. Follow the existing entry format, `* [TAG] Description. See [#N](link)`,
  with a product prefix for feature-specific entries (e.g. `* [FEATURE] RUM: …`).
- When reviewing a release preparation PR (version bump and `CHANGELOG.md` update for a new release), review
  only the changes the PR itself makes. The release includes changes from PRs that were already reviewed and
  merged, so don't flag breaking changes or binary incompatibilities that come from them.

## Handoff to human review

Follow the [contributor handoff guidance](CONTRIBUTING.md#handoff-to-human-review)
and the additional agent requirements below when checking whether to prompt the
user to add `ready-for-human-review`. Check at these checkpoints:

- After opening a PR, pushing changes to it, or marking it ready for review.
- After replying to or resolving the remaining agentic feedback.
- When an agentic review finishes and you inspect its result.
- When the user asks about PR status or readiness for human review.

After opening a PR, pushing changes, or marking a draft ready, agents must allow
at least five minutes for asynchronous reviews to start before assessing
eligibility. Measure from the latest of these actions and restart the grace period
if the head changes. The grace period alone does not establish eligibility.

Prompt for the label only when all of these conditions are met:

- At least one agentic or bot review has completed against the current PR head. No feedback is not evidence that a review occurred; a review of an older revision does not qualify.
- No observed agentic reviews remain queued or running. If an expected review has not appeared, eligibility remains unconfirmed.
- All agentic feedback has been addressed or explicitly dispositioned with evidence, each feedback item has a posted reply, and all associated review threads are resolved, including outdated threads.

Refresh the current PR head, review activity, feedback, and thread status. Check
all pages of results; if retrieval fails or is incomplete, eligibility remains
unconfirmed. Local fixes, draft replies, or a clean bot summary alone are not
sufficient. Do not resolve unaddressed feedback solely to qualify for the label.

If eligible and the label is absent, ask: "All agentic feedback has been addressed.
Shall I add `ready-for-human-review`?" Apply the label only after user confirmation,
then recheck the current head and eligibility immediately before labelling. If the
label is already present, do not prompt or add it again.

If eligibility is unconfirmed, report what remains pending. If the task ends
before the grace period or reviews complete, report the deferred check; do not
claim readiness or imply that monitoring will continue without a separately
arranged follow-up.

This instruction does not independently authorise posting replies, resolving
threads, pushing, or merging; follow the applicable approval requirements for
those actions. Carry this rule into delegated PR work and PR automations.
