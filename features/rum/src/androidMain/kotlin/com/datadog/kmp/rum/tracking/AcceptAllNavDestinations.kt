/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.tracking

import androidx.navigation.NavDestination
import com.datadog.android.rum.tracking.NavigationViewTrackingStrategy
import com.datadog.android.rum.tracking.AcceptAllNavDestinations as NativeAcceptAllNavDestinations

/**
 * A predefined [ComponentPredicate] which accepts all [NavDestination] to be tracked as a RUM View event.
 * This is the default behaviour for the [NavigationViewTrackingStrategy].
 */
class AcceptAllNavDestinations : ComponentPredicate<NavDestination> {

    private val nativeDelegate = NativeAcceptAllNavDestinations()

    override fun accept(component: NavDestination): Boolean = nativeDelegate.accept(component)

    override fun getViewName(component: NavDestination): String? = nativeDelegate.getViewName(component)
}
