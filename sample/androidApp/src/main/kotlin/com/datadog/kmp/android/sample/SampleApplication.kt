/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.android.sample

import android.app.Application
import com.datadog.kmp.sample.initDatadog

class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initDatadog(this)
    }
}
