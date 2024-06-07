/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.configuration.internal

import android.content.Context
import android.view.View
import com.datadog.android.api.SdkCore
import com.datadog.kmp.rum.configuration.RumSessionListener
import com.datadog.kmp.rum.configuration.VitalsUpdateFrequency
import com.datadog.kmp.rum.tracking.InteractionPredicate
import com.datadog.kmp.rum.tracking.ViewAttributesProvider
import com.datadog.kmp.rum.tracking.ViewTrackingStrategy
import com.datadog.android.rum.RumConfiguration as NativeAndroidConfiguration
import com.datadog.android.rum.RumSessionListener as NativeRumSessionListener
import com.datadog.android.rum.configuration.VitalsUpdateFrequency as NativeVitalsUpdateFrequency
import com.datadog.android.rum.tracking.InteractionPredicate as NativeInteractionPredicate
import com.datadog.android.rum.tracking.ViewAttributesProvider as NativeViewAttributesProvider
import com.datadog.android.rum.tracking.ViewTrackingStrategy as NativeViewTrackingStrategy

internal class AndroidRumConfigurationBuilder : PlatformRumConfigurationBuilder<NativeAndroidConfiguration> {

    private val nativeConfigurationBuilder: NativeAndroidConfiguration.Builder

    constructor(applicationId: String) : this(NativeAndroidConfiguration.Builder(applicationId))

    internal constructor(nativeConfigurationBuilder: NativeAndroidConfiguration.Builder) {
        this.nativeConfigurationBuilder = nativeConfigurationBuilder
    }

    override fun setSessionSampleRate(sampleRate: Float) {
        nativeConfigurationBuilder.setSessionSampleRate(sampleRate)
    }

    override fun setTelemetrySampleRate(sampleRate: Float) {
        nativeConfigurationBuilder.setTelemetrySampleRate(sampleRate)
    }

    override fun trackLongTasks(longTaskThresholdMs: Long) {
        nativeConfigurationBuilder.trackLongTasks(longTaskThresholdMs)
    }

    override fun trackBackgroundEvents(enabled: Boolean) {
        nativeConfigurationBuilder.trackBackgroundEvents(enabled)
    }

    override fun trackFrustrations(enabled: Boolean) {
        nativeConfigurationBuilder.trackFrustrations(enabled)
    }

    override fun setVitalsUpdateFrequency(frequency: VitalsUpdateFrequency) {
        nativeConfigurationBuilder.setVitalsUpdateFrequency(frequency.native)
    }

    override fun setSessionListener(sessionListener: RumSessionListener) {
        nativeConfigurationBuilder.setSessionListener(object : NativeRumSessionListener {
            override fun onSessionStarted(sessionId: String, isDiscarded: Boolean) {
                sessionListener.onSessionStarted(sessionId, isDiscarded)
            }
        })
    }

    fun trackNonFatalAnrs(enabled: Boolean) {
        nativeConfigurationBuilder.trackNonFatalAnrs(enabled)
    }

    fun useViewTrackingStrategy(strategy: ViewTrackingStrategy?) {
        nativeConfigurationBuilder.useViewTrackingStrategy(strategy.native)
    }

    fun trackUserInteractions(
        touchTargetExtraAttributesProviders: Array<ViewAttributesProvider>,
        interactionPredicate: InteractionPredicate
    ) {
        nativeConfigurationBuilder.trackUserInteractions(
            touchTargetExtraAttributesProviders.map { it.native }.toTypedArray(),
            interactionPredicate.native
        )
    }

    override fun build(): NativeAndroidConfiguration {
        return nativeConfigurationBuilder.build()
    }
}

private val VitalsUpdateFrequency.native: NativeVitalsUpdateFrequency
    get() = when (this) {
        VitalsUpdateFrequency.FREQUENT -> NativeVitalsUpdateFrequency.FREQUENT
        VitalsUpdateFrequency.AVERAGE -> NativeVitalsUpdateFrequency.AVERAGE
        VitalsUpdateFrequency.RARE -> NativeVitalsUpdateFrequency.RARE
        VitalsUpdateFrequency.NEVER -> NativeVitalsUpdateFrequency.NEVER
    }

private val ViewTrackingStrategy?.native: NativeViewTrackingStrategy?
    get() {
        val kmpStrategy = this ?: return null
        return object : NativeViewTrackingStrategy {
            override fun register(sdkCore: SdkCore, context: Context) {
                kmpStrategy.register(context)
            }

            override fun unregister(context: Context?) {
                kmpStrategy.unregister(context)
            }
        }
    }

private val ViewAttributesProvider.native: NativeViewAttributesProvider
    get() {
        val kmpProvider = this
        return object : NativeViewAttributesProvider {
            override fun extractAttributes(view: View, attributes: MutableMap<String, Any?>) {
                kmpProvider.extractAttributes(view, attributes)
            }
        }
    }

private val InteractionPredicate.native: NativeInteractionPredicate
    get() {
        val kmpPredicate = this
        return object : NativeInteractionPredicate {
            override fun getTargetName(target: Any): String? = kmpPredicate.getTargetName(target)
        }
    }
