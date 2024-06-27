/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

import SwiftUI
import sharedLib
import DatadogRUM

struct ContentView: View {

    static let LOG_INFO_LABEL = "Log info"
    static let LOG_ERROR_WITH_THROWABLE_LABEL = "Log error with Throwable"
    static let LOG_ERROR_WITH_ERROR_LABEL = "Log error with Error"
    static let RUM_LOGS_CHECKED_KMP_ERROR_LABEL = "RUM + Logs: checked KMP exception"
    static let NATIVE_CRASH_LABEL = "Native crash"
    static let CRASH_VIA_UNCHECKED_KMP_LABEL = "Crash: unchecked KMP exception"

    var body: some View {
        VStack {
            Button(action: {
                UtilsKt.trackAction(actionName: ContentView.LOG_INFO_LABEL)
                UtilsKt.logInfo()
            }) {
                Text(ContentView.LOG_INFO_LABEL)
                    .padding()
                    .background(Color.blue)
                    .foregroundColor(.white)
                    .cornerRadius(8)
            }

            Button(action: {
                UtilsKt.trackAction(actionName: ContentView.LOG_ERROR_WITH_THROWABLE_LABEL)
                UtilsKt.logErrorWithThrowable()
            }) {
                Text(ContentView.LOG_ERROR_WITH_THROWABLE_LABEL)
                    .padding()
                    .background(Color.red)
                    .foregroundColor(.white)
                    .cornerRadius(8)
            }

            Button(action: {
                UtilsKt.trackAction(actionName: ContentView.LOG_ERROR_WITH_ERROR_LABEL)
                logErrorWithError()
            }) {
                Text(ContentView.LOG_ERROR_WITH_ERROR_LABEL)
                    .padding()
                    .background(Color.brown)
                    .foregroundColor(.white)
                    .cornerRadius(8)
            }

            Button(action: {
                UtilsKt.trackAction(actionName: ContentView.RUM_LOGS_CHECKED_KMP_ERROR_LABEL)
                do {
                    try UtilsKt.triggerCheckedException()
                } catch {
                    RUMMonitor.shared().addError(error: error, source: RUMErrorSource.source)
                    UtilsKt.applicationLogger.error(message: "Caught KMP boundary error", error: error)
                }
            }) {
                Text(ContentView.RUM_LOGS_CHECKED_KMP_ERROR_LABEL)
                    .padding()
                    .background(Color.orange)
                    .foregroundColor(.white)
                    .cornerRadius(8)
            }

            Button(action: {
                UtilsKt.trackAction(actionName: ContentView.NATIVE_CRASH_LABEL)
                fatalError()
            }) {
                Text(ContentView.NATIVE_CRASH_LABEL)
                    .padding()
                    .background(Color.black)
                    .foregroundColor(.white)
                    .cornerRadius(8)
            }

            Button(action: {
                UtilsKt.trackAction(actionName: ContentView.CRASH_VIA_UNCHECKED_KMP_LABEL)
                UtilsKt.triggerUncheckedException()
            }) {
                Text(ContentView.CRASH_VIA_UNCHECKED_KMP_LABEL)
                    .padding()
                    .background(Color.purple)
                    .foregroundColor(.white)
                    .cornerRadius(8)
            }
        }
        .onAppear {
            UtilsKt.trackView(viewName: "Logging view")
        }
        .padding()
    }
}

private enum RuntimeError : Error {
    case error(message: String)
}

private func logErrorWithError() {
    do {
        throw RuntimeError.error(message: "Example error message")
    } catch {
        // TODO RUM-4491 Make sure we are able to capture a stacktrace
        UtilsKt.applicationLogger.error(message: "Logging error with Error", error: error)
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
