/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.core.configuration

/**
 * Defines the policy when batching data together.
 * Smaller batches will means smaller but more network requests,
 * whereas larger batches will mean fewer but larger network requests.
 */
enum class BatchSize {

    /** Prefer small batches. **/
    SMALL,

    /** Prefer medium sized batches. **/
    MEDIUM,

    /** Prefer large batches. **/
    LARGE
}
