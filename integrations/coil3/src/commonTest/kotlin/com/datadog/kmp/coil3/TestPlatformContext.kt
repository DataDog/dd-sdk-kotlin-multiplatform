/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.coil3

import coil3.PlatformContext

/**
 * Provides a [PlatformContext] to build real Coil requests in tests.
 */
internal expect fun forgePlatformContext(): PlatformContext
