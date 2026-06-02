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
                .filter { it.contains("=") }
                .map {
                    val keyValueDelimiterIndex = it.indexOf('=')
                    val key = it.substring(0, keyValueDelimiterIndex).trim()
                    val value = it.substring(keyValueDelimiterIndex + 1).trim()
                    val metaIndex = value.indexOf(";")
                    // don't decode existing items, we assume they are coming from elsewhere
                    // we will encode values added by addToW3cBaggage below
                    if (metaIndex != -1) {
                        val metadata = value.substring(metaIndex + 1).trim()
                        BaggageItem(key, value.take(metaIndex), metadata)
                    } else {
                        BaggageItem(key, value)
                    }
                }
        }
}

internal fun HeadersBuilder.addToW3cBaggage(key: String, value: String) {
    val existingBaggage = w3cBaggage()
    if (existingBaggage == null || existingBaggage.isEmpty()) {
        append(W3C_BAGGAGE_KEY, "$key=${encodeBaggageValue(value)}")
    } else {
        remove(W3C_BAGGAGE_KEY)
        // https://www.w3.org/TR/baggage/#baggage-string
        // >> Uniqueness of keys between multiple list-members in a baggage-string is not guaranteed.
        // >> The order of duplicate entries SHOULD be preserved when mutating the list. Producers SHOULD try
        // >> to produce a baggage-string without any list-members which duplicate the key of another list member.
        val headersWithGivenKey =
            existingBaggage.filter { baggage -> baggage.any { it.key == key } }
        val updatedBaggage = if (headersWithGivenKey.isNotEmpty()) {
            // we will deduplicate keys during the update
            var keyUpdated = false
            existingBaggage.map { baggage ->
                baggage.mapNotNull {
                    if (it.key == key) {
                        if (keyUpdated) {
                            null
                        } else {
                            keyUpdated = true
                            it.copy(
                                value = encodeBaggageValue(value),
                                metadata = null
                            )
                        }
                    } else {
                        it
                    }
                }
            }
        } else {
            existingBaggage.toMutableList().apply {
                add(
                    removeLastOrNull().orEmpty().toMutableList()
                        .apply { add(BaggageItem(key, encodeBaggageValue(value))) }
                )
            }
        }
        updatedBaggage
            .filter { it.isNotEmpty() }
            .forEach {
                append(
                    W3C_BAGGAGE_KEY,
                    it.joinToString(separator = ",") { it.toHeaderString() }
                )
            }
    }
}

/**
 * %x21 / %x23-2B / %x2D-3A / %x3C-5B / %x5D-7E
 * ; US-ASCII characters excluding CTLs,
 * ; whitespace, DQUOTE, comma, semicolon,
 * ; and backslash
 */
@Suppress("EndOfSentenceFormat")
private val ALLOWED_BAGGAGE_VALUE_CHARS =
    setOf('!') + ('#'..'+') + ('-'..':') + ('<'..'[') + (']'..'~')

@Suppress("MagicNumber")
private fun encodeBaggageValue(value: String): String {
    return value.encodeToByteArray().joinToString("") { byte ->
        val char = byte.toInt().toChar()
        if (char in ALLOWED_BAGGAGE_VALUE_CHARS) {
            char.toString()
        } else {
            "%" + byte.toUByte().toString(16).uppercase().padStart(2, '0')
        }
    }
}
