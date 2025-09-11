/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.configuration.internal

import android.content.Context
import android.view.View
import com.datadog.android.api.SdkCore
import com.datadog.kmp.event.EventMapper
import com.datadog.kmp.rum.configuration.RumSessionListener
import com.datadog.kmp.rum.configuration.VitalsUpdateFrequency
import com.datadog.kmp.rum.event.ViewEventMapper
import com.datadog.kmp.rum.model.ActionEvent
import com.datadog.kmp.rum.model.ErrorEvent
import com.datadog.kmp.rum.model.LongTaskEvent
import com.datadog.kmp.rum.model.ResourceEvent
import com.datadog.kmp.rum.model.toCommonModel
import com.datadog.kmp.rum.tracking.InteractionPredicate
import com.datadog.kmp.rum.tracking.ViewAttributesProvider
import com.datadog.kmp.rum.tracking.ViewTrackingStrategy
import com.datadog.android.rum.RumConfiguration as NativeAndroidConfiguration
import com.datadog.android.rum.RumSessionListener as NativeRumSessionListener
import com.datadog.android.rum.configuration.VitalsUpdateFrequency as NativeVitalsUpdateFrequency
import com.datadog.android.rum.model.ErrorEvent as NativeErrorEvent
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

    override fun setViewEventMapper(eventMapper: ViewEventMapper) {
        nativeConfigurationBuilder.setViewEventMapper { view ->
            val mapped = eventMapper.map(view.toCommonModel())

            view.view.referrer = mapped.view.referrer
            view.view.url = mapped.view.url
            view.view.name = mapped.view.name

            // usr and context additional properties are exposed as a reference, so no need to copy things back

            view
        }
    }

    override fun setResourceEventMapper(eventMapper: EventMapper<ResourceEvent>) {
        nativeConfigurationBuilder.setResourceEventMapper native@{ resource ->
            val mapped = eventMapper.map(resource.toCommonModel()) ?: return@native null

            resource.view.referrer = mapped.view.referrer
            resource.view.url = mapped.view.url
            resource.view.name = mapped.view.name

            resource.resource.url = mapped.resource.url

            resource.resource.graphql?.payload = mapped.resource.graphql?.payload
            resource.resource.graphql?.variables = mapped.resource.graphql?.variables

            // usr and context additional properties are exposed as a reference, so no need to copy things back

            resource
        }
    }

    override fun setActionEventMapper(eventMapper: EventMapper<ActionEvent>) {
        nativeConfigurationBuilder.setActionEventMapper native@{ action ->
            val mapped = eventMapper.map(action.toCommonModel()) ?: return@native null

            action.view.referrer = mapped.view.referrer
            action.view.url = mapped.view.url
            action.view.name = mapped.view.name

            mapped.action.target?.let { target ->
                action.action.target?.name = target.name
            }

            // usr and context additional properties are exposed as a reference, so no need to copy things back

            action
        }
    }

    override fun setErrorEventMapper(eventMapper: EventMapper<ErrorEvent>) {
        nativeConfigurationBuilder.setErrorEventMapper native@{ error ->
            val mapped = eventMapper.map(error.toCommonModel()) ?: return@native null

            error.view.referrer = mapped.view.referrer
            error.view.url = mapped.view.url
            error.view.name = mapped.view.name

            error.error.message = mapped.error.message
            error.error.stack = mapped.error.stack
            error.error.fingerprint = mapped.error.fingerprint
            error.error.causes = mapped.error.causes?.map {
                NativeErrorEvent.Cause(
                    message = it.message,
                    type = it.type,
                    stack = it.stack,
                    source = when (it.source) {
                        ErrorEvent.ErrorSource.NETWORK -> NativeErrorEvent.ErrorSource.NETWORK
                        ErrorEvent.ErrorSource.SOURCE -> NativeErrorEvent.ErrorSource.SOURCE
                        ErrorEvent.ErrorSource.CONSOLE -> NativeErrorEvent.ErrorSource.CONSOLE
                        ErrorEvent.ErrorSource.LOGGER -> NativeErrorEvent.ErrorSource.LOGGER
                        ErrorEvent.ErrorSource.AGENT -> NativeErrorEvent.ErrorSource.AGENT
                        ErrorEvent.ErrorSource.WEBVIEW -> NativeErrorEvent.ErrorSource.WEBVIEW
                        ErrorEvent.ErrorSource.CUSTOM -> NativeErrorEvent.ErrorSource.CUSTOM
                        ErrorEvent.ErrorSource.REPORT -> NativeErrorEvent.ErrorSource.REPORT
                    }
                )
            }

            mapped.error.resource?.let {
                error.error.resource?.url = it.url
            }

            // usr and context additional properties are exposed as a reference, so no need to copy things back

            error
        }
    }

    override fun setLongTaskEventMapper(eventMapper: EventMapper<LongTaskEvent>) {
        nativeConfigurationBuilder.setLongTaskEventMapper native@{ longTask ->
            val mapped = eventMapper.map(longTask.toCommonModel()) ?: return@native null

            longTask.view.referrer = mapped.view.referrer
            longTask.view.url = mapped.view.url
            longTask.view.name = mapped.view.name

            longTask
        }
    }

    override fun trackAnonymousUser(enabled: Boolean) {
        nativeConfigurationBuilder.trackAnonymousUser(enabled)
    }

    override fun useCustomEndpoint(endpoint: String) {
        nativeConfigurationBuilder.useCustomEndpoint(endpoint)
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
