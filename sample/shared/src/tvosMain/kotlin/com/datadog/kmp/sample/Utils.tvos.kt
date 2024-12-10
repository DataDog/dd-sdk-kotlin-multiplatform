/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sample

import com.datadog.kmp.rum.configuration.RumConfiguration
import com.datadog.kmp.rum.configuration.trackUiKitActions

actual fun startWebViewTracking(webView: Any) {
    // WebView tracking is not available on tvOS
}

actual fun stopWebViewTracking(webView: Any) {
    // WebView tracking is not available on tvOS
}

internal actual fun initSessionReplay() {
    // not available for tvOS
}

internal actual fun setupUiKitActionsTracking(rumConfigurationBuilder: RumConfiguration.Builder) {
    // interface for UIKit actions tracking is different for tvOS and iOS
    rumConfigurationBuilder.trackUiKitActions()
}
