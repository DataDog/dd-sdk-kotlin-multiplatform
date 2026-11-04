/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.coil3

import android.net.Uri
import java.io.File

internal actual fun platformRequestPath(data: Any): String? {
    return when (data) {
        is Uri -> data.path
        is File -> data.path
        else -> null
    }
}
