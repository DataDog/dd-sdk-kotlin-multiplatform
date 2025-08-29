/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

@file:Suppress("MatchingDeclarationName")

package com.datadog.kmp.ktor.internal

import com.datadog.kmp.ktor.W3C_BAGGAGE_KEY
import io.ktor.http.HeadersBuilder

internal data class BaggageItem(val key: String, val value: String, val metadata: String? = null) {
    fun toHeaderString(): String {
        return if (metadata != null) {
            "$key=$value;$metadata"
        } else {
            "$key=$value"
        }
    }
}

internal fun HeadersBuilder.w3cBaggage(): List<List<BaggageItem>>? {
    // baggage value can be split in multiple headers
    val existingHeaders = getAll(W3C_BAGGAGE_KEY) ?: return null
    return existingHeaders
        .map {
            it.split(",")
                // note: very simple parsing, without edge-cases, may be not 100% compliant with the spec
                // it doesn't handle non-ASCII character encoding, because we are not going to read/modify such
                // anyway in our flow
                .filter { it.contains("=") }
                .map {
                    val keyValueDelimiterIndex = it.indexOf('=')
                    val key = it.substring(0, keyValueDelimiterIndex).trim()
                    val value = it.substring(keyValueDelimiterIndex + 1).trim()
                    val metaIndex = value.indexOf(";")
                    if (metaIndex != -1) {
                        val metadata = value.substring(metaIndex + 1).trim()
                        BaggageItem(key, value.substring(0, metaIndex), metadata)
                    } else {
                        BaggageItem(key, value)
                    }
                }
        }
}

internal fun HeadersBuilder.addToW3cBaggage(key: String, value: String) {
    val existingBaggage = w3cBaggage()
    if (existingBaggage == null) {
        append(W3C_BAGGAGE_KEY, "$key=$value")
    } else {
        remove(W3C_BAGGAGE_KEY)
        // https://www.w3.org/TR/baggage/#baggage-string
        // >> Uniqueness of keys between multiple list-members in a baggage-string is not guaranteed.
        // >> The order of duplicate entries SHOULD be preserved when mutating the list. Producers SHOULD try
        // >> to produce a baggage-string without any list-members which duplicate the key of another list member.
        val headersWithGivenKey =
            existingBaggage.filter { it.any { it.key == key } }
        val updatedBaggage = if (headersWithGivenKey.isNotEmpty()) {
            existingBaggage.map {
                it.map {
                    if (it.key == key) it.copy(value = value, metadata = null) else it
                }
            }
        } else {
            existingBaggage.toMutableList().apply {
                add(removeLastOrNull().orEmpty().toMutableList().apply { add(BaggageItem(key, value)) })
            }
        }
        updatedBaggage
            .forEach {
                append(
                    W3C_BAGGAGE_KEY,
                    it.joinToString(separator = ",") { it.toHeaderString() }
                )
            }
    }
}
