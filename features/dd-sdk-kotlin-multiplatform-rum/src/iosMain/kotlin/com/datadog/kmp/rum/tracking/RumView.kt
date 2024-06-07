/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.tracking

/**
 * A description of the RUM View returned from the `UIKitRUMViewsPredicate`.
 *
 * @param name The RUM View name, appearing as `VIEW NAME` in RUM Explorer.
 * @param attributes Additional attributes to associate with the RUM View.
 */
data class RumView(internal val name: String, internal val attributes: Map<String, Any?>)
