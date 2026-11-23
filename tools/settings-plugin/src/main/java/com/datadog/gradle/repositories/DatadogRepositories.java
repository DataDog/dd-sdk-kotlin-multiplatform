/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.gradle.repositories;

import java.net.URI;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.provider.ProviderFactory;

/**
 * Repository definitions shared by the settings scripts.
 * <p>
 * When the Magic Mirror Depot proxy properties are set (only in CI, via `.gitlab-ci.yml`):
 * <ul>
 *     <li>Depot replaces Maven Central;</li>
 *     <li>Depot is tried before Google Maven, which is kept as a fallback because we haven't
 *     verified that Depot mirrors all of it;</li>
 *     <li>Depot is tried before the Gradle Plugin Portal, which is kept as a fallback because Depot
 *     doesn't mirror all of it.</li>
 * </ul>
 * dd-sdk-android's first Depot change (13bffbc314) also put Depot ahead of Google Maven, which
 * 17afe3566f dropped; here it's kept so that CI doesn't download from dl.google.com directly.
 * <p>
 * A Depot repository is added at most once per repository handler. Local builds, where the
 * properties are not set, use the public registries.
 */
public final class DatadogRepositories {

    /**
     * Maven repository proxy URL (Magic Mirror Depot). When set, replaces Maven Central so artifact
     * resolution goes through Datadog's internal mirror instead of hitting it directly. Used in CI
     * to avoid 429s. Set via -PmavenRepositoryProxy=... or ORG_GRADLE_PROJECT_mavenRepositoryProxy.
     */
    private static final String MAVEN_REPOSITORY_PROXY = "mavenRepositoryProxy";

    /**
     * Gradle plugin portal proxy URL (Magic Mirror Depot). When set, prepended to the Gradle Plugin
     * Portal so plugin resolution goes through Datadog's internal mirror first. Used in CI to avoid
     * 429s. Set via -PgradlePluginProxy=... or ORG_GRADLE_PROJECT_gradlePluginProxy.
     */
    private static final String GRADLE_PLUGIN_PROXY = "gradlePluginProxy";

    private static final String JITPACK_URL = "https://jitpack.io";
    private static final String JITPACK_GROUPS_REGEX = "com\\.github\\..*";

    private final String mavenProxy;
    private final String gradlePluginProxy;

    public DatadogRepositories(ProviderFactory providers) {
        this.mavenProxy = readProxy(providers, MAVEN_REPOSITORY_PROXY);
        this.gradlePluginProxy = readProxy(providers, GRADLE_PLUGIN_PROXY);
    }

    public void mavenCentralDepotProxied(RepositoryHandler repositories) {
        if (mavenProxy != null) {
            addDepot(repositories, "Maven Datadog Depot", mavenProxy);
        } else {
            repositories.mavenCentral();
        }
    }

    public void googleDepotProxied(RepositoryHandler repositories) {
        if (mavenProxy != null) {
            addDepot(repositories, "Maven Datadog Depot", mavenProxy);
        }
        // Fallback for Google Maven artifacts that Depot doesn't have.
        repositories.google();
    }

    public void gradlePluginPortalDepotProxied(RepositoryHandler repositories) {
        if (gradlePluginProxy != null) {
            addDepot(repositories, "Gradle Plugin Datadog Depot", gradlePluginProxy);
        }
        // Depot doesn't mirror all of the Gradle Plugin Portal, so keep it as a fallback, unlike
        // Maven Central.
        repositories.gradlePluginPortal();
    }

    public void jitpackConstrained(RepositoryHandler repositories) {
        repositories.maven(repository -> {
            repository.setUrl(JITPACK_URL);
            repository.mavenContent(content -> content.includeGroupByRegex(JITPACK_GROUPS_REGEX));
        });
    }

    // Both proxy properties point to the same Depot endpoint in CI, so skip a repository whose URL
    // is already declared instead of querying it twice for every artifact it doesn't have.
    private static void addDepot(RepositoryHandler repositories, String name, String url) {
        URI uri = URI.create(url);
        boolean alreadyDeclared = repositories.stream().anyMatch(repository ->
                repository instanceof MavenArtifactRepository
                        && uri.equals(((MavenArtifactRepository) repository).getUrl())
        );
        if (!alreadyDeclared) {
            repositories.maven(repository -> {
                repository.setName(name);
                repository.setUrl(uri);
            });
        }
    }

    private static String readProxy(ProviderFactory providers, String propertyName) {
        String value = providers.gradleProperty(propertyName).getOrNull();
        return value == null || value.isBlank() ? null : value.trim();
    }
}
