# Kotlin Multiplatform — external references

Official Kotlin and Android docs on the Kotlin Multiplatform mechanisms this SDK is built on:
the source set hierarchy, `expect`/`actual` declarations, Kotlin/Native interop with the
Objective-C/Swift `dd-sdk-ios` frameworks, and the Android KMP library plugin. The prompts below
pair these links with SDK tasks. For how the SDK uses them, read the code.

## Links

- [Source set hierarchy](https://kotlinlang.org/docs/multiplatform/multiplatform-hierarchy.html) — default hierarchy template (`commonMain`, `appleMain`, `iosMain`, `tvosMain`, …) and which code is visible where.
- [Expected and actual declarations](https://kotlinlang.org/docs/multiplatform/multiplatform-expect-actual.html) — rules for `expect`/`actual` functions, properties and classes.
- [Interoperability with Swift/Objective-C](https://kotlinlang.org/docs/native-objc-interop.html) — how Kotlin API is exported to Swift and how Objective-C API is imported, including exceptions and `@Throws`.
- [Interoperability with C](https://kotlinlang.org/docs/native-c-interop.html) and [definition files](https://kotlinlang.org/docs/native-definition-file.html) — cinterop and `.def` files used to bind the native frameworks.
- [Android Gradle library plugin for KMP](https://developer.android.com/kotlin/multiplatform/plugin) — the Android target configuration, including host tests (`androidHostTest`).
- [Test your multiplatform app](https://kotlinlang.org/docs/multiplatform/multiplatform-run-tests.html) — `kotlin.test` in common code and running tests per target.
- [Datadog Kotlin Multiplatform RUM docs](https://docs.datadoghq.com/real_user_monitoring/kotlin_multiplatform/) and [Logs docs](https://docs.datadoghq.com/logs/log_collection/kotlin_multiplatform/) — customer-facing setup, to keep API changes consistent with the public documentation.

## Example prompts

- **Developing:** "Read the *Source set hierarchy* and *Expected and actual declarations* pages above, then propose where a new API for `features/rum` should live (`commonMain` with `expect`/`actual`, or a platform source set) given that the capability exists only in `dd-sdk-android`."
- **Debugging:** "Read the *Interoperability with Swift/Objective-C* page above, then explain how this Kotlin exception can reach Swift code in `sample/appleApp` and how to keep it inside Kotlin."
- **Reviewing:** "Read the *Interoperability with Swift/Objective-C* page above and review this PR's new public API in `commonMain` for how it is exported to Swift: names, nullability, default arguments and generics."
