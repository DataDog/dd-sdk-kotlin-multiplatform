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
            .onAppear {
                UtilsKt.reportAppFullyDisplayed()
                UtilsKt.addViewOpenedAtAttribute()
            }
        }
    }
}

internal struct LoggingView: View {
    var body: some View {
        ActionsView(actions: SampleActions.shared.logs + NativeActions.logs)
    }
}

internal struct CrashView: View {
    var body: some View {
        ActionsView(actions: SampleActions.shared.crash + NativeActions.crash)
    }
}

internal struct RumView: View {
    var body: some View {
        ActionsView(
            actions: SampleActions.shared.rumNetwork + NativeActions.rum,
            sectionTitle: SampleActions.shared.RUM_FEATURE_OPERATIONS_TITLE,
            sectionActions: SampleActions.shared.rumFeatureOperations
        )
    }
}

/// Buttons for the given scenarios. A RUM action is tracked for each of them by `SampleAction.perform()`.
internal struct ActionsView: View {
    let actions: [SampleAction]
    var sectionTitle: String? = nil
    var sectionActions: [SampleAction] = []

    var body: some View {
        VStack {
            ForEach(actions, id: \.actionName) { ActionButton(action: $0) }
            if let sectionTitle {
                Text(sectionTitle)
                    .padding()
            }
            ForEach(sectionActions, id: \.actionName) { ActionButton(action: $0) }
        }
        .padding()
        .onAppear {
            UtilsKt.addViewOpenedAtAttribute()
        }
    }
}

private struct ActionButton: View {
    let action: SampleAction

    var body: some View {
        Button(action: { action.perform() }) {
            Text(action.title)
                .padding()
                .background(action.color.background)
                .foregroundColor(action.color.foreground)
                .cornerRadius(8)
        }
    }
}

/// Scenarios which can only be implemented in Swift, displayed after the ones from `SampleActions`.
private enum NativeActions {
    static let logs = [
        SampleAction(actionName: "Log error with Error", color: .brown, block: logErrorWithError),
        SampleAction(actionName: "Log checked KMP exception", color: .orange, block: logCheckedKmpException)
    ]
    static let crash = [
        SampleAction(actionName: "Native crash", color: .black, block: { fatalError() })
    ]
    static let rum = [
        SampleAction(
            actionName: "Add error: checked KMP exception",
            color: .orange,
            block: addCheckedKmpExceptionAsRumError
        )
    ]
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
        .onAppear {
            UtilsKt.addViewOpenedAtAttribute()
        }
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

extension SampleActionColor {
    var background: Color {
        switch self {
        case .blue: return .blue
        case .red: return .red
        case .brown: return .brown
        case .orange: return .orange
        case .magenta: return .magenta
        case .teal: return .teal
        case .black: return .black
        default: return .gray
        }
    }

    var foreground: Color {
        return self == .orange ? .black : .white
    }
}
