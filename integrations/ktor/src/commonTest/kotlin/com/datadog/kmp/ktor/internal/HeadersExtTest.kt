/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.ktor.internal

import io.ktor.http.HeadersBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class HeadersExtTest {

    @Test
    fun `M parse baggage header W w3cBaggage`() {
        // https://www.w3.org/TR/baggage/#examples-of-http-headers
        // Given
        val headers = HeadersBuilder().apply {
            append(
                "baggage",
                "key1=value1;property1;property2, key2 = value2, key3=value3; propertyKey=propertyValue"
            )
            append("baggage", "anotherKey=anotherValue")
            append("baggage", "userId=alice,serverNode=DF%2028,isProduction=false")
        }

        // When
        val parsed = headers.w3cBaggage()

        // Then
        assertNotNull(parsed)
        assertEquals(3, parsed.size)
        val firstBaggage = parsed[0]
        val secondBaggage = parsed[1]
        val thirdBaggage = parsed[2]
        assertEquals(
            listOf(
                BaggageItem("key1", "value1", "property1;property2"),
                BaggageItem("key2", "value2"),
                BaggageItem("key3", "value3", "propertyKey=propertyValue")
            ),
            firstBaggage
        )
        assertEquals(
            listOf(BaggageItem("anotherKey", "anotherValue")),
            secondBaggage
        )
        assertEquals(
            listOf(
                BaggageItem("userId", "alice"),
                BaggageItem("serverNode", "DF%2028"),
                BaggageItem("isProduction", "false")
            ),
            thirdBaggage
        )
    }

    @Test
    fun `M parse baggage header W w3cBaggage + no baggage header`() {
        // Given
        val headers = HeadersBuilder()

        // When
        val parsed = headers.w3cBaggage()

        // Then
        assertNull(parsed)
    }

    @Test
    fun `M update existing baggage header W addToW3cBaggage + key is missing`() {
        // Given
        val fakeBaggageHeader1 = "key1=value1;property1;property2=value2,key2=value3;property3;property4=value4"
        val fakeBaggageHeader2 = "key3=value5;property5,key4=value6;property6"
        val headers = HeadersBuilder().apply {
            append("baggage", fakeBaggageHeader1)
            append("baggage", fakeBaggageHeader2)
        }

        // When + Then
        headers.addToW3cBaggage("newKey", "newValue")
        val baggageHeaders = headers.getAll("baggage")
        assertNotNull(baggageHeaders)
        assertEquals(2, baggageHeaders.size)
        assertEquals(fakeBaggageHeader1, baggageHeaders.first())
        assertEquals("$fakeBaggageHeader2,newKey=newValue", baggageHeaders.last())
    }

    @Test
    fun `M update existing baggage header W addToW3cBaggage + key is missing + empty baggage value`() {
        // Given
        val headers = HeadersBuilder().apply {
            append("baggage", "")
        }

        // When + Then
        headers.addToW3cBaggage("newKey", "newValue")
        val baggageHeaders = headers.getAll("baggage")
        assertNotNull(baggageHeaders)
        assertEquals(1, baggageHeaders.size)
        assertEquals("newKey=newValue", baggageHeaders.first())
    }

    @Test
    fun `M update existing baggage header W addToW3cBaggage + key exists`() {
        // Given
        val fakeBaggageHeader1 = "key1=value1;property1;property2=value2,key2=value3;property3;property4=value4"
        val fakeBaggageHeader2 = "key3=value5;property5,existingKey=oldValue;property1,key4=value6;property6"
        val headers = HeadersBuilder().apply {
            append("baggage", fakeBaggageHeader1)
            append("baggage", fakeBaggageHeader2)
        }

        // When + Then
        headers.addToW3cBaggage(
            "existingKey",
            "newValue"
        )
        val baggageHeaders = headers.getAll("baggage")
        assertNotNull(baggageHeaders)
        assertEquals(2, baggageHeaders.size)
        assertEquals(fakeBaggageHeader1, baggageHeaders.first())
        assertEquals(
            fakeBaggageHeader2.replace(
                "oldValue;property1",
                "newValue"
            ),
            baggageHeaders.last()
        )
    }
}
