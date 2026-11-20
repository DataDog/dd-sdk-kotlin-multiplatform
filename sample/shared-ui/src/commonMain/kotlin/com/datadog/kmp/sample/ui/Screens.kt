/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sample.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.datadog.kmp.sample.SampleAction
import com.datadog.kmp.sample.SampleActionColor
import com.datadog.kmp.sample.SampleActions
import com.datadog.kmp.sample.SampleScreen
import com.datadog.kmp.sample.reportAppFullyDisplayed

@Composable
internal fun HomeScreen(onNavigate: (SampleScreen) -> Unit) {
    LaunchedEffect(Unit) {
        reportAppFullyDisplayed()
    }
    ScreenColumn {
        ColoredButton("LOGS", SampleActionColor.BLUE) { onNavigate(SampleScreen.LOGS) }
        ColoredButton("CRASH", SampleActionColor.RED) { onNavigate(SampleScreen.CRASH) }
        ColoredButton("RUM", SampleActionColor.BROWN) { onNavigate(SampleScreen.RUM) }
        ColoredButton("WEBVIEW", SampleActionColor.BLACK) { onNavigate(SampleScreen.WEBVIEW) }

        Text(
            "Session Replay Hidden Text",
            modifier = Modifier
                .padding(top = 64.dp)
                .maskAllTextInSessionReplay()
        )
    }
}

@Composable
internal fun LogsScreen(platformActions: List<SampleAction>) {
    ScreenColumn {
        ActionButtons(SampleActions.logs)
        ActionButtons(platformActions)
    }
}

@Composable
internal fun CrashScreen(platformActions: List<SampleAction>) {
    ScreenColumn {
        ActionButtons(SampleActions.crash)
        ActionButtons(platformActions)
    }
}

@Composable
internal fun RumScreen(platformActions: List<SampleAction>) {
    ScreenColumn {
        ActionButtons(SampleActions.rumNetwork)
        SectionTitle(SampleActions.RUM_FEATURE_OPERATIONS_TITLE)
        ActionButtons(SampleActions.rumFeatureOperations)
        ActionButtons(platformActions)
    }
}

@Composable
private fun ScreenColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        content = content
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(modifier = Modifier.padding(vertical = 16.dp), text = text)
}

@Composable
private fun ColoredButton(text: String, color: SampleActionColor, onClick: () -> Unit) {
    Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = Color(color.argb))) {
        Text(text = text)
    }
}

@Composable
private fun ActionButtons(actions: List<SampleAction>) {
    actions.forEach { action ->
        ColoredButton(action.title, action.color) { action.perform() }
    }
}

@Preview
@Composable
private fun LogsScreenPreview() {
    SampleTheme {
        LogsScreen(platformActions = listOf(SampleAction("Platform action", SampleActionColor.BLACK) {}))
    }
}
