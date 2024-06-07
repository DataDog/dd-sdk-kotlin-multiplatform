/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

@file:Suppress("DEPRECATION")

package com.datadog.kmp.rum.tracking

import android.app.Fragment
import com.datadog.android.rum.tracking.FragmentViewTrackingStrategy
import com.datadog.android.rum.tracking.AcceptAllDefaultFragment as NativeAcceptAllDefaultFragments

/**
 * A predefined [ComponentPredicate] which accepts all [Fragment] to be tracked as RUM View event.
 * This is the default behaviour for the [FragmentViewTrackingStrategy].
 */
class AcceptAllDefaultFragments : ComponentPredicate<Fragment> {

    private val nativeDelegate = NativeAcceptAllDefaultFragments()

    override fun accept(component: Fragment): Boolean = nativeDelegate.accept(component)

    override fun getViewName(component: Fragment): String? = nativeDelegate.getViewName(component)
}
