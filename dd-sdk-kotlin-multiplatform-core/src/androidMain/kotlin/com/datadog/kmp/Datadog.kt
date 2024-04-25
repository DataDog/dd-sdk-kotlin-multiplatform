/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp

import android.util.Log
import com.datadog.android.Datadog as DatadogAndroid

actual object Datadog {
    actual fun setVerbosity() {
        DatadogAndroid.setVerbosity(Log.DEBUG)
    }
}
