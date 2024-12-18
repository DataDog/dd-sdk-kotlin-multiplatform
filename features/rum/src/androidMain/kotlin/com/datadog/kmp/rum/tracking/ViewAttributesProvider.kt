/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.tracking

import android.view.View
import com.datadog.android.rum.RumAttributes

/**
 * Provides the extra attributes for the [View] as Map<String,Any?>.
 */
fun interface ViewAttributesProvider {

    /**
     * Add extra attributes to the default attributes Map.
     * @param view the [View].
     * @param attributes the default attributes Map. Usually this contains some default
     * attributes which are determined and added by the SDK. Please make sure you do not
     * override any of these reserved attributes.
     * @see [RumAttributes.ACTION_TARGET_RESOURCE_ID]
     * @see [RumAttributes.ACTION_TARGET_CLASS_NAME]
     */
    fun extractAttributes(view: View, attributes: MutableMap<String, Any?>)
}
