import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp.plugin)
    id("kotlinx-serialization")
    id("kotlin-parcelize")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.commons.io)
    implementation(libs.commons.codec)
    implementation(libs.commons.compress)
    implementation(libs.okio)
    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation(libs.kotlinx.serialization.json)
    api(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    // ADR-14：仅 compose.runtime 状态库（BOM 对齐版本，不引入渲染层）
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.runtime)
    implementation(libs.proxy.client.android)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.maven.artifact)
    implementation(libs.androidx.core.ktx)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
