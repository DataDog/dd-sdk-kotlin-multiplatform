/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

import SwiftUI
import sharedLib
import DatadogRUM
#if !os(tvOS)
import WebKit
#endif

internal struct ContentView: View {
    static let LOG_INFO_LABEL = "Log info"
    static let LOG_ERROR_WITH_THROWABLE_LABEL = "Log error with Throwable"
    static let LOG_ERROR_WITH_ERROR_LABEL = "Log error with Error"
    static let LOGS_CHECKED_KMP_ERROR_LABEL = "Log checked KMP exception"
    static let RUM_CHECKED_KMP_ERROR_LABEL = "Add error: checked KMP exception"
    static let NATIVE_CRASH_LABEL = "Native crash"
    static let CRASH_VIA_UNCHECKED_KMP_LABEL = "Crash: unchecked KMP exception"
    static let GET_REQUEST_LABEL = "Start GET request"
    static let POST_REQUEST_LABEL = "Start POST request"
    static let SERVER_ERROR_REQUEST_LABEL = "Start server error request"
    static let NETWORK_REQUEST_LABEL = "Start network request"

    var body: some View {
        NavigationView {
            VStack {
                NavigationLink(destination: LoggingView()) {
                    Text("LOGS")
                        .padding()
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(8)
                }

                NavigationLink(destination: CrashView()) {
                    Text("CRASH")
                        .padding()
                        .background(Color.red)
                        .foregroundColor(.white)
                        .cornerRadius(8)
                }

                NavigationLink(destination: RumView()) {
                    Text("RUM")
                        .padding()
                        .background(Color.brown)
                        .foregroundColor(.white)
                        .cornerRadius(8)
                }

                // WebView tracking is not available on tvOS
                #if !os(tvOS)
                NavigationLink(destination: WebTrackingView()) {
                    Text("WEBVIEW")
                        .padding()
                        .background(Color.black)
                        .foregroundColor(.white)
                        .cornerRadius(8)
                }
                #endif
            }
            .padding()
        }
    }
}

internal struct LoggingView: View {
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
                UtilsKt.trackAction(actionName: ContentView.LOGS_CHECKED_KMP_ERROR_LABEL)
                do {
                    try UtilsKt.triggerCheckedException()
                } catch {
                    UtilsKt.applicationLogger.error(message: "Caught KMP boundary error", error: error)
                }
            }) {
                Text(ContentView.LOGS_CHECKED_KMP_ERROR_LABEL)
                    .padding()
                    .background(Color.orange)
                    .foregroundColor(.white)
                    .cornerRadius(8)
            }
        }
        .padding()
    }
}

internal struct CrashView: View {
    var body: some View {
        VStack {
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
        .padding()
    }
}

internal struct RumView: View {
    var body: some View {
        VStack {
            Button(action: {
                UtilsKt.trackAction(actionName: ContentView.RUM_CHECKED_KMP_ERROR_LABEL)
                do {
                    try UtilsKt.triggerCheckedException()
                } catch {
                    RUMMonitor.shared().addError(error: error, source: RUMErrorSource.source)
                }
            }) {
                Text(ContentView.RUM_CHECKED_KMP_ERROR_LABEL)
                    .padding()
                    .background(Color.orange)
                    .foregroundColor(.white)
                    .cornerRadius(8)
            }

            Button(action: {
                let url = if (Bool.random()) {
                    "https://httpbin.org/get"
                } else {
                    "https://httpbin.org/redirect-to?url=get"
                }
                UtilsKt.trackAction(actionName: ContentView.GET_REQUEST_LABEL)
                NetworkUtilsKt.startGetRequest(url: url)
            }) {
                Text(ContentView.GET_REQUEST_LABEL)
                    .padding()
                    .background(Color.orange)
                    .foregroundColor(Color.black)
                    .cornerRadius(8)
            }

            Button(action: {
                UtilsKt.trackAction(actionName: ContentView.POST_REQUEST_LABEL)
                NetworkUtilsKt.startPostRequest(url: "https://httpbin.org/post", payload: "This is a payload")
            }) {
                Text(ContentView.POST_REQUEST_LABEL)
                    .padding()
                    .background(Color.orange)
                    .foregroundColor(Color.black)
                    .cornerRadius(8)
            }

            Button(action: {
                UtilsKt.trackAction(actionName: ContentView.SERVER_ERROR_REQUEST_LABEL)
                NetworkUtilsKt.startGetRequest(url: "https://httpbin.org/status/500")
            }) {
                Text(ContentView.SERVER_ERROR_REQUEST_LABEL)
                    .padding()
                    .background(Color.orange)
                    .foregroundColor(Color.black)
                    .cornerRadius(8)
            }

            Button(action: {
                UtilsKt.trackAction(actionName: ContentView.NETWORK_REQUEST_LABEL)
                NetworkUtilsKt.startGetRequest(url: "https://some-domain.in-non-existing-zone")
            }) {
                Text(ContentView.NETWORK_REQUEST_LABEL)
                    .padding()
                    .background(Color.orange)
                    .foregroundColor(Color.black)
                    .cornerRadius(8)
            }

            Text("Feature Operations")
                .padding()


            Button(action: {
                UtilsKt.trackAction(actionName: "Start Feature Operation")
                UtilsKt.startFeatureOperation()
            }) {
                Text("Start")
                    .padding()
                    .background(Color.magenta)
                    .foregroundColor(Color.white)
                    .cornerRadius(8)
            }

            Button(action: {
                UtilsKt.trackAction(actionName: "Stop Feature Operation successfully")
                UtilsKt.stopFeatureOperation()
            }) {
                Text("Stop successfully")
                    .padding()
                    .background(Color.teal)
                    .foregroundColor(Color.white)
                    .cornerRadius(8)
            }

            Button(action: {
                UtilsKt.trackAction(actionName: "Stop Feature Operation unsuccessfully")
                UtilsKt.failFeatureOperation()
            }) {
                Text("Stop unsuccessfully")
                    .padding()
                    .background(Color.red)
                    .foregroundColor(Color.white)
                    .cornerRadius(8)
            }
        }
        .padding()
    }
}

#if !os(tvOS)
internal struct WebTrackingView: View {
    var body: some View {
        let view = SwiftUIWebView()

        view.onAppear {
            Utils_iosKt.startWebViewTracking(webView: view.webView)
        }
        .onDisappear {
            Utils_iosKt.stopWebViewTracking(webView: view.webView)
        }
        .padding()
    }
}

struct SwiftUIWebView: UIViewRepresentable {
    let webView: WKWebView

    init() {
        webView = WKWebView(frame: .zero)
    }

    func makeUIView(context: Context) -> WKWebView {
        return webView
    }
    func updateUIView(_ uiView: WKWebView, context: Context) {
        webView.load(URLRequest(url: URL(string: UtilsKt.WEB_VIEW_TRACKING_LOAD_URL)!))
    }
}
#endif

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

internal struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}

extension Color {
    init(hex: Int) {
        let hex = UInt32(hex)
        let a = Double((hex >> 24) & 0xFF) / 255.0
        let r = Double((hex >> 16) & 0xFF) / 255.0
        let g = Double((hex >> 8) & 0xFF) / 255.0
        let b = Double(hex & 0xFF) / 255.0
        self.init(.sRGB, red: r, green: g, blue: b, opacity: a)
    }

    static let magenta: Color = .init(hex: 0xFFFF00FF)
    static let teal: Color = .init(hex: 0xFF03DAC5)
}
