package com.datadog.build.plugin.jsonschema.generator

class AndroidModelsMappingFileGeneratorTest : NativeModelsMappingFileGeneratorTest() {

    override fun provideGenerator(): NativeModelsMappingFileGenerator {
        return AndroidModelsMappingFileGenerator(
            outputDir = tempDir,
            commonModelsPackageName = COMMON_MODELS_PACKAGE_NAME,
            androidModelsPackageName = "com.datadog.android.rum.model",
            logger = mockLogger
        )
    }

    override fun expectedFileName(): String = "AndroidViewEventMappingExt.kt"
}
