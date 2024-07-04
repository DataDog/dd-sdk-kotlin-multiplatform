/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.ktor

/**
 * Trace Id related to the resource loading. (Number)
 */
const val RUM_TRACE_ID: String = "_dd.trace_id"

/**
 * Span Id related to the resource loading. (Number)
 */
const val RUM_SPAN_ID: String = "_dd.span_id"

/**
 * Tracing Sample Rate for the resource tracking, between zero and one. (Number)
 */
const val RUM_RULE_PSR: String = "_dd.rule_psr"
