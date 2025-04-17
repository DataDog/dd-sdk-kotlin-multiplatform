# 1.1.0 / 2025-04-17

* [FEATURE] Add API to track anonymous user. See [#166](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/166)
* [IMPROVEMENT] Add `setUserInfo` API with mandatory user ID. See [#167](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/167)
* [MAINTENANCE] Script to automatically create/update `NATIVE_SDK_VERSIONS.md`. See [#164](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/164)
* [MAINTENANCE] Update Datadog iOS SDK to 2.26.0, Datadog Android SDK to 2.20.0. See [#165](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/165)

# 1.0.0 / 2025-03-03

* [MAINTENANCE] Add Android sample app name. See [#147](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/147)
* [MAINTENANCE] Update `github/codeql-action` to latest version. See [#152](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/152)
* [MAINTENANCE] Pin github actions to commit hash. See [#153](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/153)
* [MAINTENANCE] Update Ktor instrumentation API surface with proper signature of the `datadogKtorPlugin` overload. See [#146](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/146)
* [MAINTENANCE] Update iOS SDK to 2.23.0. See [#157](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/157)
* [MAINTENANCE] Update AGP version to 8.8.2. See [#158](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/158)

# 0.5.0 / 2025-02-04

* [FEATURE] Session Replay: Add Session Replay `startRecordingImmediately` configuration API. See [#129](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/129)
* [FEATURE] RUM: Support tracking Watchdog terminations on iOS/tvOS. See [#135](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/135)
* [FEATURE] Ktor instrumentation: Trace context injection control. See [#141](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/141)
* [FEATURE] Ktor 3 support. See [#143](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/143)
* [IMPROVEMENT] Core: Update additional properties handling from json schema. See [#118](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/118)
* [IMPROVEMENT] Supporting tests for core module. See [#133](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/133)
* [IMPROVEMENT] Support redirects in Ktor instrumentation. See [#136](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/136)
* [IMPROVEMENT] Ktor instrumentation: Rename `traceSamplingRate` to `traceSampleRate`. See [#137](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/137)
* [IMPROVEMENT] Add Ktor plugin initializer with default tracing header types. See [#138](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/138)
* [IMPROVEMENT] Ktor instrumentation: Use deterministic trace sampling. See [#139](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/139)
* [IMPROVEMENT] Ktor instrumentation: Use thread-safe random. See [#140](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/140)
* [IMPROVEMENT] Add unit tests for tracing headers injection in Ktor instrumentation. See [#142](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/142)
* [MAINTENANCE] Next dev iteration. See [#123](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/123)
* [MAINTENANCE] Update Datadog iOS SDK to 2.21.0. See [#127](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/127)
* [MAINTENANCE] Disable Gradle build cache for CodeQL job. See [#130](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/130)
* [MAINTENANCE] Update iOS SDK to 2.22.0, Android SDK to 2.17.0. See [#132](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/132)
* [MAINTENANCE] Attach Datadog Java Agent to test jobs. See [#134](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/134)
* [MAINTENANCE] Update Android SDK to 2.18.0, iOS SDK to 2.22.1. See [#144](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/144)

# 0.4.0 / 2024-12-09

* [FEATURE] Session Replay: Support fine-grained masking and privacy overrides. See [#112](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/112)
* [FEATURE] Support tvOS target. See [#115](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/115)
* [FEATURE] RUM: Add RUM Resource attributes provider for Ktor instrumentation. See [#116](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/116)
* [FEATURE] Core: Support proxy. See [#117](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/117)
* [IMPROVEMENT] Support mutable additional properties. See [#113](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/113)
* [IMPROVEMENT] Style Android sample app similar to iOS. See [#114](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/114)
* [IMPROVEMENT] Avoid using `HttpRequestData` for resource attributes collection in Ktor instrumentation. See [#119](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/119)
* [MAINTENANCE] Next dev iteration. See [#101](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/101)
* [MAINTENANCE] Switch to Android API 35. See [#102](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/102)
* [MAINTENANCE] Migrate Slack notifier to k8s runner. See [#104](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/104)
* [MAINTENANCE] Kotlin 2.0.21 & AGP 8.6.1. See [#105](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/105)
* [MAINTENANCE] Update transitive dependencies after Kotlin 2.0.21 update. See [#109](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/109)
* [MAINTENANCE] Update iOS SDK to 2.20.0, Android SDK to 2.16.0. See [#112](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/112)
* [MAINTENANCE] Update Gradle to 8.11.1, AGP to 8.7.3. See [#120](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/120)
* [MAINTENANCE] Fix flaky Ktor instrumentation tests for redirects. See [#121](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/121)

# 0.0.3 / 2024-10-10

* [FEATURE] Add Ktor network errors tracking. See [#92](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/92)
* [FEATURE] Report response size in Ktor instrumentation. See [#93](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/93)
* [FEATURE] Support wider domain matching for traced hosts. See [#99](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/99)
* [BUGFIX] Don't use `kotlinx.datetime` on Android. See [#97](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/97)
* [BUGFIX] Propagate tracing attributes to RUM. See [#98](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/98)
* [DOCS] Setup `CONTRIBUTING` docs, update `README`. See [#88](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/88)
* [DOCS] Apply editorial fixes. See [#91](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/91)
* [MAINTENANCE] Remove Github -> Gitlab git URL overwrite for macOS runner. See [#79](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/79)
* [MAINTENANCE] Next dev iteration. See [#78](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/78)
* [MAINTENANCE] Handle case when no Github -> Gitlab overwrite. See [#80](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/80)
* [MAINTENANCE] Add Slack notification for the release publishing. See [#83](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/83)
* [MAINTENANCE] Merge `release/0.0.2` branch into `develop` branch. See [#85](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/85)
* [MAINTENANCE] Remove git URL rewrite cleanup. See [#86](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/86)
* [MAINTENANCE] Lift `CrashReporter` linker workaround to the convention plugin. See [#87](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/87)
* [MAINTENANCE] Setup CodeQL check. See [#34](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/34)
* [MAINTENANCE] Escape comma in the licenses list. See [#89](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/89)
* [MAINTENANCE] Rename master to main branch in CodeQL config. See [#90](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/90)
* [MAINTENANCE] Update org name in the iOS sample app attributes. See [#95](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/95)

# 0.0.2 / 2024-09-17

* [FEATURE] Support RUM event mappers. See [#68](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/68)
* [FEATURE] Add Session Replay support. See [#74](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/74)
* [FEATURE] Support event mapping for Logs. See [#71](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/71)
* [IMPROVEMENT] Support event model generation from JSON schema. See [#59](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/59)
* [IMPROVEMENT] Add Log event JSON schema. See [#60](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/60)
* [IMPROVEMENT] Update Logs API surface with event model declaration. See [#61](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/61)
* [IMPROVEMENT] Generate mapping functions for mapping native models to common models. See [#66](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/66)
* [IMPROVEMENT] Handle nullable enum properties conversion in the ObjC RUM API to common enums. See [#67](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/67)
* [IMPROVEMENT] Provide fallback values for enums in case of native -> common models conversion. See [#70](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/70)
* [MAINTENANCE] Merge release `0.0.1` into `develop` branch. See [#56](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/56)
* [MAINTENANCE] Next dev iteration. See [#57](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/57)
* [MAINTENANCE] Update `com.datadoghq.dependency-license` plugin to version 0.3. See [#58](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/58)
* [MAINTENANCE] Remove Android SDK installation via brew from Gitlab CI definition. See [#63](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/63)
* [MAINTENANCE] Gradle 8.9 & Android SDK tools update. See [#62](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/62)
* [MAINTENANCE] Update Android SDK to 2.12.0. See [#64](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/64)
* [MAINTENANCE] Kotlin 2.0.20 and Mokkery 2.3.0. See [#65](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/65)
* [MAINTENANCE] AGP 8.6.0. See [#69](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/69)
* [MAINTENANCE] Setup Session Replay module. See [#72](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/72)
* [MAINTENANCE] Stop generating podspec for public modules. See [#73](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/73)
* [MAINTENANCE] Explicitly set `antlr-runtime` transitive dependency version. See [#76](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/76)
* [MAINTENANCE] Update iOS SDK to 2.17.0, Android SDK to 2.13.1. See [#75](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/75)

# 0.0.1 / 2024-07-19

* Initial release
