// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false
}

// Optional: Add allprojects configuration for consistent dependency versions
allprojects {
    repositories {
        // Repositories are usually in settings.gradle.kts now
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}