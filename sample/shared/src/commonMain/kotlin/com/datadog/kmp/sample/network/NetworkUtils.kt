/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sample.network

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
fun startGetRequest(url: String) {
    GlobalScope.launch {
        NetworkClient.get(
            url = url,
            onSuccess = { println("GET Success") },
            onFailure = { status, exception ->
                if (exception != null) {
                    println("GET failure")
                    exception.printStackTrace()
                } else {
                    println("GET responded with $status")
                }
            }
        )
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun startPostRequest(url: String, payload: String) {
    GlobalScope.launch {
        NetworkClient.post(
            url = url,
            payload = payload,
            onSuccess = { println("POST success") },
            onFailure = { status, exception ->
                if (exception != null) {
                    println("POST failure")
                    exception.printStackTrace()
                } else {
                    println("POST responded with $status")
                }
            }
        )
    }
}
