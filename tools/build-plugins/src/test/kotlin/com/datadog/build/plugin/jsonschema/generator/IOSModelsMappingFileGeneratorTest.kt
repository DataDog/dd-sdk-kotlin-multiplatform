package com.datadog.build.plugin.jsonschema.generator

class IOSModelsMappingFileGeneratorTest : NativeModelsMappingFileGeneratorTest() {

    override fun provideGenerator(): NativeModelsMappingFileGenerator {
        return IOSModelsMappingFileGenerator(
            outputDir = tempDir,
            commonModelsPackageName = COMMON_MODELS_PACKAGE_NAME,
            iosModelsPackageName = "cocoapods.DatadogObjc",
            iosModelsClassNamePrefix = "DDRUM",
            typeNameRemapping = mapOf(
                "Connectivity" to "RUMConnectivity",
                "USR" to "RUMUser",
                "Method" to "RUMMethod",
                "Context" to "RUMEventAttributes",
                "CiTest" to "RUMCITest",
                "SessionType" to "RUMSessionType",
                "Synthetics" to "RUMSyntheticsTest",
                "Device" to "RUMDevice",
                "OS" to "RUMOperatingSystem",
                "SessionPrecondition" to "RUMSessionPrecondition"
            ),
            defaultCommonEnumValues = mapOf(
                "ViewEvent.ViewEventSessionType" to "USER",
                "ViewEvent.Status" to "CONNECTED",
                "ViewEvent.EffectiveType" to "`4G`",
                "ViewEvent.DeviceType" to "OTHER",
                "ViewEvent.ReplayLevel" to "MASK_USER_INPUT",
                "ViewEvent.Plan" to "PLAN_1",
                "ViewEvent.SessionPrecondition" to "USER_APP_LAUNCH",
                "ViewEvent.State" to "ACTIVE",
                "ViewEvent.ViewEventSource" to "IOS",
                "ViewEvent.LoadingType" to "VIEW_CONTROLLER_DISPLAY"
            ),
            logger = mockLogger
        )
    }

    override fun expectedFileName(): String = "IOSViewEventMappingExt.kt"
}
