rootProject.name = "glottony"

pluginManagement {
    val kotlinVersion: String by settings
    val kspVersion: String by settings
    val koverVersion: String by settings
    val dokkaVersion: String by settings

    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "com.google.devtools.ksp" -> useVersion(kspVersion)
                "org.jetbrains.kotlinx.kover" -> useVersion(koverVersion)
                "org.jetbrains.dokka" -> useVersion(dokkaVersion)
            }
            if (requested.id.id.startsWith("org.jetbrains.kotlin.")) {
                useVersion(kotlinVersion)
            }
        }
    }

    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

include("server")
include("client")
