// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Define plugin versions in settings.gradle.kts or libs.versions.toml for better management
    // For this manual setup, we apply them directly or ensure they are resolvable
    id("com.android.application") version "8.2.0" apply false // Example AGP version
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false // Example Kotlin version
    // kotlin-kapt is applied in app/build.gradle.kts, classpath is defined below
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Classpaths for plugins
        // These versions should ideally be managed via libs.versions.toml
        classpath("com.android.tools.build:gradle:8.2.0") // Example AGP version
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22") // Example Kotlin version
        classpath("org.jetbrains.kotlin:kotlin-kapt-gradle-plugin:1.9.22") // Example Kapt version
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
