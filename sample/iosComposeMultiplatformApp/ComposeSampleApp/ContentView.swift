/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

import SwiftUI
import UIKit
import composeSampleLib

/// Hosts the Compose Multiplatform UI shared with Android.
internal struct ContentView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        return MainViewControllerKt.MainViewController(platformActions: NativeActions.all)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // nothing to update
    }
}
