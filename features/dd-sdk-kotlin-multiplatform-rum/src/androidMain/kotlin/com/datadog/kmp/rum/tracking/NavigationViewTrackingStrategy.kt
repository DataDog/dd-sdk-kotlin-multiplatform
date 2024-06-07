/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.tracking

import android.app.Activity
import android.content.Context
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavDestination
import com.datadog.android.Datadog
import com.datadog.android.rum.tracking.NavigationViewTrackingStrategy as NativeNavigationViewTrackingStrategy

/**
 * A [ViewTrackingStrategy] that will track [Fragment]s within a NavigationHost
 * as RUM Views.
 *
 * @param navigationViewId the id of the NavHost view within the hosting [Activity].
 * @param trackArguments whether to track navigation arguments
 * @param componentPredicate the predicate to keep/discard/rename the tracked [NavDestination]s
 */
class NavigationViewTrackingStrategy(
    @IdRes navigationViewId: Int,
    trackArguments: Boolean,
    componentPredicate: ComponentPredicate<NavDestination> = AcceptAllNavDestinations()
) : ViewTrackingStrategy {

    private val nativeDelegate = NativeNavigationViewTrackingStrategy(
        navigationViewId,
        trackArguments,
        componentPredicate.native
    )

    override fun register(context: Context) {
        nativeDelegate.register(Datadog.getInstance(), context)
    }

    override fun unregister(context: Context?) {
        nativeDelegate.unregister(context)
    }
}
