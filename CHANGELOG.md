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
