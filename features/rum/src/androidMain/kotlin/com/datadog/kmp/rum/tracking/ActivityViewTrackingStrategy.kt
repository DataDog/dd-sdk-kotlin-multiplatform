/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.tracking

import android.app.Activity
import android.content.Context
import com.datadog.android.Datadog
import com.datadog.android.rum.tracking.ActivityViewTrackingStrategy as NativeActivityViewTrackingStrategy

/**
 * A [ViewTrackingStrategy] that will track [Activity] as RUM Views.
 *
 * Each activity's lifecycle will be monitored to start and stop RUM Views when relevant.
 * @param trackExtras whether to track the Activity's Intent information (extra attributes,
 * action, data URI)
 * @param componentPredicate to accept the Activities that will be taken into account as
 * valid RUM View events.
 */
class ActivityViewTrackingStrategy(
    trackExtras: Boolean,
    componentPredicate: ComponentPredicate<Activity> = AcceptAllActivities()
) : ViewTrackingStrategy {

    private val nativeDelegate = NativeActivityViewTrackingStrategy(
        trackExtras,
        componentPredicate.native
    )

    override fun register(context: Context) {
        nativeDelegate.register(Datadog.getInstance(), context)
    }

    override fun unregister(context: Context?) {
        nativeDelegate.unregister(context)
    }
}
