/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.tracking

/**
 * A description of the RUM Action returned from the `UIKitRUMActionsPredicate`.
 *
 * @param name the RUM Action name, appearing as `Action NAME` in RUM Explorer. If no name is given, default one will be used.
 * @param attributes additional attributes to associate with the RUM Action.
 */
data class RumAction(internal val name: String, internal val attributes: Map<String, Any?>)
