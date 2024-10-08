/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.ktor

import kotlin.random.Random

// TODO RUM-6453 Documentation says that it is not thread-safe for JVM target, we need to handle this
internal val RNG = Random(seed())

internal expect fun seed(): Long
