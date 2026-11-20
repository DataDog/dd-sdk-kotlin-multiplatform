/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

import composeSampleLib
import DatadogRUM

/// Scenarios which can only be implemented in Swift. They are displayed in the shared Compose UI after the ones from
/// `SampleActions`, and a RUM action is tracked for each of them by `SampleAction.perform()`.
internal enum NativeActions {
    static let all = PlatformActions(
        logs: [
            SampleAction(actionName: "Log error with Error", color: .brown, block: logErrorWithError),
            SampleAction(actionName: "Log checked KMP exception", color: .orange, block: logCheckedKmpException)
        ],
        crash: [
            SampleAction(actionName: "Native crash", color: .black, block: { fatalError() })
        ],
        rum: [
            SampleAction(
                actionName: "Add error: checked KMP exception",
                color: .orange,
                block: addCheckedKmpExceptionAsRumError
            )
        ]
    )
}

private enum RuntimeError: Error {
    case error(message: String)
}

private func logErrorWithError() {
    do {
        throw RuntimeError.error(message: "Example error message")
    } catch {
        UtilsKt.applicationLogger.error(message: "Logging error with Error", error: error)
    }
}

private func logCheckedKmpException() {
    do {
        try UtilsKt.triggerCheckedException()
    } catch {
        UtilsKt.applicationLogger.error(message: "Caught KMP boundary error", error: error)
    }
}

private func addCheckedKmpExceptionAsRumError() {
    do {
        try UtilsKt.triggerCheckedException()
    } catch {
        RUMMonitor.shared().addError(error: error, source: RUMErrorSource.source)
    }
}
