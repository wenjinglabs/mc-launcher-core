import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    // 注意：本模块禁止引入 compose 插件与任何 Android UI 依赖，见 scripts/gate_core_purity.sh
}

android {
    namespace = "io.launchcore.core"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.commons.io)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
