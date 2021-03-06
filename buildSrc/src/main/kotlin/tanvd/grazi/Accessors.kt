@file:Suppress("ObjectPropertyName")

package tanvd.grazi

import org.gradle.api.Action
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.accessors.runtime.addExternalModuleDependencyTo

fun DependencyHandler._compile(
        group: String,
        name: String,
        version: String? = null,
        configuration: String? = null,
        classifier: String? = null,
        ext: String? = null,
        dependencyConfiguration: Action<ExternalModuleDependency>? = null
): ExternalModuleDependency = addExternalModuleDependencyTo(
        this, "compile", group, name, version, configuration, classifier, ext, dependencyConfiguration
)
