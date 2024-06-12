/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.tracking

import android.content.Context
import androidx.fragment.app.Fragment
import com.datadog.android.Datadog
import com.datadog.android.rum.tracking.FragmentViewTrackingStrategy
import com.datadog.android.rum.tracking.FragmentViewTrackingStrategy as NativeFragmentViewTrackingStrategy

/**
 * A [ViewTrackingStrategy] that will track [Fragment]s as RUM Views.
 *
 * Each fragment's lifecycle will be monitored to start and stop RUM Views when relevant.
 *
 * **Note**: This version of the [FragmentViewTrackingStrategy] is compatible with
 * the AndroidX Compat Library.
 *
 *  @param trackArguments whether we track Fragment arguments
 *  @param supportFragmentComponentPredicate to accept the Androidx Fragments
 *  that will be taken into account as valid RUM View events.
 *  @param defaultFragmentComponentPredicate to accept the default Android Fragments
 *  that will be taken into account as valid RUM View events.
 */
class FragmentViewTrackingStrategy(
    trackArguments: Boolean,
    supportFragmentComponentPredicate: ComponentPredicate<Fragment> =
        AcceptAllSupportFragments(),
    @Suppress("DEPRECATION")
    defaultFragmentComponentPredicate: ComponentPredicate<android.app.Fragment> =
        AcceptAllDefaultFragments()
) : ViewTrackingStrategy {

    private val nativeDelegate = NativeFragmentViewTrackingStrategy(
        trackArguments,
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
