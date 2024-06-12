/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.tracking

import android.app.Activity
import com.datadog.android.rum.tracking.AcceptAllActivities
import com.datadog.android.rum.tracking.ActivityViewTrackingStrategy

/**
 * A predefined [ComponentPredicate] which accepts all [Activity]  to be tracked as a RUM View event.
 * This is the default behaviour for the [ActivityViewTrackingStrategy].
 */
class AcceptAllActivities : ComponentPredicate<Activity> {

    private val nativeDelegate = AcceptAllActivities()

    override fun accept(component: Activity): Boolean = nativeDelegate.accept(component)

    override fun getViewName(component: Activity): String? = nativeDelegate.getViewName(component)
}
