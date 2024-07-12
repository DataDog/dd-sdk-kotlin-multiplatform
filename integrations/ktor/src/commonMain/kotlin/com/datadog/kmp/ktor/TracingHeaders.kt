/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.ktor

// taken from DatadogHttpCodec
internal const val DATADOG_TAGS_KEY = "x-datadog-tags"
internal const val DATADOG_TRACE_ID_KEY = "x-datadog-trace-id"
internal const val DATADOG_SPAN_ID_KEY = "x-datadog-parent-id"
internal const val DATADOG_SAMPLING_PRIORITY_KEY = "x-datadog-sampling-priority"
internal const val DATADOG_DROP_SAMPLING_DECISION = "0"
internal const val DATADOG_KEEP_SAMPLING_DECISION = "1"
internal const val DATADOG_ORIGIN_KEY = "x-datadog-origin"
internal const val DATADOG_ORIGIN_RUM = "rum"
internal const val DATADOG_MOST_SIGNIFICANT_TRACE_ID_TAG = "_dd.p.tid"

// taken from B3HttpCodec
internal const val B3_HEADER_KEY = "b3"
internal const val B3_DROP_SAMPLING_DECISION = "0"

// taken from B3MHttpCodec
internal const val B3M_TRACE_ID_KEY = "X-B3-TraceId"
internal const val B3M_SPAN_ID_KEY = "X-B3-SpanId"
internal const val B3M_SAMPLING_PRIORITY_KEY = "X-B3-Sampled"
internal const val B3M_DROP_SAMPLING_DECISION = "0"
internal const val B3M_KEEP_SAMPLING_DECISION = "1"

// taken from W3CHttpCodec
internal const val W3C_TRACEPARENT_KEY = "traceparent"
internal const val W3C_TRACESTATE_KEY = "tracestate"

// https://www.w3.org/TR/trace-context/#traceparent-header
internal const val W3C_TRACEPARENT_DROP_SAMPLING_DECISION = "00-%s-%s-00"
internal const val W3C_TRACESTATE_DROP_SAMPLING_DECISION = "dd=p:%s;s:0"
internal const val W3C_SAMPLING_DECISION_INDEX = 3
internal const val W3C_TRACE_ID_LENGTH = 32
internal const val W3C_PARENT_ID_LENGTH = 16
internal const val W3C_SAMPLE_PRIORITY_ACCEPT = "01"
internal const val W3C_SAMPLE_PRIORITY_DROP = "01"

internal const val HEX_RADIX: Int = 16
