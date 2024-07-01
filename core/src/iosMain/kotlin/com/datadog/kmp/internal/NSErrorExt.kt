/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.internal

import com.datadog.binary_images._dd_get_binary_images
import kotlinx.cinterop.get
import kotlinx.cinterop.toKString
import kotlinx.cinterop.useContents
import platform.Foundation.NSError
import platform.Foundation.NSLocalizedDescriptionKey
import kotlin.experimental.ExperimentalNativeApi

/**
 * Creates instance of [NSError] from a given [Throwable] instance with a provided stacktrace. If there is
 * a custom user message, it will be added to the error as well.
 *
 * **NOTE**: This is a part of internal API and shouldn't be used.
 *
 * @param throwable [Throwable] instance to create [NSError] from.
 * @param message Optional user-provided message.
 *
 */
fun createNSErrorFromThrowable(throwable: Throwable, message: String? = null): NSError {
    val throwableMessage = throwable.message
    val fullMessage: String = if (!message.isNullOrBlank() && !throwableMessage.isNullOrBlank()) {
        "${message}\n${throwable.message}"
    } else if (!message.isNullOrBlank()) {
        message
    } else if (!throwableMessage.isNullOrBlank()) {
        throwableMessage
    } else {
        "No message"
    }
    return KotlinThrowableBridgeError(fullMessage, throwable)
}

/**
 * Creates instance of [NSError] with a message attached.
 *
 * **NOTE**: This is a part of internal API and shouldn't be used.
 *
 * @param message message to attache to the error.
 */
fun createNSErrorFromMessage(message: String): NSError {
    return NSError.errorWithDomain("Error", 0, mapOf(NSLocalizedDescriptionKey to message))
}

private val ADDRESS_REGEX = Regex("^0x[0-9a-fA-F]{8,16}\$")

private fun binaryImages(): Map<String, ULong> = _dd_get_binary_images().useContents {
    val images = mutableMapOf<String, ULong>()
    for (i in 0 until count.toInt()) {
        val imagesPointer = this.images ?: continue
        val image = imagesPointer[i]
        val libName = image.path
            ?.toKString()
            ?.split("/")
            ?.lastOrNull()
        if (libName != null) {
            images[libName] = image.load_address
        }
    }
    images
}

// keep it aligned with iOS SDK expectations
// https://github.com/DataDog/dd-sdk-ios/blob/725e59b118935100c36fe90792c3cb2a26438fe2/DatadogInternal/Sources/Utils/DDError.swift#L40-L47
private class KotlinThrowableBridgeError(message: String, private val throwable: Throwable) : NSError(
    "KotlinException",
    0,
    mapOf(NSLocalizedDescriptionKey to message)
) {
    @OptIn(ExperimentalNativeApi::class)
    override fun description(): String {
        // TODO RUM-5160 Support nested exceptions
        // Currently it is not possible (maybe only include message chain, but not stacktrace chain), because iOS
        // stacktrace format doesn't have a concept of nested exceptions, so it cannot be supported by the backend
        val stacktrace = throwable.getStackTrace()
        val addresses = throwable.getStackTraceAddresses()
        if (stacktrace.size != addresses.size) return ""

        val binaryImages = binaryImages()
        return createDatadogStacktrace(stacktrace, addresses, binaryImages)
    }
}

private const val STACK_FRAME_MIN_PIECES_COUNT = 6

@OptIn(ExperimentalStdlibApi::class)
internal fun createDatadogStacktrace(
    kotlinNativeStacktrace: Array<String>,
    instructionAddresses: List<Long>,
    binaryImages: Map<String, ULong>
): String {
    return kotlinNativeStacktrace.mapIndexed { index, line ->
        // 1       iosApp   0x1041adf77   symbol + 135      (/sample/iosApp/iosApp/ContentView.swift:35:25)
        // index   name     instr_addr    symbol + offset   source_info
        // 0        1           2           3    4   5      6
        val pieces = line.split(" ").filter { it.isNotEmpty() }
        if (pieces.size < STACK_FRAME_MIN_PIECES_COUNT || pieces.firstOrNull()?.toIntOrNull() == null) {
            return@mapIndexed "$index ??? 0x0000000000000000 0x00000000 + 0"
        }

        // library name can contain spaces
        val libName = pieces.drop(1)
            .takeWhile { !ADDRESS_REGEX.matches(it) }
            .joinToString(" ")
        val instructionAddress = "0x${instructionAddresses[index].toHexString(HexFormat.Default)}"
        val loadAddress = binaryImages[libName] ?: ULong.MIN_VALUE
        val offset = instructionAddresses[index].toULong() - loadAddress
        "$index $libName $instructionAddress 0x${loadAddress.toHexString(HexFormat.Default)} + $offset"
    }
        .joinToString("\n")
}
