/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.internal

/**
 * Internal flag to include binary images with an error with sent to Datadog.
 */
const val INCLUDE_BINARY_IMAGES: String = "_dd.error.include_binary_images"

/**
 * Internal flag to treat RUM error as crash.
 */
const val RUM_ERROR_IS_CRASH: String = "_dd.error.is_crash"

/**
 * Internal flag to treat Log error as crash.
 */
const val LOG_ERROR_IS_CRASH: String = "_dd.error_log.is_crash"

/**
 * Adds an internal flag to include binary images.
 *
 * **NOTE**: This is a part of internal API and shouldn't be used outside of the SDK classes.
 *
 * @param attributes attributes to process.
 * @return Attributes with a flag to include binary images.
 */
fun withIncludeBinaryImages(attributes: Map<String, Any?>): Map<String, Any?> {
    return attributes.toMutableMap().apply {
        put(INCLUDE_BINARY_IMAGES, true)
    }
}

/**
 * Removes key type.
 *
 * **NOTE**: This is a part of internal API and shouldn't be used outside of the SDK classes.
 *
 * @param K key type.
 * @param V value type.
 * @param attributes attributes to process.
 * @return Same attributes, but with erased key type.
 */
fun <K, V> eraseKeyType(attributes: Map<K, V>): Map<Any?, V> {
    // in reality in ObjC it is [String: Any], but KMP generates the
    // signature extraInfo: Map<Any?, *>, erasing String type,
    // so we have to do that
    @Suppress("UNCHECKED_CAST")
    return attributes as Map<Any?, V>
}
