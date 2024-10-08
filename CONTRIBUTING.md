# Contributing

First of all, thanks for contributing!

This document provides some basic guidelines for contributing to this repository.
To propose improvements, feel free to submit a PR or open an issue.

**Note**: Datadog requires that all commits within this repository be signed, including those within external contribution PRs. Make sure you have followed GitHub's [Signing Commits](https://docs.github.com/en/authentication/managing-commit-signature-verification/signing-commits) guide before proposing a contribution. PRs without signed commits are not processed and may be rejected.
## Setup your developer Environment

To setup your environment, make sure you installed [Android Studio](https://developer.android.com/studio).

**Note**: You can also compile and develop using only an IDE of choice, e.g.: IntelliJ Idea, Vim, etc.

In addition, to be able to run the static analysis tools locally, you should run the `local-ci.sh` script locally as follows.

```shell
./local_ci.sh --setup
```

### Modules

This project hosts the following modules:

  - `core`: Main library implementing the core functionality of SDK (storage and upload of data, core APIs);
  - `features/***`: Set of libraries implementing Datadog products:
    - `features/logs`: Library to send logs to Datadog;
    - `features/rum`: Library to track user navigation and interaction;
    - `features/session-replay`: Library to capture the application window content;
    - `features/webview`: Library to forward logs and RUM events captured in a webview to be linked with the mobile session;
  - `integrations/***`: Set of libraries integrating Datadog products in third party libraries:
    - `integrations/ktor`: Lightweight library providing a bridge integration between Datadog SDK and [Ktor client](https://ktor.io/);
  - `tools/*`: Set of modules used to extend the tools we use in our workflow:
    - `tools/build-plugins`: Convention plugins to ease build configuration;
    - `tools/license`: Script to check the license header in all project source files;
    - `tools/lint`: Linter for Swift code based on [SwiftLint](https://github.com/realm/SwiftLint);
    - `tools/unit`: Utility library with code to help writing unit tests;
  - `sample/***`: Sample applications showcasing how to use the library features in production code;
    - `sample/android`: Sample mobile application for Android;
    - `sample/iosAp`: Sample mobile application for iOS;
    - `sample/shared`: Kotlin Multiplatform module containing shared logic for the sample apps above;

### Building the SDK

You can build the SDK using the following Gradle command:

```shell
./gradlew assemble
```

This command may take a long time, so you can also run the following to make the build faster:

```shell
./gradlew assembleDebug linkPodDebugFrameworkIosArm64
```

This command builds a debug variant for Android and debug variant of the framework for iOS arm64 target.

Alternatively, you can run unit tests to check if the code can be compiled.

### Running the tests

The whole project is covered by a set of static analysis tools, linters, and tests. To mimic the steps taken by our CI, you can run the `local_ci.sh` script:

```shell
# cleans the repo
./local_ci.sh --clean

# runs the static analysis
./local_ci.sh --analysis

# compiles all the different library modules and tools
./local_ci.sh --compile

# Runs the unit tests
./local_ci.sh --test
```

## Submitting Issues

Many great ideas for new features come from the community, and we'd be happy to
consider yours!

To share your request, you can open an [issue](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/issues/new?labels=enhancement&template=FeatureRequest.yml)
with the details about what you'd like to see. At a minimum, please provide:

 - The goal of the new feature;
 - A description of how it might be used or behave;
 - Links to any important resources (e.g. Github repos, websites, screenshots,
     specifications, diagrams).

## Found a bug?

For any urgent matters (such as outages) or issues concerning the Datadog service
or UI, [contact our support team](https://docs.datadoghq.com/help/) for direct assistance.

You may submit bug reports concerning the Datadog SDK for Kotlin Multiplatform by
[opening a Github issue](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/issues/new?labels=bug&template=BugReport.yml).
At a minimum, please provide:

 - A description of the problem
 - Steps to reproduce the problem
 - Expected behavior
 - Actual behavior
 - Errors (with stack traces) or warnings received
 - Any details you can share about your configuration, including:
    - Android API level or iOS version
    - Datadog SDK version
    - Versions of any other relevant dependencies (Ktor, ...)
    - Your ProGuard configuration (Android)
    - The list of Gradle plugins applied to your project

If at all possible, also provide:

 - Logs (from the tracer/application/agent) or other diagnostics
 - Screenshots, links, or other visual aids that are publicly accessible
 - Code sample or test that reproduces the problem
 - An explanation of what causes the bug and/or how it can be fixed

Reports that include rich details are better, and ones with code that reproduce
the bug are best.

## Have a patch?

We welcome code contributions to the library, which you can
[submit as a pull request](https://github.com/DataDog/dd-sdk-kotlin-multiplatform/pull/new/develop).
Before you submit a PR, make sure that you first create an issue to explain the
bug or the feature your patch covers, and make sure another issue or PR doesn't
already exist.

To create a pull request:

1. **Fork the repository** from https://github.com/DataDog/dd-sdk-kotlin-multuplatform.
2. **Make any changes** for your patch.
3. **Write tests** that demonstrate how the feature works or how the bug is fixed.
4. **Update any documentation**, especially for new features. It can be found either in the `docs` folder of this repository, or in [documentation repository](https://github.com/DataDog/documentation).
5. **Submit the pull request** from your fork back to this [repository](https://github.com/DataDog/dd-sdk-kotlin-multiplatform).


The pull request is run through our CI pipeline, and a project member will
review the changes with you. For a pull request to be accepted and merged, it must, at a minimum:

 - Have a stated goal and detailed description of the changes made
 - Include thorough test coverage and documentation, where applicable
 - Pass all tests and code quality checks (linting/coverage/benchmarks) on CI
 - Receive at least one approval from a project member with push permissions

Make sure that your code is clean and readable and that your commits are small and
atomic, with a proper commit message.

## Coding Conventions

### Code quality

Our code uses [Detekt](https://detekt.dev/) static analysis with a shared configuration, which is slightly
stricter than the default one. A Detekt check is run on every on every PR to ensure that all new code
follow this rule.
Current Detekt version: 1.23.4

### Code style

Our coding style is ensured by [KtLint](https://ktlint.github.io/), with the
default settings. A KtLint check is run on every PR to ensure that all new code
follow this rule.
Current KtLint version: 0.50.0

Classes should group their methods in folding regions named after the declaring
class. Private methods should be grouped in an `Internal` named folding region.
For example, a class inheriting from `Runnable` and `Observable` should use the
following regions.

```kotlin

class Foo : Observable(), Runnable {

    // region Foo

    fun fooSpecificMethod(){}

    // endregion

    // region Observable

    override fun addObserver(o: Observer?) {
        super.addObserver(o)
        doSomething()
    }

    // endregion

    // region Runnable

    override fun run() {}

    // endregion

    // region Internal

    private fun doSomething() {}

    // endregion
}

```

There is also a command that you can use to automatically format the code following the
required styling rules (requires `ktlint` installed on your machine):

```console
ktlint -F "**/*.kt" "**/*.kts" '!**/build/generated/**' '!**/build/kspCaches/**'
```

### #TestMatters

It is important to be sure that our library work properly in any scenario. All
non-trivial code must be tested. If you're not used to writing tests, you can
take a look at the `test` folder to get some ideas on how we write them at Datadog.

We use a variety of tools to help us write tests that are easy to read and maintain:

#### Android source set

 - [JUnit5 Jupiter](https://junit.org/junit5/): The test runner, which is similar to JUnit4.
 - [Mockito](https://site.mockito.org/): A mocking framework to decouple concerns in the Unit Tests.
 - [AssertJ](https://assertj.github.io/doc/): A framework to write fluent assertions.
 - [Elmyr](https://github.com/xgouchet/Elmyr): A framework to generate fake data in the Unit Tests.

#### Common and iOS source sets

 - [kotlin.test](https://kotlinlang.org/api/latest/kotlin.test/): The test runner and different assertions.
 - [Mokkery](https://github.com/lupuuss/Mokkery): A mocking framework.

### Test Conventions

Below are a set of naming conventions and coding style to use as guidance to ensure your test classes are readable.

#### Classes

The accepted convention is to use the name of the class under test, with the suffix `Test`.
For example, the test class corresponding to the class `Foo` must be named `FooTest`.

Some classes need to be created in the `test` sourceSets to integrate with our testing tools
(AssertJ, Elmyr, ...). Those classes must be placed in a package named
`{module_package}.tests.{test_library}`, and be named by combining the base class name and
the new class purpose.

E.g.:
 - A custom assertion class for class `Foo` in module `com.datadog.module` will be
    `com.datadog.module.tests.assertj.FooAssert`
- A custom forgery factory class for class `Foo` in module `com.datadog.module` will be
    `com.datadog.module.tests.elmyr.FooForgeryFactory`

#### Fields & Test Method parameters

Fields should appear in the following order, and be named as explained by these rules:

- The object(s) under test must be named from their class, and prefixed by `tested`.
    E.g.: `testedListener: Listener`, `testedHandler: Handler`.
- Stubbed objects (mocks with predefined behavior) must be named from their class (with an optional qualifier), and prefixed by `stub`.
    E.g.: `stubDataProvider: DataProvider`, `stubReader: Reader`.
- Mocked objects (mocks being verified) must be named from their class (with an optional qualifier), and prefixed by `mock`.
    E.g.: `mockListener: Listener`, `mockLogger: Logger`.
- Fixtures (data classes or primitives with no behavior) must be named from their class (with an optional qualifier), and prefixed by `fake`.
    E.g.: `fakeContext: Context`, `fakeApplicationId: UUID`, `fakeRequest: NetworkRequest`.
- Other fields can be named on case by case basis, but a few rules can still apply:
    - If the field is annotated by a JUnit 5 extension (e.g.: `@TempDir`), then it should be named after the extension (e.g.: `tempOutputDir`).

#### Test Methods

Test methods must follow the Given-When-Then principle, that is they must all consist of three steps:

- Given (optional): sets up the instance under test to be in the correct state.
- When (optional): performs an action — directly or indirectly — on the instance under test.
- Then (mandatory): performs any number of assertions on the instance under test's state, the mocks or output values. It must perform at least one assertion.

If present, these steps will always be intruded by one line comments, i.e.: `// Given`, `// When`, `// Then`.

Based on this principle, the test name should reflect the intent, and use the following pattern: `MUST expected behavior WHEN method() GIVEN context`.
To avoid being too verbose, `MUST` will be written `M`, and `WHEN` will be written `W`. The `context` part should be concise, and wrapped in curly braces to avoid duplicate names
(e.g.: `M create a span with info W intercept() {statusCode=5xx}`)

Parameters shall have simple local names reflecting their intent (see above), whether they use an `@Forgery` or `@Mock` annotation (or none).

Here's a test method following those conventions:

```kotlin
    @Test
    fun `M forward boolean attribute to handler W addAttribute()`(
        @StringForgery(StringForgeryType.ALPHABETICAL) fakeMessage : String,
        @StringForgery(StringForgeryType.ALPHABETICAL) fakeKey : String,
        @BoolForgery value : Boolean,
        @Mock mockLogHandler: InternalLogger
    ) {
        // Given
        testedLogger = Logger(mockLogHandler)

        // When
        testedLogger.addAttribute(fakeKey, value)
        testedLogger.v(fakeMessage)

        // Then
        verify(mockLogHandler)
            .handleLog(
                Log.VERBOSE,
                fakeMessage,
                null,
                mapOf(key to value),
                emptySet()
            )
    }
```

#### Test Utility Methods

Because we sometimes need to reuse some setup or assertions in our tests, we tend to write utility methods.
Those methods should be private (or internal in a dedicated class/file if they need to be shared across tests).

- `fun stubSomething(mock, [args])`: methods setting up a mock (or rarely a fake). These methods must be of Unit type, and only stub responses for the given mock.
- `fun forgeSomething([args]): T`: methods setting up a forgery or an instance of a concrete class. These methods must return the forged instance.
- `fun assertObjectMatchesCondition(object, [args])`: methods verifying that a given object matches a given condition. These methods must be of Unit type, and only call assertions with the AssertJ framework (or native assertions).
- `fun verifyMockMatchesState(mock, [args])`: methods verifying that a mock’s interaction. These methods must be of Unit type, and only call verifications with the Mockito framework.
- `fun setupSomething()`: method to setup a complex test (should only be used in the Given part of a test).

#### Clear vs Closed Box testing

Clear Box testing is an approach to testing where the test knows
the implementation details of the production code. It usually involves making a class property visible
in the test (via the `internal` keyword instead of `private`).

Closed Box testing on the contrary will only use `public` fields and
functions without checking the internal state of the object under test.

While both can be useful, relying too much on Clear Box testing will make maintenance more complex:

 - The tiniest change in the production code will make the test break.
 - Clear Box testing often leads to higher coupling and repeating the tested logic in the test class.
 - It focuses more on the way the object under test works, and less on the behavior and usage.

It is recommended to use Closed Box testing as much as possible.

#### Property Based Testing

To ensure that our tests cover the widest range of possible states and inputs, we use property based
testing thanks to the Elmyr library (only for Android source set). Given a unit under test, we must
make sure that the whole range of possible input is covered for all tests.
