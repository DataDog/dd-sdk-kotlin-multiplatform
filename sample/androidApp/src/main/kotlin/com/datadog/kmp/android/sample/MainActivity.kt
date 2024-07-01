/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.android.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.datadog.kmp.sample.logErrorWithThrowable
import com.datadog.kmp.sample.logInfo
import com.datadog.kmp.sample.network.startGetRequest
import com.datadog.kmp.sample.network.startPostRequest
import com.datadog.kmp.sample.trackAction
import com.datadog.kmp.sample.trackView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SampleApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoggingView()
                }
            }
        }
    }
}

@Composable
fun LoggingView() {
    LaunchedEffect(null) {
        trackView("Logging view")
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 32.dp)
    ) {
        Button(onClick = {
            trackAction("Log info")
            logInfo()
        }) {
            Text(text = "Log info")
        }
        Button(onClick = {
            trackAction("Log error")
            logErrorWithThrowable()
        }) {
            Text(text = "Log error with Throwable")
        }
        Button(onClick = {
            throw IllegalStateException("crash!")
        }) {
            Text(text = "Crash")
        }

        Button(onClick = {
            trackAction("Start GET request")
            startGetRequest("https://httpbin.org/get")
        }) {
            Text(text = "Trigger GET request ")
        }

        Button(onClick = {
            trackAction("Start POST request")
            startPostRequest("https://httpbin.org/post", "Some payload")
        }) {
            Text(text = "Trigger POST request ")
        }
    }
}

@Preview
@Composable
fun DefaultPreview() {
    SampleApplicationTheme {
        LoggingView()
    }
}
