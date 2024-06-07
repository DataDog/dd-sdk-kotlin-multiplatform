/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.tracking

import androidx.fragment.app.Fragment
import com.datadog.android.rum.tracking.FragmentViewTrackingStrategy
import com.datadog.android.rum.tracking.AcceptAllSupportFragments as NativeAcceptAllSupportFragments

/**
 * A predefined [ComponentPredicate] which accepts all [Fragment] to be tracked as RUM View
 * event. This is the default behaviour of the [FragmentViewTrackingStrategy].
 */
class AcceptAllSupportFragments : ComponentPredicate<Fragment> {

    private val nativeDelegate = NativeAcceptAllSupportFragments()

    override fun accept(component: Fragment): Boolean = nativeDelegate.accept(component)

    override fun getViewName(component: Fragment): String? = nativeDelegate.getViewName(component)
}
