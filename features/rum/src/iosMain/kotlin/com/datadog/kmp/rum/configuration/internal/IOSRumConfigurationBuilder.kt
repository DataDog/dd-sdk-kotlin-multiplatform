/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.configuration.internal

import cocoapods.DatadogObjc.DDDefaultUIKitRUMActionsPredicate
import cocoapods.DatadogObjc.DDRUMAction
import cocoapods.DatadogObjc.DDRUMConfiguration
import cocoapods.DatadogObjc.DDRUMView
import cocoapods.DatadogObjc.DDRUMVitalsFrequency
import cocoapods.DatadogObjc.DDRUMVitalsFrequencyAverage
import cocoapods.DatadogObjc.DDRUMVitalsFrequencyFrequent
import cocoapods.DatadogObjc.DDRUMVitalsFrequencyNever
import cocoapods.DatadogObjc.DDRUMVitalsFrequencyRare
import cocoapods.DatadogObjc.DDUIKitRUMViewsPredicateProtocol
import com.datadog.kmp.rum.configuration.RumSessionListener
import com.datadog.kmp.rum.configuration.VitalsUpdateFrequency
import com.datadog.kmp.rum.tracking.DefaultUIKitRUMActionsPredicate
import com.datadog.kmp.rum.tracking.DefaultUIKitRUMViewsPredicate
import com.datadog.kmp.rum.tracking.UIKitRUMActionsPredicate
import com.datadog.kmp.rum.tracking.UIKitRUMViewsPredicate
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.darwin.NSObject

internal class IOSRumConfigurationBuilder : PlatformRumConfigurationBuilder<DDRUMConfiguration> {

    private val nativeConfiguration: DDRUMConfiguration

    internal constructor(applicationId: String) : this(DDRUMConfiguration(applicationId))

    internal constructor(nativeConfiguration: DDRUMConfiguration) {
        this.nativeConfiguration = nativeConfiguration
    }

    override fun setSessionSampleRate(sampleRate: Float) {
        nativeConfiguration.setSessionSampleRate(sampleRate)
    }

    override fun setTelemetrySampleRate(sampleRate: Float) {
        nativeConfiguration.setTelemetrySampleRate(sampleRate)
    }

    override fun trackLongTasks(longTaskThresholdMs: Long) {
        // iOS takes this parameter in seconds
        nativeConfiguration.setLongTaskThreshold(longTaskThresholdMs.toDouble() / MILLISECONDS_IN_SECOND)
    }

    override fun trackBackgroundEvents(enabled: Boolean) {
        nativeConfiguration.setTrackBackgroundEvents(enabled)
    }

    override fun trackFrustrations(enabled: Boolean) {
        nativeConfiguration.setTrackFrustrations(enabled)
    }

    override fun setVitalsUpdateFrequency(frequency: VitalsUpdateFrequency) {
        nativeConfiguration.setVitalsUpdateFrequency(frequency.native)
    }

    override fun setSessionListener(sessionListener: RumSessionListener) {
        nativeConfiguration.setOnSessionStart { sessionId, isDiscarded ->
            // in iOS SDK source code, sessionId is String, not String?. But by some reason KMP generates
            // binding with String? type.
            if (sessionId != null) {
                sessionListener.onSessionStarted(sessionId, isDiscarded)
            }
        }
    }

    fun setUiKitViewsPredicate(uiKitViewsPredicate: UIKitRUMViewsPredicate) {
        val nativePredicate = if (uiKitViewsPredicate is DefaultUIKitRUMViewsPredicate) {
            // just a short path to avoid creating unnecessary layers. NB: if DefaultUIKitRUMViewsPredicate becomes
            // open, it is better to remove this branch, because its behavior may become different
            // from the wrapped value
            uiKitViewsPredicate.nativeDelegate
        } else {
            object : NSObject(), DDUIKitRUMViewsPredicateProtocol {
                override fun rumViewFor(viewController: UIViewController): DDRUMView? {
                    val rumView = uiKitViewsPredicate.createView(viewController) ?: return null
                    return DDRUMView(
                        rumView.name,
                        rumView.attributes.mapKeys {
                            @Suppress("USELESS_CAST")
                            it.key as Any
                        }
                    )
                }
            }
        }
        nativeConfiguration.setUiKitViewsPredicate(nativePredicate)
    }

    fun setUiKitActionsPredicate(uiKitActionsPredicate: UIKitRUMActionsPredicate) {
        val nativePredicate = if (uiKitActionsPredicate is DefaultUIKitRUMActionsPredicate) {
            // just a short path to avoid creating unnecessary layers. NB: if DefaultUIKitRUMActionsPredicate becomes
            // open, it is better to remove this branch, because its behavior may become different
            // from the wrapped value
            uiKitActionsPredicate.nativeDelegate
        } else {
            // TODO RUM-4818 by some reason the object which just implements DDUIKitRUMActionsPredicateProtocol cannot
            //  be retrieved once it is successfully set. So extending DDDefaultUIKitRUMActionsPredicate instead (which
            //  implements the same protocol, but on the Swift/ObjC side)
            object : DDDefaultUIKitRUMActionsPredicate() {
                override fun rumActionWithTargetView(targetView: UIView): DDRUMAction? {
                    val rumAction = uiKitActionsPredicate.createAction(targetView) ?: return null
                    return DDRUMAction(
                        rumAction.name,
                        rumAction.attributes.mapKeys {
                            @Suppress("USELESS_CAST")
                            it.key as Any
                        }
                    )
                }
            }
        }
        nativeConfiguration.setUiKitActionsPredicate(nativePredicate)
    }

    override fun build(): DDRUMConfiguration {
        return nativeConfiguration
    }

    companion object {
        const val MILLISECONDS_IN_SECOND = 1000
    }
}

private val VitalsUpdateFrequency.native: DDRUMVitalsFrequency
    get() = when (this) {
        VitalsUpdateFrequency.FREQUENT -> DDRUMVitalsFrequencyFrequent
        VitalsUpdateFrequency.AVERAGE -> DDRUMVitalsFrequencyAverage
        VitalsUpdateFrequency.RARE -> DDRUMVitalsFrequencyRare
        VitalsUpdateFrequency.NEVER -> DDRUMVitalsFrequencyNever
    }
