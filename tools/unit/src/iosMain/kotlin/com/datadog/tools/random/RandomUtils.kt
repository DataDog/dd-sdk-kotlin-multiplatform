/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

@file:Suppress("TooManyFunctions")

package com.datadog.tools.random

import kotlin.random.Random

fun randomBoolean(): Boolean = Random.nextBoolean()

fun randomFloat(from: Float = -Float.MAX_VALUE, until: Float = Float.MAX_VALUE): Float =
    Random.nextDouble(from.toDouble(), until.toDouble()).toFloat()

fun randomLong(from: Long = Long.MIN_VALUE, until: Long = Long.MAX_VALUE): Long =
    Random.nextLong(from, until)

fun randomInt(from: Int = Int.MIN_VALUE, until: Int = Int.MAX_VALUE): Int =
    Random.nextInt(from, until)

inline fun <reified T : Enum<T>> randomEnumValue(): T {
    val values = enumValues<T>()
    return values[Random.nextInt(values.size)]
}

fun exhaustiveAttributes(): Map<String, Any?> {
    return mapOf(
        "string-key" to "value",
        "long-key" to randomLong(),
        "float-key" to randomFloat(),
        "object-key" to Any(),
        "null-key" to null
    )
}

fun <T> nullable(value: T): T? = if (randomBoolean()) null else value

fun randomThrowable(): Throwable {
    return listOf(
        randomError(),
        randomException()
    ).randomElement()
}

fun randomError(): Error {
    val errorMessage = "fakeErrorMessage"
    return listOf(
        NotImplementedError(errorMessage),
        OutOfMemoryError(errorMessage)
    ).randomElement()
}

fun randomException(): Exception {
    val errorMessage = "fakeExceptionMessage"
    return listOf(
        IndexOutOfBoundsException(errorMessage),
        ArithmeticException(errorMessage),
        IllegalStateException(errorMessage),
        IndexOutOfBoundsException(errorMessage),
        NullPointerException(errorMessage),
        UnsupportedOperationException(errorMessage)
    ).randomElement()
}

fun <T> List<T>.randomElement(): T {
    val index = randomInt(from = 0, until = this.size)
    return this[index]
}