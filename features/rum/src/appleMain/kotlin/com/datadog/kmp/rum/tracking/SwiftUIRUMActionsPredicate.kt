/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.tracking

/**
 * The predicate deciding if the RUM Action should be recorded.
 */
fun interface SwiftUIRUMActionsPredicate {

    /**
     * Creates a [RumAction] instance from the given name of Swift UI component.
     *
     * @param componentName Swift UI component name.
     * @return RUM Action if it should be recorded, `null` otherwise.
     */
    fun createAction(componentName: String): RumAction?
}
