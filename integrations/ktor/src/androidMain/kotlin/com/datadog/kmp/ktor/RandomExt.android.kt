/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.ktor

import java.security.SecureRandom
import kotlin.random.Random
import kotlin.random.asKotlinRandom

/**
 * Creates implementation of [Random] backed by [SecureRandom], which is thread-safe instead of default JVM platform
 * implementation.
 */
internal actual val RNG: Random = SecureRandom().asKotlinRandom()
