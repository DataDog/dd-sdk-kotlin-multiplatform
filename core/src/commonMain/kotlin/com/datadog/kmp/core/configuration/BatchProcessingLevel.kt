/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.core.configuration

/**
 * Defines the policy for sending the batches.
 * High level will mean that more data will be sent in a single upload cycle but more CPU and memory
 * will be used to process the data.
 * Low level will mean that less data will be sent in a single upload cycle but less CPU and memory
 * will be used to process the data.
 */
enum class BatchProcessingLevel {
    /**
     * Only 1 batch will be sent in a single upload cycle.
     */
    LOW,

    /**
     * 10 batches will be sent in a single upload cycle.
     */
    MEDIUM,

    /**
     * 100 batches will be sent in a single upload cycle.
     */
    HIGH
}
