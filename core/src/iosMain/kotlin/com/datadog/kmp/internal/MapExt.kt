/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.internal

private const val INCLUDE_BINARY_IMAGES = "_dd.error.include_binary_images"

/**
 * Adds an internal flag to include binary images.
 *
 * **NOTE**: This is a part of internal API and shouldn't be used.
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
 * **NOTE**: This is a part of internal API and shouldn't be used.
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
