/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build.plugin.gitclone

import java.io.Serializable

open class GitCloneDependenciesExtension : Serializable {

    internal data class Dependency(
        var originRepository: String,
        var originSubFolder: String,
        var excludedPrefixes: List<String>,
        var originRef: String,
        var destinationFolder: String
    ) : Serializable

    internal val dependencies: MutableList<Dependency> = mutableListOf()
}
