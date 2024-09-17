/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.log.configuration

import com.datadog.kmp.log.configuration.internal.AndroidLogsConfigurationBuilder
import com.datadog.kmp.log.configuration.internal.PlatformLogsConfigurationBuilder

internal actual fun platformLogsConfigurationBuilder(): PlatformLogsConfigurationBuilder<Any> =
    AndroidLogsConfigurationBuilder()
