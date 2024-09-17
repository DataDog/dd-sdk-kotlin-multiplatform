package com.datadog.build.plugin.jsonschema.generator

class AndroidModelsMappingFileGeneratorTest : NativeModelsMappingFileGeneratorTest() {

    override fun provideGenerator(): NativeModelsMappingFileGenerator {
        return AndroidModelsMappingFileGenerator(
            outputDir = tempDir,
            commonModelsPackageName = COMMON_MODELS_PACKAGE_NAME,
            androidModelsPackageName = "com.datadog.android.rum.model",
            defaultCommonEnumValues = mapOf(
                "ViewEvent.ViewEventSessionType" to "USER",
                "ViewEvent.Status" to "CONNECTED",
                "ViewEvent.EffectiveType" to "`4G`",
                "ViewEvent.DeviceType" to "OTHER",
                "ViewEvent.ReplayLevel" to "MASK_USER_INPUT",
                "ViewEvent.Plan" to "PLAN_1",
                "ViewEvent.SessionPrecondition" to "USER_APP_LAUNCH",
                "ViewEvent.State" to "ACTIVE",
                "ViewEvent.ViewEventSource" to "ANDROID",
                "ViewEvent.LoadingType" to "ACTIVITY_DISPLAY"
            ),
            logger = mockLogger
        )
    }

    override fun expectedFileName(): String = "AndroidViewEventMappingExt.kt"
}
