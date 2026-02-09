/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.ktor

import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitNanosecond
import platform.Foundation.NSDate
import kotlin.random.Random

internal actual val RNG: Random = Random(
    NSCalendar.currentCalendar.component(
        unit = NSCalendarUnitNanosecond,
        fromDate = NSDate()
    )
)
