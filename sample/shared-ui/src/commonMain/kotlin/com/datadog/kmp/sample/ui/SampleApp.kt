/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sample.ui

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.datadog.kmp.sample.PlatformActions
import com.datadog.kmp.sample.SampleScreen
import com.datadog.kmp.sample.addViewOpenedAtAttribute

/**
 * Root of the sample application UI, shared between Android and iOS.
 *
 * @param platformActions native platform actions to display along with the shared ones.
 */
@Composable
fun SampleApp(platformActions: PlatformActions = PlatformActions.NONE) {
    SampleTheme {
        val navController = rememberNavController()
        RumViewTrackingEffect(navController, onViewStarted = ::addViewOpenedAtAttribute)

        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.run { destination.route }
        val isBackNavigationAvailable = currentRoute != null && currentRoute != SampleScreen.HOME.screenName

        Scaffold(
            topBar = {
                SampleTopBar(
                    title = currentRoute.orEmpty(),
                    isBackNavigationAvailable = isBackNavigationAvailable,
                    onNavigateBack = { navController.navigateUp() }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { contentPadding ->
            NavHost(
                navController = navController,
                startDestination = SampleScreen.HOME.screenName,
                modifier = Modifier.fillMaxSize().padding(contentPadding)
            ) {
                animatedComposable(SampleScreen.HOME) {
                    HomeScreen(onNavigate = { screen -> navController.navigate(screen.screenName) })
                }
                animatedComposable(SampleScreen.LOGS) { LogsScreen(platformActions.logs) }
                animatedComposable(SampleScreen.CRASH) { CrashScreen(platformActions.crash) }
                animatedComposable(SampleScreen.RUM) { RumScreen(platformActions.rum) }
                animatedComposable(SampleScreen.WEBVIEW) { TrackedWebView(Modifier.fillMaxSize()) }
            }
        }
    }
}

private const val TRANSITION_DURATION_MS = 300

private fun NavGraphBuilder.animatedComposable(
    screen: SampleScreen,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable(
        screen.screenName,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(TRANSITION_DURATION_MS)
            ) + fadeIn()
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth },
                animationSpec = tween(TRANSITION_DURATION_MS)
            ) + fadeOut()
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth },
                animationSpec = tween(TRANSITION_DURATION_MS)
            ) + fadeIn()
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(TRANSITION_DURATION_MS)
            ) + fadeOut()
        },
        content = content
    )
}
