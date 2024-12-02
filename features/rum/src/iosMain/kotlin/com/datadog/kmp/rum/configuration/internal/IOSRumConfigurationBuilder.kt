/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.configuration.internal

import cocoapods.DatadogObjc.DDDefaultUIKitRUMActionsPredicate
import cocoapods.DatadogObjc.DDRUMAction
import cocoapods.DatadogObjc.DDRUMConfiguration
import cocoapods.DatadogObjc.DDRUMErrorEventErrorCauses
import cocoapods.DatadogObjc.DDRUMView
import cocoapods.DatadogObjc.DDRUMVitalsFrequency
import cocoapods.DatadogObjc.DDRUMVitalsFrequencyAverage
import cocoapods.DatadogObjc.DDRUMVitalsFrequencyFrequent
import cocoapods.DatadogObjc.DDRUMVitalsFrequencyNever
import cocoapods.DatadogObjc.DDRUMVitalsFrequencyRare
import cocoapods.DatadogObjc.DDUIKitRUMViewsPredicateProtocol
import com.datadog.kmp.event.EventMapper
import com.datadog.kmp.internal.eraseKeyType
import com.datadog.kmp.rum.configuration.RumSessionListener
import com.datadog.kmp.rum.configuration.VitalsUpdateFrequency
import com.datadog.kmp.rum.event.ViewEventMapper
import com.datadog.kmp.rum.model.ActionEvent
import com.datadog.kmp.rum.model.ErrorEvent
import com.datadog.kmp.rum.model.LongTaskEvent
import com.datadog.kmp.rum.model.ResourceEvent
import com.datadog.kmp.rum.model.errorEventErrorCausesSourceToCommonEnum
import com.datadog.kmp.rum.model.toCommonModel
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

    override fun setViewEventMapper(eventMapper: ViewEventMapper) {
        nativeConfiguration.setViewEventMapper native@{ view ->
            if (view == null) return@native null
            val mapped = eventMapper.map(view.toCommonModel())

            view.view().setReferrer(mapped.view.referrer)
            view.view().setUrl(mapped.view.url)
            view.view().setName(mapped.view.name)

            mapped.usr?.additionalProperties?.let {
                view.usr()?.setUsrInfo(eraseKeyType(it))
            }

            mapped.context?.additionalProperties?.let {
                view.context()?.setContextInfo(eraseKeyType(it))
            }

            view
        }
    }

    override fun setResourceEventMapper(eventMapper: EventMapper<ResourceEvent>) {
        nativeConfiguration.setResourceEventMapper native@{ resource ->
            if (resource == null) return@native null
            val mapped = eventMapper.map(resource.toCommonModel()) ?: return@native null

            resource.view().setReferrer(mapped.view.referrer)
            resource.view().setUrl(mapped.view.url)
            resource.view().setName(mapped.view.name)

            resource.resource().setUrl(mapped.resource.url)

            resource.resource().graphql()?.setPayload(mapped.resource.graphql?.payload)
            resource.resource().graphql()?.setVariables(mapped.resource.graphql?.variables)

            mapped.usr?.additionalProperties?.let {
                resource.usr()?.setUsrInfo(eraseKeyType(it))
            }

            mapped.context?.additionalProperties?.let {
                resource.context()?.setContextInfo(eraseKeyType(it))
            }

            resource
        }
    }

    override fun setActionEventMapper(eventMapper: EventMapper<ActionEvent>) {
        nativeConfiguration.setActionEventMapper native@{ action ->
            if (action == null) return@native null
            val mapped = eventMapper.map(action.toCommonModel()) ?: return@native null

            action.view().setReferrer(mapped.view.referrer)
            action.view().setUrl(mapped.view.url)
            action.view().setName(mapped.view.name)

            mapped.action.target?.let { target ->
                action.action().target()?.setName(target.name)
            }

            mapped.usr?.additionalProperties?.let {
                action.usr()?.setUsrInfo(eraseKeyType(it))
            }

            mapped.context?.additionalProperties?.let {
                action.context()?.setContextInfo(eraseKeyType(it))
            }

            action
        }
    }

    override fun setErrorEventMapper(eventMapper: EventMapper<ErrorEvent>) {
        nativeConfiguration.setErrorEventMapper native@{ error ->
            if (error == null) return@native null
            val mapped = eventMapper.map(error.toCommonModel()) ?: return@native null

            error.view().setReferrer(mapped.view.referrer)
            error.view().setUrl(mapped.view.url)
            error.view().setName(mapped.view.name)

            error.error().setMessage(mapped.error.message)
            error.error().setStack(mapped.error.stack)
            error.error().setFingerprint(mapped.error.fingerprint)

            // causes is "var causes: List<...>", but on iOS DDRUMErrorEventErrorCauses constructor is not public
            // so we can map only in the following cases
            // * if returned list is empty or null
            // * if there is a single item where immutable fields are matching native model
            // Otherwise we cannot create new instances.
            if (mapped.error.causes == null) {
                error.error().setCauses(null)
            } else if (mapped.error.causes?.isEmpty() == true) {
                error.error().setCauses(emptyList<DDRUMErrorEventErrorCauses>())
            } else if (mapped.error.causes?.size == 1 && error.error().causes()?.size == 1) {
                val firstMapped = mapped.error.causes?.first()
                val firstOriginal = error.error().causes()?.first() as DDRUMErrorEventErrorCauses
                if (firstMapped != null &&
                    firstMapped.type == firstOriginal.type() &&
                    firstMapped.source == errorEventErrorCausesSourceToCommonEnum(firstOriginal.source())
                ) {
                    firstOriginal.setStack(firstMapped.stack)
                    firstOriginal.setMessage(firstMapped.message)
                }
            }

            mapped.error.resource?.let {
                error.error().resource()?.setUrl(it.url)
            }

            mapped.usr?.additionalProperties?.let {
                error.usr()?.setUsrInfo(eraseKeyType(it))
            }

            mapped.context?.additionalProperties?.let {
                error.context()?.setContextInfo(eraseKeyType(it))
            }

            error
        }
    }

    override fun setLongTaskEventMapper(eventMapper: EventMapper<LongTaskEvent>) {
        nativeConfiguration.setLongTaskEventMapper native@{ longTask ->
            if (longTask == null) return@native null
            val mapped = eventMapper.map(longTask.toCommonModel()) ?: return@native null

            longTask.view().setReferrer(mapped.view.referrer)
            longTask.view().setUrl(mapped.view.url)
            longTask.view().setName(mapped.view.name)

            longTask
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

    fun setAppHangThreshold(thresholdMs: Long?) {
        val thresholdSeconds = if (thresholdMs != null) {
            thresholdMs.toDouble() / MILLISECONDS_IN_SECOND
        } else {
            0.0
        }
        nativeConfiguration.setAppHangThreshold(thresholdSeconds)
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
