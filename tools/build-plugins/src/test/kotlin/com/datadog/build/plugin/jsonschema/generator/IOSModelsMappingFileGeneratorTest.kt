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
            logger = mockLogger
        )
    }

    override fun expectedFileName(): String = "IOSViewEventMappingExt.kt"
}
