# Datadog Integration for Coil 3

## Getting Started

To include the Datadog integration for [Coil 3][1] in your Kotlin Multiplatform project, add the
following to your shared module's `build.gradle.kts` file.

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.datadoghq:dd-sdk-kotlin-multiplatform-rum:<latest-version>")
            implementation("com.datadoghq:dd-sdk-kotlin-multiplatform-coil3:<latest-version>")
        }
    }
}
```

**Note**: This integration is supported on Android and iOS. It is not supported on `tvOS`.

### Initial Setup

1. Set up RUM monitoring, see the dedicated [Datadog Kotlin Multiplatform RUM Collection documentation][2] to learn how.
2. Add `DatadogCoilRequestListener` to your Coil `ImageRequest` whenever you load an image:

```kotlin
AsyncImage(
    model = ImageRequest.Builder(LocalPlatformContext.current)
        .data("https://example.com/image.png")
        .listener(DatadogCoilRequestListener())
        .build(),
    contentDescription = null
)
```

When an image fails to load, a RUM Error event is sent. When available, the location of the image is added in the `request_path` attribute.

## Contributing

For details on contributing, read the
[Contributing Guide](../../CONTRIBUTING.md).

## License

[Apache License, v2.0](../../LICENSE)

[1]: https://coil-kt.github.io/coil/
[2]: https://docs.datadoghq.com/real_user_monitoring/kotlin_multiplatform/
