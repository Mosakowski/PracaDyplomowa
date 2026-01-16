import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import com.android.build.api.dsl.LibraryExtension //  WAŻNY IMPORT DLA SHARED

val localProperties = project.rootProject.file("local.properties")
val isAndroidAvailable = localProperties.exists()

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary) apply false // apply false
    alias(libs.plugins.kotlinSerialization)
}

if (isAndroidAvailable) {
    apply(plugin = "com.android.library")
}

kotlin {
    if (isAndroidAvailable) {
        androidTarget {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_11)
            }
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    jvm()

    js {
        browser()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
            implementation("io.ktor:ktor-client-core:3.0.1")
            implementation("io.ktor:ktor-client-content-negotiation:3.0.1")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.1")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        if (isAndroidAvailable) {
            val androidMain by getting {
                dependencies {
                    // Silnik dla Androida w wersji pasującej do Core (3.0.1)
                    implementation("io.ktor:ktor-client-android:3.0.1")
                }
            }
        }
    }
}

//  ZMIANA: extensions.configure<LibraryExtension>
if (isAndroidAvailable) {
    extensions.configure<LibraryExtension> {
        namespace = "org.pracainzynierska.sportbooking.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
        defaultConfig {
            minSdk = libs.versions.android.minSdk.get().toInt()
        }
    }
}