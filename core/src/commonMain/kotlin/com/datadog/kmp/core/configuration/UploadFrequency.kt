/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.core.configuration

/**
 * Defines the frequency at which batch upload are tried.
 */
enum class UploadFrequency {

    /** Try to upload batch data frequently. */
    FREQUENT,

    /** Try to upload batch data with a medium frequency. */
    AVERAGE,

    /** Try to upload batch data rarely. */
    RARE
}
