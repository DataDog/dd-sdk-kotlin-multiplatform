/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.tracking

import android.content.Context

/**
 * A TrackingStrategy dedicated to views tracking.
 */
interface ViewTrackingStrategy {
    /**
     * This method will register the tracking strategy to the current Context and SDK instance.
     * @param context as [Context]
     */
    fun register(context: Context)

    /**
     * This method will unregister the tracking strategy from the current Context.
     * @param context as [Context]
     */
    fun unregister(context: Context?)
}
