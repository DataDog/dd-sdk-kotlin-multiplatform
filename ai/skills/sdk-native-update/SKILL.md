---
name: sdk-native-update
description: >-
  Native SDK update flow for dd-sdk-kotlin-multiplatform: bumps dd-sdk-android and/or dd-sdk-ios, analyzes
  their public API changes between the old and new tags (Android `**/api/apiSurface`, iOS `api-surface-objc`)
  and the RUM schema, adds event model properties that both native SDKs now provide, and reports common
  API changes (to propose Jira tickets for) and platform-only changes. Use when asked to "update", "bump" or
  "upgrade" the native SDKs, Datadog Android SDK or Datadog iOS SDK, or to "analyze native API changes".
when_to_use: >-
  Bumping `datadog-android` / `datadog-ios` in gradle/libs.versions.toml, checking what a new native SDK
  release adds, or deciding which native changes the KMP layer should follow.
---

# sdk-native-update — Native SDK Update

Paths are relative to the repository root. The scripts live next to this file:

- `ai/skills/sdk-native-update/api_diff.py` — extracts the API surface diffs and the RUM schema gaps (Python 3
  standard library). It doesn't decide which Android and iOS changes are the same feature; you do (step 3).
- `ai/skills/sdk-native-update/bump_versions.sh` — sets the versions in the catalog and in both sample Xcode
  projects.

Past updates for reference: #359 (version bump only), #366 (model properties common to both SDKs), #273
(bump with native API changes that broke the build). To replay a past update, pass the versions and the KMP
commit to read the schema from, e.g. for #359/#366 (expected: `session.has_timeseries`,
`session.is_main_process`, `error.resource.graphql` with mutable `payload`/`variables`, and the deprecated
`resource.local_cache_hit`, which #366 skipped):

```bash
python3 ai/skills/sdk-native-update/api_diff.py --android-from 3.14.1 --android-to 3.16.0 \
  --ios-from 3.18.0 --ios-to 3.21.0 --kmp-ref 0a61ab3 --output build/native-sdk-update/replay-rum-19784.md
```

## 1. Ticket and branch

Follow the user's ship workflow (Jira ticket, then a branch off a fresh `develop`). Commit subject:
`RUM-XXXX: Update Datadog Android SDK to A.B.C, iOS SDK to X.Y.Z` (or the one SDK that changes).

## 2. Extract the API changes

```bash
python3 ai/skills/sdk-native-update/api_diff.py
```

Defaults: the "from" versions come from `gradle/libs.versions.toml`, the "to" versions are the latest
`x.y.z` tags. Override with `--android-to`, `--ios-to`, `--android-from`, `--ios-from`.

The native repositories are read from the sibling clones `../dd-sdk-android`, `../dd-sdk-ios` and
`../rum-events-format` when they exist; otherwise the script makes blobless clones in `build/native-sdk-update/`
(~50 MB, ~1 min). Override with `--android-repo`, `--ios-repo`, `--rum-events-format-repo`. In existing clones
the script only runs `git fetch` (tags, and the rum-events-format commits it needs); it never checks out or
touches a working tree.

The report is printed and written to `build/native-sdk-update/api-diff.md`:

1. **dd-sdk-android** — added, removed and changed declarations of every `**/api/apiSurface`, per module, split
   into API and event models (parameter names from 3.17.0 on, see Gotchas). Modules are labelled with the KMP modules that wrap them (read from the library
   build files); wrapped modules come first.
2. **dd-sdk-ios** — the same for `api-surface-objc`, per framework section.
3. **RUM schema gaps** — per KMP RUM event, properties present in both the dd-sdk-android vendored schema and
   the rum-events-format commit dd-sdk-ios generated its models from, but missing in
   `features/rum/src/commonMain/json/rum`. Lists the properties that became available with this update (with
   `deprecated`, mutable and "iOS objc ✗" notes), then, collapsed: new enum values, new `_dd` internals, older
   gaps, and Android-only / iOS-only properties.

## 3. Pair and classify the changes

The script doesn't pair Android and iOS API changes: the same feature is often named differently on the two
platforms (for example, Android `addErrorWithStacktrace` and iOS `addError(message:type:stack:)`). Do the
pairing yourself:

1. Go through the API changes of the **wrapped** modules in sections 1 and 2 (skip `_*InternalProxy`,
   `internal` packages and `_internal_*` members unless KMP already uses them).
2. For each change, look for the counterpart in the other SDK by meaning: the same option, parameter or
   callback, possibly with a different name or shape. Search the other SDK's whole surface at its target tag,
   not only its diff, since it may have shipped the feature earlier:
   `git -C ../dd-sdk-ios show 3.23.0:api-surface-objc | grep -n -i "<keyword>"`,
   `git -C ../dd-sdk-android grep -n -i "<keyword>" 3.16.0 -- '*/api/apiSurface'`. Read the native release notes
   or sources when the surface is ambiguous.
3. Quote both signatures for every pair you report as common. If you're not sure two changes are the same
   feature, say so.

| Finding | Action |
|---|---|
| Event model property in both SDKs, "became available with this update" (section 3) | Add it in this update PR (see step 5), unless it is `deprecated`. |
| New enum value in both SDKs | Usually skip: unknown native values map to the defaults in `defaultEnumValues` (`features/rum/build.gradle.kts`). Mention them in the report. |
| Public API method/option in both SDKs | Don't implement in the update PR. Propose a Jira ticket per feature to the user: title, both native signatures, the KMP modules affected. Create tickets only after the user confirms. |
| Change in one SDK only (in a wrapped module) | Report it. Platform-only API goes to `androidMain`/`appleMain`/`iosMain` only if the user asks for it. |
| Removed or changed signature in a wrapped module | Check the KMP call sites; fix them in the update PR if the build breaks. |
| Modules "not wrapped by KMP" | List them in the report as informational. |

Older schema gaps can be deliberate (for example, `connectivity.interfaces` was removed from the KMP schema in
RUM-5996). Don't add them unless the user asks.

## 4. Bump the versions

```bash
ai/skills/sdk-native-update/bump_versions.sh --ios 3.23.0   # and/or --android <version>
```

It updates `datadog-android` / `datadog-ios` in `gradle/libs.versions.toml` and the dd-sdk-ios `exactVersion`
of every sample Xcode project (`sample/appleApp`, `sample/iosComposeMultiplatformApp`). Don't edit
`NATIVE_SDK_VERSIONS.md`; it is updated at release time.

Then rebuild the native frameworks and regenerate the Android dependency lists:

```bash
./gradlew :buildDatadogFrameworks
./gradlew listTransitiveDependenciesAll
```

## 5. Add model properties common to both SDKs

As in #366 (RUM-19784):

1. Edit the KMP schema by hand. Copy the property definitions from the Android vendored schema
   (`features/dd-sdk-android-rum/src/main/json/rum/` at the new tag) into
   `features/rum/src/commonMain/json/rum/*.json`. Don't run `:features:rum:cloneRumSchema`: it pulls
   rum-events-format `master`, which has properties the native SDKs don't ship yet, and the generated
   native → common mappings then fail to compile. Logs: `features/logs/src/commonMain/json/log/log-schema.json`
   (the Apple mapping is hand-written in `features/logs/src/appleMain/.../LogEventMappingExt.kt`).
2. `readOnly: false` properties (and `additionalProperties`) also need the hand-written common → native copy in
   `features/rum/src/androidMain/.../AndroidRumConfigurationBuilder.kt` and
   `features/rum/src/appleMain/.../AppleRumConfigurationBuilder.kt`.
3. Update the forgeries in `features/rum/src/androidHostTest/kotlin/com/datadog/kmp/rum/utils/forge/` (load the
   `sdk-tests` skill).
4. Regenerate the API surface (`./gradlew :features:rum:generateApiSurface`) and review the diff: new
   constructor parameters of public model classes are expected.

## 6. Validate

Run the per-module checks from CLAUDE.md ("Before You Finish") for every module touched, at least:

```bash
./gradlew :features:rum:iosSimulatorArm64Test :core:iosSimulatorArm64Test
./gradlew :features:rum:testAndroidHostTest :core:testAndroidHostTest
./gradlew checkGeneratedFiles
```

A version bump touches every module through the native dependency, so prefer the aggregates
(`./gradlew unitTestAll`) before opening the PR, then follow the `sdk-pr-prepare` skill.

## 7. Report and PR

Report to the user, from the analysis:

- What was added to the KMP models in this PR.
- Common public API changes, with a proposed Jira ticket for each.
- Android-only and iOS-only changes in wrapped modules.

PR description (template in `.github/PULL_REQUEST_TEMPLATE.md`): the versions, the KMP public API changes
("No KMP public API changes." when none), and the model properties added. Mention prerequisites such as a new
minimum Xcode version.

## Gotchas

- Android `apiSurface` lists parameter names only from dd-sdk-android 3.17.0 on (dd-sdk-android#4225). Up to
  3.16.0, constructors and functions list types only, so a changed model constructor shows as "N → M params;
  added: `kotlin.Boolean? = null`" and section 3 is the only way to name the new properties. From 3.17.0 on,
  section 1 shows the names too (`added: hasTimeseries: kotlin.Boolean? = null`).
- When the range crosses 3.17.0 (e.g. 3.16.0 → 3.17.0), the script ignores the format change: it compares
  signatures without parameter names and skips the enum constructors that 3.17.0 no longer lists. Only real
  changes are reported, shown with the names of the newer version. A raw `git diff` of that range shows almost
  every line as changed.
- Native behavior changes don't show up in any API surface. #359 had to fix a test because native
  `TimeseriesConfiguration` started storing the types in `collectTypes`: always build and run the tests.
- iOS `api-surface-objc` has no nesting: members are indented under the last unindented declaration, and nested
  model types are flattened into names like `objc_RUMResourceEventResourceMessage`.
- The rum-events-format commit of dd-sdk-ios is read from the `// Generated from …/tree/<sha>` line of
  `RUMDataModels.swift`. That commit may not be on any branch of a local clone, so the script fetches it by SHA.
- dd-sdk-ios releases can raise the minimum Xcode version (3.19.0 needed Xcode 26; CI support came first in
  #356). Check the dd-sdk-ios release notes before bumping.
- Kotlin code is compiled against the catalog version. An older dd-sdk-ios linked by a sample app still compiles
  but crashes at runtime with unrecognized selectors, so always update both Xcode projects.
