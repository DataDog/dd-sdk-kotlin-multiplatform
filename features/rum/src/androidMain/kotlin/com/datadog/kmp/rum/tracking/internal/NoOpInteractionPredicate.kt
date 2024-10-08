/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.tracking.internal

import com.datadog.kmp.rum.tracking.InteractionPredicate

internal class NoOpInteractionPredicate : InteractionPredicate {
    override fun getTargetName(target: Any): String? = null
}
