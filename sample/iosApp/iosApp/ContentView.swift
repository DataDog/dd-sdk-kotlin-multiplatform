/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

import SwiftUI
import sharedLib

struct ContentView: View {

    var body: some View {
        VStack {
            Button(action: UtilsKt.logInfo) {
                Text("Log info")
                    .padding()
                    .background(Color.blue)
                    .foregroundColor(.white)
                    .cornerRadius(8)
            }

            Button(action: UtilsKt.logErrorWithThrowable) {
                Text("Log error with Throwable")
                    .padding()
                    .background(Color.red)
                    .foregroundColor(.white)
                    .cornerRadius(8)
            }

            Button(action: logErrorWithError) {
                Text("Log error with Error")
                    .padding()
                    .background(Color.brown)
                    .foregroundColor(.white)
                    .cornerRadius(8)
            }
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
