---
name: sdk-native-update
description: >-
  Native SDK update flow for dd-sdk-kotlin-multiplatform: bumps dd-sdk-android and/or dd-sdk-ios, analyzes
  their public API changes between the old and new tags (Android `**/api/apiSurface`, iOS `api-surface-objc`)
  and their RUM schemas, adds event model properties that both native SDKs now provide, and reports common
  API changes (to propose Jira tickets for) and platform-only changes. Use when asked to "update", "bump" or
  "upgrade" the native SDKs, Datadog Android SDK or Datadog iOS SDK, or to "analyze native API changes".
when_to_use: >-
  Bumping `datadog-android` / `datadog-ios` in gradle/libs.versions.toml, checking what a new native SDK
  release adds, or deciding which native changes the KMP layer should follow.
---

# sdk-native-update — Native SDK Update

Paths are relative to the repository root. The commands use the sibling clones `../dd-sdk-android`,
`../dd-sdk-ios` and `../rum-events-format`; if one is missing, ask the user where it is or clone it
(`git clone --filter=blob:none --no-checkout <url> <dir>`). Only run `git fetch`, `git diff`, `git show` and
`git grep` in those clones: never check out or change their working trees.

Past updates for reference: #359 (version bump only), #366 (model properties common to both SDKs), #273
(bump with native API changes that broke the build).

## 1. Ticket and branch

Follow the user's ship workflow (Jira ticket, then a branch off a fresh `develop`). Commit subject:
`RUM-XXXX: Update Datadog Android SDK to A.B.C, iOS SDK to X.Y.Z` (or the one SDK that changes).

## 2. Find the versions and the wrapped modules

Current versions, then the latest release tags:

```bash
grep -E '^datadog-(android|ios) =' gradle/libs.versions.toml
git -C ../dd-sdk-android fetch -q --tags origin && git -C ../dd-sdk-android tag -l '[0-9]*.[0-9]*.[0-9]*' --sort=-v:refname | head -1
git -C ../dd-sdk-ios fetch -q --tags origin && git -C ../dd-sdk-ios tag -l '[0-9]*.[0-9]*.[0-9]*' --sort=-v:refname | head -1
```

Native modules the KMP libraries wrap (Android catalog aliases and iOS frameworks); changes elsewhere are
informational only:

```bash
grep -rhoE 'libs\.datadog\.android\.[A-Za-z]+|framework\("[A-Za-z]+"\)' core/build.gradle.kts features/*/build.gradle.kts integrations/*/build.gradle.kts | sort -u
```

Map the aliases to module directories with `gradle/libs.versions.toml` (e.g. `datadog-android-rum` →
`features/dd-sdk-android-rum`, `datadog-android-core` → `dd-sdk-android-core`).

## 3. Read the API changes

Examples below use the RUM-19784 range (Android 3.14.1 → 3.16.0, iOS 3.18.0 → 3.21.0); substitute your
versions.

```bash
git -C ../dd-sdk-android diff --stat 3.14.1 3.16.0 -- '*/api/apiSurface'
git -C ../dd-sdk-android diff 3.14.1 3.16.0 -- dd-sdk-android-core/api/apiSurface features/dd-sdk-android-rum/api/apiSurface
git -C ../dd-sdk-ios diff 3.18.0 3.21.0 -- api-surface-objc
```

- Each hunk header names the last top-level declaration before the change (`@@ … @@ data class
  com.datadog.android.rum.model.ErrorEvent`, `@@ … @@ public class objc_RUMMonitor: NSObject`). If a hunk
  adds a new top-level declaration, the lines after it belong to the new one.
- Android: hunks under `com.datadog.android.*.model.*Event` are event models; skip them here and use step 4.
- iOS: `api-surface-objc` is one section per framework (`# API surface for DatadogRUM:`). Classes named
  `objc_RUM*Event*`, `objc_Telemetry*Event*` and `objc_LogEvent*` are event models (step 4).
- Skip `_*InternalProxy`, `internal` packages and `_internal_*` members unless KMP already uses them
  (`grep -rn "<name>" core features integrations --include=*.kt`).

Then pair the changes yourself. The same feature is often named differently on the two platforms (e.g. Android
`addErrorWithStacktrace` and iOS `addError(message:type:stack:)`), so pair by meaning, not by name. For each
API change in a wrapped module, search the other SDK's **whole** surface at its target tag, not only its diff,
since it may have shipped the counterpart earlier:

```bash
git -C ../dd-sdk-ios show 3.21.0:api-surface-objc | grep -n -i "<keyword>"
git -C ../dd-sdk-android grep -n -i "<keyword>" 3.16.0 -- '*/api/apiSurface'
```

Quote both signatures for every pair you report as common. If you're not sure two changes are the same
feature, say so. Read the native release notes or sources when the surface is ambiguous.

## 4. Find event model properties available in both SDKs

KMP has models for the RUM `view`, `action`, `error`, `resource` and `long_task` events
(`features/rum/src/commonMain/json/rum`, a hand-edited copy of the schema) and for logs
(`features/logs/src/commonMain/json/log/log-schema.json`). The native models come from:

- dd-sdk-android: its vendored schema, `features/dd-sdk-android-rum/src/main/json/rum/` at the tag.
- dd-sdk-ios: the rum-events-format commit in the `// Generated from …/tree/<sha>` line of
  `RUMDataModels.swift`:

```bash
git -C ../dd-sdk-ios show 3.18.0:DatadogInternal/Sources/Models/RUM/RUMDataModels.swift | grep -m1 -oE 'rum-events-format/tree/[0-9a-f]{40}'
git -C ../dd-sdk-ios show 3.21.0:DatadogInternal/Sources/Models/RUM/RUMDataModels.swift | grep -m1 -oE 'rum-events-format/tree/[0-9a-f]{40}'
git -C ../rum-events-format fetch -q origin 75aac05f07c331fffa8598919931106ed24bee63 e56d61f0016c507091c8093befa9e73c42037c70
```

The commits may not be on any branch of the clone, hence the fetch by SHA. Diff both sides:

```bash
git -C ../dd-sdk-android diff 3.14.1 3.16.0 -- features/dd-sdk-android-rum/src/main/json/rum
git -C ../rum-events-format diff 75aac05f07c331fffa8598919931106ed24bee63 e56d61f0016c507091c8093befa9e73c42037c70 -- schemas/rum
```

A property is a candidate when, at the new versions, it is in **both** native schemas and missing in KMP. It
doesn't have to be in both diffs: an addition on one side counts when the other side already had it. Check that
with the other side's full schema, then check KMP:

```bash
git -C ../rum-events-format show e56d61f0016c507091c8093befa9e73c42037c70:schemas/rum/error-schema.json | grep -n graphql
grep -rn '"graphql"' features/rum/src/commonMain/json/rum
```

In the RUM-19784 range, `session.has_timeseries` and `session.is_main_process` were added on both sides, while
`error.resource.graphql` and `resource.local_cache_hit` were added on Android only and iOS already had them.
#366 added the first three.

For every candidate, check in the schema:

- `"deprecated": true` — don't add it (#366 skipped `resource.local_cache_hit`).
- `"readOnly": false` on it or on a child (look into `$ref` files such as `_graphql-schema.json`) — it also needs
  the hand-written common → native mapping (step 6).
- The iOS objc surface has it (`objc_RUM<Event>Event…` property in `api-surface-objc`). The iOS generator can
  skip properties, and a property the objc model lacks can't be mapped on Apple targets.
- Ignore event types without a KMP model (`view_update`, vitals, timeseries, telemetry).
- New enum values usually need nothing: unknown native values map to the defaults in `defaultEnumValues`
  (`features/rum/build.gradle.kts`). Mention them in the report.
- Properties both SDKs had before this update but KMP lacks can be deliberate (`connectivity.interfaces` was
  removed from the KMP schema in RUM-5996). Don't add them unless the user asks.

For logs, compare the Android `com.datadog.android.log.model.LogEvent` part of
`features/dd-sdk-android-logs/api/apiSurface` and the iOS `objc_LogEvent*` classes with
`features/logs/src/commonMain/json/log/log-schema.json`.

## 5. Classify and act

| Finding | Action |
|---|---|
| Event model property in both SDKs, not deprecated (step 4) | Add it in this update PR (step 6). |
| Public API method/option in both SDKs (step 3) | Don't implement in the update PR. Propose a Jira ticket per feature to the user: title, both native signatures, the KMP modules affected. Create tickets only after the user confirms. |
| Change in one SDK only, in a wrapped module | Report it. Platform-only API goes to `androidMain`/`appleMain`/`iosMain` only if the user asks for it. |
| Removed or changed signature in a wrapped module | Check the KMP call sites; fix them in the update PR if the build breaks. |
| Modules KMP doesn't wrap, event types without a KMP model | List them in the report as informational. |

## 6. Bump the versions and add the common properties

Versions:

- `gradle/libs.versions.toml`: `datadog-android` and/or `datadog-ios`.
- For iOS, also the dd-sdk-ios package `version` (`kind = exactVersion`) in
  `sample/appleApp/appleApp.xcodeproj/project.pbxproj` and
  `sample/iosComposeMultiplatformApp/iosApp.xcodeproj/project.pbxproj`. An app linking an older dd-sdk-ios than
  the catalog version compiles but crashes at runtime with unrecognized selectors.
- Don't edit `NATIVE_SDK_VERSIONS.md`; it is updated at release time.

Then:

```bash
./gradlew :buildDatadogFrameworks
./gradlew listTransitiveDependenciesAll
```

Common model properties, as in #366 (RUM-19784):

1. Copy the property definitions from the Android vendored schema at the new tag into
   `features/rum/src/commonMain/json/rum/*.json` (or `log-schema.json`). Don't run `:features:rum:cloneRumSchema`:
   it pulls rum-events-format `master`, which has properties the native SDKs don't ship yet, and the generated
   native → common mappings then fail to compile.
2. `readOnly: false` properties (and `additionalProperties`) also need the common → native copy in
   `features/rum/src/androidMain/.../AndroidRumConfigurationBuilder.kt` and
   `features/rum/src/appleMain/.../AppleRumConfigurationBuilder.kt`. For logs, the copy-back is in
   `features/logs/src/androidMain/.../AndroidLogsConfigurationBuilder.kt` and
   `features/logs/src/appleMain/.../IOSLogsConfigurationBuilder.kt` (some log properties are mutable on Android
   only, see the `RUM-6088` TODO there). The Apple native → common log mapping is hand-written too, in
   `features/logs/src/appleMain/.../LogEventMappingExt.kt`, so a new log property also needs to be added there.
3. Update the forgeries in `features/rum/src/androidHostTest/kotlin/com/datadog/kmp/rum/utils/forge/` (load the
   `sdk-tests` skill).
4. Regenerate the API surface (`./gradlew :features:rum:generateApiSurface`) and review the diff: new constructor
   parameters of public model classes are expected.

## 7. Validate

Run the per-module checks from CLAUDE.md ("Before You Finish") for every module touched, at least:

```bash
./gradlew :features:rum:iosSimulatorArm64Test :core:iosSimulatorArm64Test
```

plus the Android host tests and `./gradlew checkGeneratedFiles`. A version bump touches every module through the
native dependency, so prefer `./gradlew unitTestAll` before opening the PR, then follow the `sdk-pr-prepare`
skill.

## 8. Report and PR

Report to the user:

- What was added to the KMP models in this PR, and what was skipped (deprecated, enum values).
- Common public API changes, with both signatures and a proposed Jira ticket for each.
- Android-only and iOS-only changes in wrapped modules.

PR description (template in `.github/PULL_REQUEST_TEMPLATE.md`): the versions, the KMP public API changes
("No KMP public API changes." when none), and the model properties added. Mention prerequisites such as a new
minimum Xcode version.

## Gotchas

- dd-sdk-android `apiSurface` lists parameter names only from 3.17.0 on (dd-sdk-android#4225). Up to 3.16.0,
  constructors and functions list types only, so a changed model constructor shows new parameters as
  `kotlin.Boolean? = null` and the schema diff (step 4) is the only way to name them.
- 3.17.0 also stops listing enum constructors. When the range crosses it (e.g. 3.16.0 → 3.17.0), almost every
  line of the Android `apiSurface` diff changes (`--word-diff` doesn't help). Strip the names from the newer
  side and diff per file, then ignore the removed enum `constructor(...)` lines:

  ```bash
  P=features/dd-sdk-android-rum/api/apiSurface
  diff <(git -C ../dd-sdk-android show "3.16.0:$P") <(git -C ../dd-sdk-android show "3.17.0:$P" | sed -E 's/([(]|, )(vararg )?[a-z][A-Za-z0-9_]*: /\1\2/g')
  ```

  Verified against the head of dd-sdk-android#4225 before 3.17.0 was released: for the RUM module, 3,207
  changed lines come down to `addLongTask` gaining a parameter, plus the removed enum constructors.
- Native behavior changes don't show up in any API surface. #359 had to fix a test because native
  `TimeseriesConfiguration` started storing the types in `collectTypes`: always build and run the tests.
- dd-sdk-ios releases can raise the minimum Xcode version (3.19.0 needed Xcode 26; CI support came first in
  #356). Check the dd-sdk-ios release notes before bumping.
