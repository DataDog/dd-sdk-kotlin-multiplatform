/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.android.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.datadog.android.compose.ExperimentalTrackingApi
import com.datadog.android.compose.NavigationViewTrackingEffect
import com.datadog.android.sessionreplay.TextAndInputPrivacy
import com.datadog.android.sessionreplay.compose.sessionReplayTextAndInputPrivacy
import com.datadog.kmp.sample.CRASH_SCREEN_NAME
import com.datadog.kmp.sample.HOME_SCREEN_NAME
import com.datadog.kmp.sample.LOGS_SCREEN_NAME
import com.datadog.kmp.sample.RUM_SCREEN_NAME
import com.datadog.kmp.sample.WEBVIEW_SCREEN_NAME
import com.datadog.kmp.sample.WEB_VIEW_TRACKING_LOAD_URL
import com.datadog.kmp.sample.logErrorWithThrowable
import com.datadog.kmp.sample.logInfo
import com.datadog.kmp.sample.network.startGetRequest
import com.datadog.kmp.sample.network.startPostRequest
import com.datadog.kmp.sample.startWebViewTracking
import com.datadog.kmp.sample.trackAction
import kotlin.random.Random
import android.webkit.WebView as AndroidWebView

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalTrackingApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SampleApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController().apply {
                        NavigationViewTrackingEffect(navController = this)
                    }
                    NavHost(navController = navController, startDestination = HOME_SCREEN_NAME) {
                        animatedComposable(HOME_SCREEN_NAME) { HomeView(navController) }
                        animatedComposable(LOGS_SCREEN_NAME) { LoggingView() }
                        animatedComposable(CRASH_SCREEN_NAME) { CrashView() }
                        animatedComposable(RUM_SCREEN_NAME) { RumView() }
                        animatedComposable(WEBVIEW_SCREEN_NAME) { WebView() }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeView(navController: NavController) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 32.dp)
        ) {
            Button(
                onClick = { navController.navigate(LOGS_SCREEN_NAME) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
            ) {
                Text(text = "LOGS")
            }
            Button(
                onClick = { navController.navigate(CRASH_SCREEN_NAME) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text(text = "CRASH")
            }
            Button(
                onClick = { navController.navigate(RUM_SCREEN_NAME) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Brown)
            ) {
                Text(text = "RUM")
            }
            Button(
                onClick = { navController.navigate(WEBVIEW_SCREEN_NAME) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text(text = "WEBVIEW")
            }

            Text(
                "Session Replay Hidden Text",
                modifier = Modifier
                    .padding(top = 64.dp)
                    .sessionReplayTextAndInputPrivacy(TextAndInputPrivacy.MASK_ALL)
            )
        }
    }
}

@Composable
fun LoggingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 32.dp)
        ) {
            Button(
                onClick = {
                    trackAction("Log info")
                    logInfo()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
            ) {
                Text(text = "Log info")
            }
            Button(
                onClick = {
                    trackAction("Log error")
                    logErrorWithThrowable()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text(text = "Log error with Throwable")
            }
        }
    }
}

@Composable
fun CrashView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 32.dp)
        ) {
            Button(
                onClick = {
                    throw IllegalStateException("crash!")
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text(text = "Trigger Crash")
            }
        }
    }
}

@Composable
fun RumView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 32.dp)
        ) {
            Button(onClick = {
                trackAction("Start GET request")
                val url = if (Random.nextBoolean()) {
                    "https://httpbin.org/get"
                } else {
                    "https://httpbin.org/redirect-to?url=get"
                }
                startGetRequest(url)
            }, colors = ButtonDefaults.buttonColors(containerColor = Color.Orange)) {
                Text(text = "Trigger GET request")
            }

            Button(onClick = {
                trackAction("Start POST request")
                startPostRequest("https://httpbin.org/post", "Some payload")
            }, colors = ButtonDefaults.buttonColors(containerColor = Color.Orange)) {
                Text(text = "Trigger POST request")
            }

            Button(onClick = {
                trackAction("Start server error request")
                startGetRequest("https://httpbin.org/status/500")
            }, colors = ButtonDefaults.buttonColors(containerColor = Color.Orange)) {
                Text(text = "Trigger server error request")
            }

            Button(onClick = {
                trackAction("Start network error request")
                startGetRequest("https://some-domain.in-non-existing-zone")
            }, colors = ButtonDefaults.buttonColors(containerColor = Color.Orange)) {
                Text(text = "Trigger network error request")
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebView() {
    AndroidView(
        factory = { context ->
            AndroidWebView(context).apply {
                settings.javaScriptEnabled = true
                webViewClient = WebViewClient()

                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.setSupportZoom(true)
                startWebViewTracking(this)
            }
        },
        update = { webView ->
            webView.loadUrl(WEB_VIEW_TRACKING_LOAD_URL)
        }
    )
}

@Preview
@Composable
fun DefaultPreview() {
    SampleApplicationTheme {
        LoggingView()
    }
}

@Suppress("MagicNumber")
private fun NavGraphBuilder.animatedComposable(
    route: String,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    return composable(
        route,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(300)
            ) + fadeIn()
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth },
                animationSpec = tween(300)
            ) + fadeOut()
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth },
                animationSpec = tween(300)
            ) + fadeIn()
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(300)
            ) + fadeOut()
        },
        content = content
    )
}

@Suppress("MagicNumber")
@Stable
private val Color.Companion.Brown
    get() = Color(0xff996633)

@Suppress("MagicNumber")
@Stable
private val Color.Companion.Orange
    get() = Color(0xffffA500)
