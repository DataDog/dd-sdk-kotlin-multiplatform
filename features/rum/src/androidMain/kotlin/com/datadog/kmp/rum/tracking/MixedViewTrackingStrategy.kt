/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.tracking

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import com.datadog.android.Datadog
import com.datadog.android.rum.tracking.ActivityViewTrackingStrategy
import com.datadog.android.rum.tracking.FragmentViewTrackingStrategy
import com.datadog.android.rum.tracking.MixedViewTrackingStrategy
import com.datadog.android.rum.tracking.MixedViewTrackingStrategy as NativeMixedViewTrackingStrategy

/**
 * A [ViewTrackingStrategy] that will track [Activity] and [Fragment] as RUM View Events.
 * This strategy will apply both the [ActivityViewTrackingStrategy]
 * and the [FragmentViewTrackingStrategy] and will remain for you to decide whether to exclude
 * some activities or fragments from tracking by providing an implementation for the right
 * predicate in the constructor arguments.
 * @see ActivityViewTrackingStrategy
 * @see FragmentViewTrackingStrategy
 * **Note**: This version of the [MixedViewTrackingStrategy] is compatible with
 * the AndroidX Compat Library.
 *
 * @param trackExtras whether we track Activity/Fragment arguments (extra attributes,
 *  * action, data URI)
 * @param componentPredicate to accept the Activities that will be taken into account as
 * valid RUM View events.
 * @param supportFragmentComponentPredicate to accept the Androidx Fragments
 * that will be taken into account as valid RUM View events.
 * @param defaultFragmentComponentPredicate to accept the default Android Fragments
 * that will be taken into account as valid RUM View events.
 */
class MixedViewTrackingStrategy(
    trackExtras: Boolean,
    componentPredicate: ComponentPredicate<Activity> = AcceptAllActivities(),
    supportFragmentComponentPredicate: ComponentPredicate<Fragment> =
        AcceptAllSupportFragments(),
    @Suppress("DEPRECATION")
    defaultFragmentComponentPredicate: ComponentPredicate<android.app.Fragment> =
        AcceptAllDefaultFragments()
) : ViewTrackingStrategy {

    private val nativeDelegate = NativeMixedViewTrackingStrategy(
        trackExtras,
        componentPredicate.native,
        supportFragmentComponentPredicate.native,
        defaultFragmentComponentPredicate.native
    )

    override fun register(context: Context) {
        nativeDelegate.register(Datadog.getInstance(), context)
    }

    override fun unregister(context: Context?) {
        nativeDelegate.unregister(context)
    }
}
