/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.coil3

/**
 * Extracts the request path from Coil request data types which are specific to the current platform
 * (for example `android.net.Uri` or `java.io.File` on Android).
 *
 * @param data the data of the Coil [coil3.request.ImageRequest].
 * @return the request path, or null if the data type is not supported.
 */
internal expect fun platformRequestPath(data: Any): String?
