/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.internal

import com.datadog.tools.random.randomThrowable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NSErrorExtTest {

    @OptIn(ExperimentalStdlibApi::class)
    private val fakeBinaryImages = mapOf(
        "iosApp" to "100000000",
        "SwiftUI" to "200000000",
        "UIKitCore" to "300000000",
        "CoreFoundation" to "400000000",
        "GraphicsServices" to "500000000"
    ).mapValues { it.value.hexToULong() }

    // region createDatadogStacktrace

    @Test
    fun `M convert Kotlin_Native stacktrace to symbolication-friendly format W createDatadogStacktrace`() {
        // Given
        val kotlinNativeStacktrace = KOTLIN_NATIVE_STACKTRACE.lines().toTypedArray()
        val instructionAddresses = kotlinNativeStacktrace.mapIndexed { index, frame ->
            val libName = fakeBinaryImages.keys.firstOrNull { frame.contains(it) }
            if (libName != null) {
                fakeBinaryImages.getValue(libName).toLong() + index * 100L
            } else {
                0L
            }
        }

        // When
        val datadogStacktrace =
            createDatadogStacktrace(kotlinNativeStacktrace, instructionAddresses, fakeBinaryImages)

        // Then
        val expectedStacktrace = """
            0 iosApp 0x0000000100000000 0x0000000100000000 + 0
            1 iosApp 0x0000000100000064 0x0000000100000000 + 100
            2 iosApp 0x00000001000000c8 0x0000000100000000 + 200
            3 iosApp 0x000000010000012c 0x0000000100000000 + 300
            4 iosApp 0x0000000100000190 0x0000000100000000 + 400
            5 iosApp 0x00000001000001f4 0x0000000100000000 + 500
            6 iosApp 0x0000000100000258 0x0000000100000000 + 600
            7 SwiftUI 0x00000002000002bc 0x0000000200000000 + 700
            8 SwiftUI 0x0000000200000320 0x0000000200000000 + 800
            9 SwiftUI 0x0000000200000384 0x0000000200000000 + 900
            10 SwiftUI 0x00000002000003e8 0x0000000200000000 + 1000
            11 SwiftUI 0x000000020000044c 0x0000000200000000 + 1100
            12 SwiftUI 0x00000002000004b0 0x0000000200000000 + 1200
            13 SwiftUI 0x0000000200000514 0x0000000200000000 + 1300
            14 SwiftUI 0x0000000200000578 0x0000000200000000 + 1400
            15 SwiftUI 0x00000002000005dc 0x0000000200000000 + 1500
            16 SwiftUI 0x0000000200000640 0x0000000200000000 + 1600
            17 SwiftUI 0x00000002000006a4 0x0000000200000000 + 1700
            18 SwiftUI 0x0000000200000708 0x0000000200000000 + 1800
            19 SwiftUI 0x000000020000076c 0x0000000200000000 + 1900
            20 SwiftUI 0x00000002000007d0 0x0000000200000000 + 2000
            21 SwiftUI 0x0000000200000834 0x0000000200000000 + 2100
            22 SwiftUI 0x0000000200000898 0x0000000200000000 + 2200
            23 SwiftUI 0x00000002000008fc 0x0000000200000000 + 2300
            24 UIKitCore 0x0000000300000960 0x0000000300000000 + 2400
            25 UIKitCore 0x00000003000009c4 0x0000000300000000 + 2500
            26 UIKitCore 0x0000000300000a28 0x0000000300000000 + 2600
            27 UIKitCore 0x0000000300000a8c 0x0000000300000000 + 2700
            28 UIKitCore 0x0000000300000af0 0x0000000300000000 + 2800
            29 UIKitCore 0x0000000300000b54 0x0000000300000000 + 2900
            30 iosApp 0x0000000100000bb8 0x0000000100000000 + 3000
            31 iosApp 0x0000000100000c1c 0x0000000100000000 + 3100
            32 UIKitCore 0x0000000300000c80 0x0000000300000000 + 3200
            33 UIKitCore 0x0000000300000ce4 0x0000000300000000 + 3300
            34 UIKitCore 0x0000000300000d48 0x0000000300000000 + 3400
            35 CoreFoundation 0x0000000400000dac 0x0000000400000000 + 3500
            36 CoreFoundation 0x0000000400000e10 0x0000000400000000 + 3600
            37 CoreFoundation 0x0000000400000e74 0x0000000400000000 + 3700
            38 CoreFoundation 0x0000000400000ed8 0x0000000400000000 + 3800
            39 CoreFoundation 0x0000000400000f3c 0x0000000400000000 + 3900
            40 GraphicsServices 0x0000000500000fa0 0x0000000500000000 + 4000
            41 UIKitCore 0x0000000300001004 0x0000000300000000 + 4100
            42 UIKitCore 0x0000000300001068 0x0000000300000000 + 4200
            43 SwiftUI 0x00000002000010cc 0x0000000200000000 + 4300
            44 SwiftUI 0x0000000200001130 0x0000000200000000 + 4400
            45 SwiftUI 0x0000000200001194 0x0000000200000000 + 4500
            46 iosApp 0x00000001000011f8 0x0000000100000000 + 4600
            47 iosApp 0x000000010000125c 0x0000000100000000 + 4700
            48 dyld 0x0000000000000000 0x0000000000000000 + 0
            49 ??? 0x0000000000000000 0x0000000000000000 + 0
            50 ??? 0x0000000000000000 0x0000000000000000 + 0
        """.lines()
            .filter {
                it.isNotBlank()
            }
            .map { it.trim() }

        println(datadogStacktrace)
        val datadogStacktraceLines = datadogStacktrace.lines()
        assertEquals(expectedStacktrace.size, datadogStacktraceLines.size)
        expectedStacktrace.forEachIndexed { index, line ->
            assertEquals(line, datadogStacktraceLines[index])
        }
    }

    @Test
    fun `M convert Kotlin_Native stacktrace W createDatadogStacktrace + lib name with spaces`() {
        // Given
        val kotlinNativeStacktrace = KOTLIN_NATIVE_STACKTRACE.lines()
            .take(3)
            .map { it.replace("iosApp", "ios App") }
            .toTypedArray()
        val instructionAddresses = kotlinNativeStacktrace.mapIndexed { index, _ ->
            fakeBinaryImages.getValue("iosApp").toLong() + index * 100L
        }

        // When
        val datadogStacktrace =
            createDatadogStacktrace(
                kotlinNativeStacktrace,
                instructionAddresses,
                fakeBinaryImages.mapKeys { if (it.key == "iosApp") "ios App" else it.key }
            )

        // Then
        val expectedStacktrace = """
            0 ios App 0x0000000100000000 0x0000000100000000 + 0
            1 ios App 0x0000000100000064 0x0000000100000000 + 100
            2 ios App 0x00000001000000c8 0x0000000100000000 + 200
        """.lines().filter {
            it.isNotBlank()
        }.map { it.trim() }

        val datadogStacktraceLines = datadogStacktrace.lines()
        assertEquals(expectedStacktrace.size, datadogStacktraceLines.size)
        expectedStacktrace.forEachIndexed { index, line ->
            assertEquals(line, datadogStacktraceLines[index])
        }
    }

    @Test
    fun `M drop frames in a wrong format W createDatadogStacktrace + wrong number of pieces`() {
        // Given
        val kotlinNativeStacktrace = KOTLIN_NATIVE_STACKTRACE.lines()
            .take(3)
            .mapIndexed { index, line ->
                if (index == 1) "0 iosApp " else line
            }.toTypedArray()
        val instructionAddresses = kotlinNativeStacktrace.mapIndexed { index, frame ->
            val libName = fakeBinaryImages.keys.firstOrNull { frame.contains(it) }
            if (libName != null) {
                fakeBinaryImages.getValue(libName).toLong() + index * 100L
            } else {
                0L
            }
        }

        // When
        val datadogStacktrace =
            createDatadogStacktrace(kotlinNativeStacktrace, instructionAddresses, fakeBinaryImages)

        // Then
        val expectedStacktrace = """
            0 iosApp 0x0000000100000000 0x0000000100000000 + 0
            1 ??? 0x0000000000000000 0x00000000 + 0
            2 iosApp 0x00000001000000c8 0x0000000100000000 + 200
        """.lines().filter {
            it.isNotBlank()
        }.map { it.trim() }

        val datadogStacktraceLines = datadogStacktrace.lines()
        assertEquals(expectedStacktrace.size, datadogStacktraceLines.size)
        expectedStacktrace.forEachIndexed { index, line ->
            assertEquals(line, datadogStacktraceLines[index])
        }
    }

    @Test
    fun `M drop frames in a wrong format W createDatadogStacktrace + first piece is not a number`() {
        // Given
        val kotlinNativeStacktrace = KOTLIN_NATIVE_STACKTRACE.lines()
            .take(3)
            .mapIndexed { index, line ->
                if (index == 1) "a iosApp 0x104924d37 kfun:kotlin.Exception#<init>(kotlin.Throwable?){} + 115" else line
            }.toTypedArray()
        val instructionAddresses = kotlinNativeStacktrace.mapIndexed { index, frame ->
            val libName = fakeBinaryImages.keys.firstOrNull { frame.contains(it) }
            if (libName != null) {
                fakeBinaryImages.getValue(libName).toLong() + index * 100L
            } else {
                0L
            }
        }

        // When
        val datadogStacktrace =
            createDatadogStacktrace(kotlinNativeStacktrace, instructionAddresses, fakeBinaryImages)

        // Then
        val expectedStacktrace = """
            0 iosApp 0x0000000100000000 0x0000000100000000 + 0
            1 ??? 0x0000000000000000 0x00000000 + 0
            2 iosApp 0x00000001000000c8 0x0000000100000000 + 200
        """.lines().filter {
            it.isNotBlank()
        }.map { it.trim() }

        val datadogStacktraceLines = datadogStacktrace.lines()
        assertEquals(expectedStacktrace.size, datadogStacktraceLines.size)
        expectedStacktrace.forEachIndexed { index, line ->
            assertEquals(line, datadogStacktraceLines[index])
        }
    }

    // endregion

    // region createNSErrorFromThrowable

    @Test
    fun `M return an error of expected format W createNSErrorFromThrowable`() {
        // Given
        val fakeThrowable = randomThrowable()

        // When
        val error = createNSErrorFromThrowable(fakeThrowable, "fake message")

        // Then
        assertEquals("fake message\n${fakeThrowable.message}", error.localizedDescription)

        val stacktrace = error.description
        checkNotNull(stacktrace)

        val stacktraceLines = stacktrace.lines()
        assertTrue(stacktraceLines.isNotEmpty(), "Expected to have non-empty stacktrace")

        stacktraceLines.forEach {
            assertTrue(
                VALID_IOS_STACK_FRAME_REGEX.matches(it),
                "Expected stack frame to match a valid format, but it wasn't:\n\n$it"
            )
        }
    }

    @Test
    fun `M return only throwable message W createNSErrorFromThrowable + no user message`() {
        // Given
        val fakeThrowable = randomThrowable()

        // When
        val error = createNSErrorFromThrowable(fakeThrowable, null)

        // Then
        assertEquals(fakeThrowable.message, error.localizedDescription)
    }

    // endregion

    companion object {

        val VALID_IOS_STACK_FRAME_REGEX = Regex("^([0-9]+)\\s+(.+)\\s+(0x[0-9a-f]{16}) (0x[0-9a-f]+) \\+ [0-9]+\$")

        const val KOTLIN_NATIVE_STACKTRACE =
            """0   iosApp                              0x10492ac8b        kfun:kotlin.Throwable#<init>(kotlin.Throwable?){} + 271 
            1   iosApp                              0x104924d37        kfun:kotlin.Exception#<init>(kotlin.Throwable?){} + 115 
            2   iosApp                              0x104924f57        kfun:kotlin.RuntimeException#<init>(kotlin.Throwable?){} + 115 
            3   iosApp                              0x1048e436f        kfun:com.datadog.kmp.sample#triggerUncheckedException(){} + 275 
            4   iosApp                              0x1048e3aef        kfun:com.datadog.kmp.sample#logErrorWithThrowable(){} + 279 
            5   iosApp                              0x1048f0e2f        objc2kotlin_kfun:com.datadog.kmp.sample#logErrorWithThrowable(){} + 107 
            6   iosApp                              0x1041adf77        ${'$'}s6iosApp11ContentViewV4bodyQrvg7SwiftUI05TupleD0VyAE6ButtonVyAE0D0PAEE12cornerRadius_11antialiasedQr12CoreGraphics7CGFloatV_SbtFQOyAkEE15foregroundColoryQrAE0Q0VSgFQOyAkEE10background_20ignoresSafeAreaEdgesQrqd___AE4EdgeO3SetVtAE10ShapeStyleRd__lFQOyAkEE7paddingyQrAZ_APSgtFQOyAE4TextV_Qo__ASQo__Qo__Qo_G_A8_A8_A8_A8_A8_tGyXEfU_yycfU1_ + 135 (/sample/iosApp/iosApp/ContentView.swift:35:25)
            7   SwiftUI                             0x1c51dda47        OUTLINED_FUNCTION_11 + 619 
            8   SwiftUI                             0x1c497d7e7        OUTLINED_FUNCTION_19 + 9343 
            9   SwiftUI                             0x1c4982363        objectdestroy.56Tm + 175 
            10  SwiftUI                             0x1c49807af        OUTLINED_FUNCTION_19 + 21575 
            11  SwiftUI                             0x1c4980703        OUTLINED_FUNCTION_19 + 21403 
            12  SwiftUI                             0x1c4edb03b        OUTLINED_FUNCTION_1 + 4307 
            13  SwiftUI                             0x1c4eda4b3        OUTLINED_FUNCTION_1 + 1355 
            14  SwiftUI                             0x1c4ed9fd3        OUTLINED_FUNCTION_1 + 107 
            15  SwiftUI                             0x1c4aff79f        OUTLINED_FUNCTION_21 + 31 
            16  SwiftUI                             0x1c4aff7bb        OUTLINED_FUNCTION_21 + 59 
            17  SwiftUI                             0x1c4aff79f        OUTLINED_FUNCTION_21 + 31 
            18  SwiftUI                             0x1c51cbd6b        OUTLINED_FUNCTION_17 + 2339 
            19  SwiftUI                             0x1c51cc32f        OUTLINED_FUNCTION_17 + 3815 
            20  SwiftUI                             0x1c53482b3        OUTLINED_FUNCTION_10 + 143 
            21  SwiftUI                             0x1c51342a3        OUTLINED_FUNCTION_20 + 9187 
            22  SwiftUI                             0x1c513297f        OUTLINED_FUNCTION_20 + 2751 
            23  SwiftUI                             0x1c5132ab7        OUTLINED_FUNCTION_20 + 3063 
            24  UIKitCore                           0x184e0c0bb        -[UIGestureRecognizer _componentsEnded:withEvent:] + 139 
            25  UIKitCore                           0x18533c753        -[UITouchesEvent _sendEventToGestureRecognizer:] + 555 
            26  UIKitCore                           0x184e0022f        -[UIGestureEnvironment _deliverEvent:toGestureRecognizers:usingBlock:] + 159 
            27  UIKitCore                           0x184e0001f        -[UIGestureEnvironment _updateForEvent:window:] + 155 
            28  UIKitCore                           0x1852f49ff        -[UIWindow sendEvent:] + 3087 
            29  UIKitCore                           0x1852d468b        -[UIApplication sendEvent:] + 575 
            30  iosApp                              0x1046cc36b        ${'$'}s10DatadogRUM21UIApplicationSwizzlerC9SendEventC7swizzleyyFSbSo0C0C_So7UIEventCtXBSbAH_10ObjectiveC8SelectorVAJtXCcfU_SbAH_AJtcfU_ + 279 (/Library/Developer/Xcode/DerivedData/iosApp-ajtwommphdihriaplbsvsxixlbbd/SourcePackages/checkouts/dd-sdk-ios/DatadogRUM/Sources/Instrumentation/Actions/UIKit/UIApplicationSwizzler.swift:46:28)
            31  iosApp                              0x1046cc3cf        ${'$'}sSo13UIApplicationCSo7UIEventCSbIegggd_AbDSbIeyByyd_TR + 79 
            32  UIKitCore                           0x185354013        __dispatchPreprocessedEventFromEventQueue + 1707 
            33  UIKitCore                           0x185356ec3        __processEventQueue + 5519 
            34  UIKitCore                           0x18534ff57        __eventFetcherSourceCallback + 155 
            35  CoreFoundation                      0x1803ee69b        __CFRUNLOOP_IS_CALLING_OUT_TO_A_SOURCE0_PERFORM_FUNCTION__ + 23 
            36  CoreFoundation                      0x1803ee5e3        __CFRunLoopDoSource0 + 171 
            37  CoreFoundation                      0x1803edd53        __CFRunLoopDoSources0 + 231 
            38  CoreFoundation                      0x1803e843b        __CFRunLoopRun + 767 
            39  CoreFoundation                      0x1803e7d27        CFRunLoopRunSpecific + 571 
            40  GraphicsServices                    0x18e7cdbbf        GSEventRunModal + 159 
            41  UIKitCore                           0x1852bafdb        -[UIApplication _run] + 867 
            42  UIKitCore                           0x1852bec53        UIApplicationMain + 123 
            43  SwiftUI                             0x1c4b04523        OUTLINED_FUNCTION_70 + 499 
            44  SwiftUI                             0x1c4b043c3        OUTLINED_FUNCTION_70 + 147 
            45  SwiftUI                             0x1c4816107        OUTLINED_FUNCTION_2 + 91 
            46  iosApp                              0x1041acb4b        ${'$'}s6iosApp6iOSAppV5${'$'}mainyyFZ + 39 
            47  iosApp                              0x1041acbf7        main + 11 (/sample/iosApp/iosApp/iOSApp.swift:<unknown>)
            48  dyld                                0x105a4d557        0x0 + 4389655895 
            49  ???                                 0x105cba0df        0x0 + 4392198367 
            50  ???                                 0xd17c7fffffffffff 0x0 + -3351663285182136321"""
    }
}
