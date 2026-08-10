import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

        plugins {
            alias(libs.plugins.kotlinMultiplatform)
            // alias(libs.plugins.androidMultiplatformLibrary)
            alias(libs.plugins.composeMultiplatform)
            alias(libs.plugins.composeCompiler)
            kotlin("plugin.serialization") version "2.0.0"
        }

kotlin {
    jvm()

    js {
        browser()
    }

    // Habilitamos wasmJs ya que Supabase 3.x lo soporta
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    /*
    android {
        namespace = "com.terapia.terasenior.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }
    */

    sourceSets {
        /*
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
            implementation("io.ktor:ktor-client-okhttp:3.0.0")
        }
        */
        jvmMain.dependencies {
            implementation("io.ktor:ktor-client-cio:3.0.0")
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material)
            implementation(compose.materialIconsExtended)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // Supabase 3.7.0 + Ktor 3.5.1
            implementation("io.github.jan-tennert.supabase:postgrest-kt:3.7.0")
            implementation("io.github.jan-tennert.supabase:auth-kt:3.7.0")
            implementation("io.github.jan-tennert.supabase:storage-kt:3.7.0")
            implementation("io.ktor:ktor-client-core:3.0.0")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
            implementation(libs.kotlinx.datetime)
            
            // Cargador de imágenes Multiplataforma (v1.3.0)
            implementation("media.kamel:kamel-image:1.0.9")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jsMain.dependencies {
            implementation(libs.wrappers.browser)
            implementation("io.ktor:ktor-client-js:3.0.0")
        }
    }
}

/*
dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}
*/