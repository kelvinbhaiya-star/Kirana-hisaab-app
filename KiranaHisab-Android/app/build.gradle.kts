plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.kiranahisab.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kiranahisab.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
}

val copyDebugApk = tasks.register("copyDebugApk") {
    val apkSource = layout.buildDirectory.file("outputs/apk/debug/app-debug.apk")
    val apkDest = rootProject.layout.projectDirectory.file("app-debug.apk")

    inputs.file(apkSource).optional()
    outputs.file(apkDest)

    doLast {
        val sourceApk = apkSource.get().asFile
        val destApk = apkDest.get().asFile

        if (!sourceApk.exists()) {
            logger.warn("copyDebugApk: Debug APK not found at ${sourceApk.absolutePath}")
            return@doLast
        }

        // Wait briefly for APK file size to stabilize and ensure no packaging lock remains
        var previousSize = -1L
        var stableChecks = 0
        val maxAttempts = 20

        for (attempt in 1..maxAttempts) {
            val currentSize = sourceApk.length()
            val readable = try {
                sourceApk.inputStream().use { true }
            } catch (_: Exception) {
                false
            }

            if (currentSize > 0L && currentSize == previousSize && readable) {
                stableChecks++
                if (stableChecks >= 2) break
            } else {
                stableChecks = 0
            }

            previousSize = currentSize
            Thread.sleep(150L)
        }

        sourceApk.copyTo(destApk, overwrite = true)
        logger.lifecycle("copyDebugApk: Copied ${sourceApk.name} (${destApk.length()} bytes) to ${destApk.absolutePath}")
    }
}

tasks.matching { it.name == "assembleDebug" }.configureEach {
    finalizedBy(copyDebugApk)
}

