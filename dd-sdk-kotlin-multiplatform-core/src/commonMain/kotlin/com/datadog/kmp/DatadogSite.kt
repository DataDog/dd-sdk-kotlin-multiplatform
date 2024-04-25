/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp

/**
 * Defines the Datadog sites you can send tracked data to.
 */
enum class DatadogSite {

    /**
     *  The US1 site: [app.datadoghq.com](https://app.datadoghq.com).
     */
    US1,

    /**
     *  The US3 site: [us3.datadoghq.com](https://us3.datadoghq.com).
     */
    US3,

    /**
     *  The US5 site: [us5.datadoghq.com](https://us5.datadoghq.com).
     */
    US5,

    /**
     *  The EU1 site: [app.datadoghq.eu](https://app.datadoghq.eu).
     */
    EU1,

    /**
     *  The AP1 site: [ap1.datadoghq.com](https://ap1.datadoghq.com).
     */
    AP1,

    /**
     *  The US1_FED site (FedRAMP compatible): [app.ddog-gov.com](https://app.ddog-gov.com).
     */
    US1_FED
}
