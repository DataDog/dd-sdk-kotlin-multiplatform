---
name: sdk-tests
description: >-
  Unit-test conventions for dd-sdk-kotlin-multiplatform. Use when creating, editing, or
  reviewing Kotlin unit tests: JVM host tests of the Android target (JUnit 5, Mockito,
  Elmyr Forge, AssertJ) and common/Apple tests (kotlin.test, Mokkery).
when_to_use: >-
  Writing new tests, adding regression coverage, fixing failing tests,
  or refactoring test fixtures and assertions.
paths:
  - "**/src/androidHostTest/**/*.kt"
  - "**/src/commonTest/**/*.kt"
  - "**/src/appleTest/**/*.kt"
  - "**/src/iosTest/**/*.kt"
  - "**/src/tvosTest/**/*.kt"
  - "**/*Test.kt"
---

# dd-sdk-kotlin-multiplatform Test Writing Guide

## Stacks per source set

Each KMP module has two test stacks. Use the one of the source set you are writing in, and
don't mix them.

| Source set | Runs on | Stack |
|---|---|---|
| `androidHostTest` | JVM (`:<module>:testAndroidHostTest`) | JUnit 5, Mockito + `mockito-kotlin`, Elmyr Forge, AssertJ |
| `commonTest` | JVM host and Apple simulators | `kotlin.test`, Mokkery, `com.datadog.tools.random` |
| `appleTest`, `iosTest`, `tvosTest` | Apple simulators, macOS only (`:<module>:iosSimulatorArm64Test`, `:<module>:tvosSimulatorArm64Test`) | `kotlin.test`, Mokkery, `com.datadog.tools.random`, `com.datadog.tools.concurrent` |

- `androidHostTest` tests verify that the Android `actual`s map to `dd-sdk-android` correctly, usually by mocking
  the native SDK types.
- `appleTest` / `iosTest` / `tvosTest` tests do the same for `dd-sdk-ios`.
- `commonTest` tests cover logic written in common code; they run on every target.
- Mokkery is applied per module (`alias(libs.plugins.mokkery)`): check the module's `build.gradle.kts` before
  using it; `core` doesn't apply it.
- Test dependencies are declared per source set in `kotlin { sourceSets { … } }`
  (`androidHostTest.dependencies { … }`, `commonTest.dependencies { … }`, `appleTest.dependencies { … }`).
  `tools/unit` (`projects.tools.unit`) provides the shared helpers.

## Naming convention

Backtick names follow the **M…W…** pattern on every source set:
- **M** = "Must" (expected outcome)
- **W** = "When" (action / condition)

```kotlin
@Test
fun `M set native debug W debug`() { /* ... */ }

@Test
fun `M call platform logger+debug W debug`() { /* ... */ }

@Test
fun `M clear user ID and account ID W stopInstance`() { /* ... */ }
```

**Method-name consistency is mandatory.** The method(s) in the `W` part of the test name must match the actual
methods called in the `// When` block. When renaming source methods, update test names to match.

```kotlin
// Good
fun `M return info and remove W remove()`() {
    // When
    val result = testedRegistry.remove(mockCall)
}

// Bad — test name says "unregister", real method is "remove"
fun `M return info and remove W unregister()`() {
    // When
    val result = testedRegistry.remove(mockCall)
}
```

## Test body — Given / When / Then

Always split the body with `// Given`, `// When`, `// Then` comments. A test whose action and assertion are a single
expression may use one `// When / Then` marker instead. Omit `// Given` when there is no setup beyond the set-up
method.

Keep the markers bare. Don't add explanations next to them or comments that restate the code; the test name and
the code already say it.

```kotlin
@Test
fun `M return native debug W debug`() {
    // Given
    val fakeDebug = randomBoolean()
    every { mockNativeRumMonitor.debug() } returns fakeDebug

    // When
    val isDebug = testedRumMonitorAdapter.debug

    // Then
    assertEquals(fakeDebug, isDebug)
}
```

## Naming of variables

- Mock vars → `mock` prefix: `mockNativeRumMonitor`, `mockPlatformLogger`
- Fake-data vars → `fake` prefix: `fakeMessage`, `fakeAttributes`
- Object under test → `tested` prefix; mocks with preset behavior that aren't verified → `stub` prefix

Import test framework symbols; never write fully qualified names inline (e.g. `org.mockito.kotlin.any()`,
`dev.mokkery.matcher.any()`).

## `androidHostTest`: JUnit 5, Mockito, Elmyr, AssertJ

### Test class structure

```kotlin
@Extensions(
    ExtendWith(MockitoExtension::class),
    ExtendWith(ForgeExtension::class)
)
@MockitoSettings(strictness = Strictness.LENIENT)
@ForgeConfiguration(Configurator::class)
internal class MyClassTest {
    // ...
}
```

Drop `MockitoExtension` and `@MockitoSettings` if the test has no mocks, and `ForgeExtension` /
`@ForgeConfiguration` if it forges nothing. Tests in `core` that need an Android `Context` also use
`ExtendWith(TestConfigurationExtension::class)` (`core/src/androidHostTest/kotlin/com/datadog/kmp/tools/unit/extensions/`).

New test classes are `internal`.

### Mocking

```kotlin
@Mock
lateinit var mockNativeRumMonitor: NativeRumMonitor
```

Declare mocks as `@Mock` fields (or `@Mock` test parameters), not with inline `mock()` calls. `mock { }` is fine for
building a stub with several behaviors.

Use `whenever(...) doReturn ...` (mockito-kotlin DSL) for stubbing. Use `mock { }` for 3+ stubs on the same mock:

```kotlin
// Good
mockBuilder = mock {
    on { setUrl(any()) } doReturn it
    on { setMethod(any()) } doReturn it
    on { build() } doReturn mockResult
}

// Bad — 3 separate whenever calls for the same mock
whenever(mockBuilder.setUrl(any())) doReturn mockBuilder
whenever(mockBuilder.setMethod(any())) doReturn mockBuilder
whenever(mockBuilder.build()) doReturn mockResult
```

Static entry points of `dd-sdk-android` (e.g. `com.datadog.android.Datadog`) are mocked with `mockStatic`, closed in
`@AfterEach` (see `core/src/androidHostTest/kotlin/com/datadog/kmp/DatadogTest.kt`).

### Verification

- Prefer `verifyNoInteractions(mock)` over `verify(mock, never())` when the only assertion is that the mock received
  no calls at all. Use `verify(mock, never()).method(...)` only to assert that a *specific* method was not called
  while others were.
- Prefer specific matchers over `any()`: use `eq()` / `isA()` when the expected value is known. Use `any()` only when
  the argument is genuinely irrelevant or can't be asserted (e.g. in `never()` checks). Never add a captor whose
  value is never read.
- Omit defaults: no `times(1)`, and no `eq()` when every matcher would be `eq` (pass the values directly). Use
  `never()` instead of `times(0)`.
- Capture arguments with `argumentCaptor` and assert individual fields. Never use `argThat { … }`: a failing
  `argThat` gives only a generic mismatch message.

```kotlin
// Good
argumentCaptor<ViewEvent> {
    verify(mockWriter).write(capture())
    assertThat(firstValue.view.name).isEqualTo(fakeName)
}

// Bad
verify(mockWriter).write(argThat { view.name == fakeName })
```

### Forge (random data)

```kotlin
@Test
fun `M set native debug W debug`(
    @BoolForgery fakeDebug: Boolean
) { /* ... */ }
```

- Annotations: `@StringForgery` (regex supported), `@BoolForgery`, `@IntForgery` (min/max), `@LongForgery`,
  `@Forgery` (any type with a registered factory), `@MapForgery`, `@AdvancedForgery`.
- In `@BeforeEach`, take a `forge: Forge` parameter: `forge.aStringMatching(regex)`, `forge.anElementFrom(...)`,
  `forge.aList { ... }`, `forge.aNullable { ... }`, `forge.getForgery<T>()`.
- Each module has one forge configurator, referenced by `@ForgeConfiguration(...)` in its existing tests (e.g.
  `features/rum/src/androidHostTest/kotlin/com/datadog/kmp/rum/utils/forge/Configurator.kt`, extending
  `BaseConfigurator` from `tools/unit`). When a test needs a new type, add a `ForgeryFactory` and register it there;
  don't create a new configurator.

### Assertions (AssertJ)

Use AssertJ `assertThat` for value assertions, and never the Kotlin `assert` keyword (a no-op without a JVM flag).
Use the most specific assertion: `isTrue()` / `isFalse()`, not `isEqualTo(true)`; `isEqualTo(list)` when order is
deterministic. Use JUnit's `assertThrows` / `assertDoesNotThrow` for exception contracts.

### Setup / teardown

```kotlin
@BeforeEach
fun `set up`() { /* ... */ }

@AfterEach
fun `tear down`() { /* ... */ }
```

### Parametrized and repeated tests

Prefer a single test with forged inputs over `@ParameterizedTest`. The suite runs on many branches and CI runs,
so random inputs cover the input space over time. This includes boolean or enum inputs: forge the value
(`@BoolForgery`, `@Forgery`) instead of a `@ValueSource` or `@EnumSource` pair. Keep `@ParameterizedTest` for the
rare case where a small, fixed set of specific values each defines distinct behavior.

## `commonTest` and Apple tests: kotlin.test, Mokkery

### Test class structure

```kotlin
class RumMonitorAdapterTest {

    private val mockNativeRumMonitor = mock<DDRumMonitorProxy>()

    private lateinit var testedRumMonitorAdapter: RumMonitorAdapter

    @BeforeTest
    fun `set up`() {
        testedRumMonitorAdapter = RumMonitorAdapter(mockNativeRumMonitor, mockRumSessionListener)
    }
}
```

- Use `kotlin.test` annotations (`@Test`, `@BeforeTest`, `@AfterTest`, `@Ignore`), not JUnit ones: these tests also
  compile for Kotlin/Native.
- Mokkery mocks are created with `mock<T>()`, stubbed with `every { … } returns …` / `everySuspend { … }` and
  verified with `verify { … }` / `verifySuspend { … }`. Each module configures its default `MockMode` in its
  `mokkery { }` block.
- Objective-C classes from `dd-sdk-ios` can't be mocked by Mokkery. Wrap the native class in an internal Kotlin
  interface that only forwards calls, and mock that interface (see
  `features/rum/src/appleMain/kotlin/com/datadog/kmp/rum/internal/DDRumMonitorProxy.kt`).

### Random data

There is no Elmyr outside the JVM. Use the helpers from `tools/unit`
(`tools/unit/src/commonMain/kotlin/com/datadog/tools/random/RandomUtils.kt`): `randomBoolean()`, `randomInt()`,
`randomLong()`, `randomEnumValue<T>()`, `nullable(value)`, `randomThrowable()`, `exhaustiveAttributes()`,
`List<T>.randomElement()`, or `kotlin.random.Random` directly. Forge incidental data whose exact value doesn't matter;
keep fixed literals when the value defines the tested behavior (boundary values, protocol constants).

### Assertions

Use `kotlin.test` assertions (`assertEquals(expected, actual)`, `assertNull`, `assertTrue`, `assertIs<T>`,
`assertFailsWith<T>`, `assertContains`). Put the expected value first. When several tests assert multiple fields of
the same model, add a small assertion helper instead of repeating field-by-field assertions (see
`integrations/ktor/src/commonTest/kotlin/com/datadog/kmp/ktor/test/HeadersAssert.kt`).

### Concurrency on Apple targets

To exercise code on a background queue, use `runOnBackgroundQueueAndWait { … }` and the `CountDownLatch` from
`tools/unit/src/appleMain/kotlin/com/datadog/tools/concurrent/`, with a bounded `await(timeoutMs)`.

## Fixtures and global state

- If a test must touch global state (e.g. `Datadog.currentUserId`), reset it in the tear-down method
  (`@AfterEach` / `@AfterTest`).
- Don't add mutable static fields to production code to make it testable (see Threading in `AGENTS.md`).

### Nullability checks

When a test needs to assert that a value is non-null before accessing its members, use `checkNotNull` (or
`assertNotNull`, which returns the non-null value, in `kotlin.test` tests). Place the check inside the `// Then`
block — it is an assertion, not setup.

```kotlin
// Good
// Then
val result = checkNotNull(testedInstance.getResult()) { "Expected a non-null result" }
assertThat(result.value).isEqualTo(fakeExpected)

// Bad — null check buried in Given/When, not surfaced as an assertion
val result = testedInstance.getResult()!!
assertThat(result.value).isEqualTo(fakeExpected)
```

### Asserting that no exception is thrown

If a test's implicit contract is "this call must not throw", make that contract explicit (`assertDoesNotThrow` in
JUnit tests). Never rely on the test simply completing without error.

## File location

Mirror the source structure in the matching test source set:
- Source: `src/androidMain/kotlin/com/datadog/kmp/rum/internal/RumMonitorAdapter.kt`
- Test: `src/androidHostTest/kotlin/com/datadog/kmp/rum/internal/RumMonitorAdapterTest.kt`
- Source: `src/appleMain/kotlin/com/datadog/kmp/rum/internal/RumMonitorAdapter.kt`
- Test: `src/appleTest/kotlin/com/datadog/kmp/rum/internal/RumMonitorAdapterTest.kt`

When the same class has tests on both platforms, keep the test names aligned so that coverage gaps between
Android and Apple are visible. Inside a test class, put private and internal helper methods at the bottom of the
file, after the tests.

## Best practices

1. **One behavior per test** — single assertion, or related ones testing one behavior
2. **Independent tests** — no shared mutable state between tests
3. **Meaningful fake data** — random values should be realistic
4. **Clean up** — reset global state, close static mocks
5. **Edge cases** — nulls, empty collections, boundary values
6. **Verify interactions** — ensure the expected native SDK calls happened, with the expected arguments
7. **No empty set-up methods** — drop them if there is nothing to set up
8. **DRY** — shared setup goes in the set-up method, not duplicated per test
9. **Concise tests** — aim for one line per Given/When/Then block; extract helpers. **Never sacrifice coverage for brevity**.
10. **No `!!` operator** — use `checkNotNull()` / `requireNotNull()` / `assertNotNull()` instead.
11. **No sleeps** — no `Thread.sleep`/`delay`; synchronize with latches and bounded timeouts. Don't use short timeouts
    that become flaky on a busy CI runner.
12. **Prove it can fail** — after writing a test, briefly break the code under test (or invert the assertion) and check
    it goes red.
13. **No low-value tests** — don't test data class getters or language features; remove tests covered by another test;
    merge near-duplicates into one test with random inputs. Drop artificial scenarios that can't happen in production.
14. **No reflection or static mocks for code we own** — make the member `internal` instead. Static mocks of the native
    SDK entry points are fine.
15. **Test the intended behavior** — derive expectations from what the code is supposed to do (and what the native SDK
    expects), not from what it currently does.
16. **Named arguments** — when the meaning of an argument isn't clear at the call site, use named arguments.
