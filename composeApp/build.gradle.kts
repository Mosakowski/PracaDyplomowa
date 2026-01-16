import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import com.android.build.api.dsl.ApplicationExtension // 👈 WAŻNY IMPORT

val localProperties = project.rootProject.file("local.properties")
val isAndroidAvailable = localProperties.exists()

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication) apply false //  apply false musi zostać dla rendera
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

if (isAndroidAvailable) {
    apply(plugin = "com.android.application")
}

kotlin {
    if (isAndroidAvailable) {
        androidTarget {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_11)
            }
        }
    }

    js {
        browser()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        if (isAndroidAvailable) {
            val androidMain by getting {
                dependencies {
                    implementation(compose.preview)
                    implementation(libs.androidx.activity.compose)
                }
            }
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(projects.shared)
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
            implementation(compose.materialIconsExtended)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

//  ZMIANA: Zamiast "android {}", używamy "extensions.configure"
if (isAndroidAvailable) {
    extensions.configure<ApplicationExtension> {
        namespace = "org.pracainzynierska.sportbooking"
        compileSdk = libs.versions.android.compileSdk.get().toInt()

        defaultConfig {
            applicationId = "org.pracainzynierska.sportbooking"
            minSdk = libs.versions.android.minSdk.get().toInt()
            targetSdk = libs.versions.android.targetSdk.get().toInt()
            versionCode = 1
            versionName = "1.0"
        }

        sourceSets {
            getByName("main") {
                manifest.srcFile("src/androidMain/AndroidManifest.xml")
                res.srcDirs("src/androidMain/res")
                resources.srcDirs("src/commonMain/resources")
            }
        }

        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }
        }
        buildTypes {
            getByName("release") {
                isMinifyEnabled = false
            }
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
    }
}

dependencies {
    if (isAndroidAvailable) {
        //  ZMIANA: Zamiast debugImplementation(...) używamy add("debugImplementation", ...)
        add("debugImplementation", compose.uiTooling)
    }
}