---
name: sdk-feature-knowledge
description: >-
  External knowledge for developing, debugging, and reviewing dd-sdk-kotlin-multiplatform
  code: official Kotlin Multiplatform and platform docs the codebase does not own, plus
  example prompts pairing them with SDK tasks.
when_to_use: >-
  Developing, debugging, or reviewing code in an area that has a reference file listed below.
---

# dd-sdk-kotlin-multiplatform Feature Knowledge

For developing, debugging, and reviewing SDK modules: external references the codebase does
not own (platform docs, guides) for the technologies the SDK builds on, with example prompts
pairing them with SDK tasks. This is not a spec of the code: for how the SDK works, read the code.
For the behavior of a feature, read the native SDK the module delegates to
([`dd-sdk-android`](https://github.com/DataDog/dd-sdk-android), [`dd-sdk-ios`](https://github.com/DataDog/dd-sdk-ios)).

## Feature index

| Area | Modules | Reference |
|---|---|---|
| Kotlin Multiplatform source sets, `expect`/`actual`, Apple interop | all library modules | [references/kotlin-multiplatform.md](references/kotlin-multiplatform.md) |
| Logs, RUM, Session Replay, WebView, integrations | `features/*`, `integrations/*` | Not yet documented |

## Rules

- Add only external links (one line on why each matters) and SDK-oriented example prompts. Never restate code.
- Update a reference file when the external docs or links change, not when the code changes.
