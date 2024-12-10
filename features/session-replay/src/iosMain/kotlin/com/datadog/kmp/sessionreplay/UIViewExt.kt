/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sessionreplay

import cocoapods.DatadogSessionReplay.DDImagePrivacyLevelOverride
import cocoapods.DatadogSessionReplay.DDImagePrivacyLevelOverrideMaskAll
import cocoapods.DatadogSessionReplay.DDImagePrivacyLevelOverrideMaskNonBundledOnly
import cocoapods.DatadogSessionReplay.DDImagePrivacyLevelOverrideMaskNone
import cocoapods.DatadogSessionReplay.DDImagePrivacyLevelOverrideNone
import cocoapods.DatadogSessionReplay.DDTextAndInputPrivacyLevelOverride
import cocoapods.DatadogSessionReplay.DDTextAndInputPrivacyLevelOverrideMaskAll
import cocoapods.DatadogSessionReplay.DDTextAndInputPrivacyLevelOverrideMaskAllInputs
import cocoapods.DatadogSessionReplay.DDTextAndInputPrivacyLevelOverrideMaskSensitiveInputs
import cocoapods.DatadogSessionReplay.DDTextAndInputPrivacyLevelOverrideNone
import cocoapods.DatadogSessionReplay.DDTouchPrivacyLevelOverride
import cocoapods.DatadogSessionReplay.DDTouchPrivacyLevelOverrideHide
import cocoapods.DatadogSessionReplay.DDTouchPrivacyLevelOverrideNone
import cocoapods.DatadogSessionReplay.DDTouchPrivacyLevelOverrideShow
import cocoapods.DatadogSessionReplay.ddSessionReplayPrivacyOverrides
import com.datadog.kmp.sessionreplay.configuration.ImagePrivacy
import com.datadog.kmp.sessionreplay.configuration.TextAndInputPrivacy
import com.datadog.kmp.sessionreplay.configuration.TouchPrivacy
import platform.Foundation.NSNumber
import platform.Foundation.numberWithBool
import platform.UIKit.UIView

/**
 * Allows setting a view to be "hidden" in the hierarchy in Session Replay.
 * When hidden the view will be replaced with a placeholder in the replay and
 * no attempt will be made to record it's children.
 *
 * @param hide pass `true` to hide the view, or `false` to remove the override
 */
fun UIView.setSessionReplayHidden(hide: Boolean) {
    ddSessionReplayPrivacyOverrides().setHide(NSNumber.numberWithBool(hide))
}

/**
 * Allows overriding the image privacy for a view in Session Replay.
 *
 * @param privacy the new privacy level to use for the view
 * or null to remove the override.
 */
fun UIView.setSessionReplayImagePrivacy(privacy: ImagePrivacy?) {
    ddSessionReplayPrivacyOverrides().setImagePrivacy(privacy.nativeOverride)
}

/**
 * Allows overriding the text and input privacy for a view in Session Replay.
 *
 * @param privacy the new privacy level to use for the view
 * or null to remove the override.
 */
fun UIView.setSessionReplayTextAndInputPrivacy(privacy: TextAndInputPrivacy?) {
    ddSessionReplayPrivacyOverrides().setTextAndInputPrivacy(privacy.nativeOverride)
}

/**
 * Allows overriding the touch privacy for a view in Session Replay.
 *
 * @param privacy the new privacy level to use for the view
 * or null to remove the override.
 */
fun UIView.setSessionReplayTouchPrivacy(privacy: TouchPrivacy?) {
    ddSessionReplayPrivacyOverrides().setTouchPrivacy(privacy.nativeOverride)
}

private val ImagePrivacy?.nativeOverride: DDImagePrivacyLevelOverride
    get() = when (this) {
        ImagePrivacy.MASK_NONE -> DDImagePrivacyLevelOverrideMaskNone
        ImagePrivacy.MASK_LARGE_ONLY -> DDImagePrivacyLevelOverrideMaskNonBundledOnly
        ImagePrivacy.MASK_ALL -> DDImagePrivacyLevelOverrideMaskAll
        null -> DDImagePrivacyLevelOverrideNone
    }

private val TextAndInputPrivacy?.nativeOverride: DDTextAndInputPrivacyLevelOverride
    get() = when (this) {
        TextAndInputPrivacy.MASK_SENSITIVE_INPUTS -> DDTextAndInputPrivacyLevelOverrideMaskSensitiveInputs
        TextAndInputPrivacy.MASK_ALL_INPUTS -> DDTextAndInputPrivacyLevelOverrideMaskAllInputs
        TextAndInputPrivacy.MASK_ALL -> DDTextAndInputPrivacyLevelOverrideMaskAll
        null -> DDTextAndInputPrivacyLevelOverrideNone
    }

private val TouchPrivacy?.nativeOverride: DDTouchPrivacyLevelOverride
    get() = when (this) {
        TouchPrivacy.SHOW -> DDTouchPrivacyLevelOverrideShow
        TouchPrivacy.HIDE -> DDTouchPrivacyLevelOverrideHide
        null -> DDTouchPrivacyLevelOverrideNone
    }
