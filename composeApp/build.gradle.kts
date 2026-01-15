import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

//  ZMIANA 1: Sprawdzamy, czy istnieje plik local.properties (jest lokalnie, nie ma w Dockerze)
val localProperties = project.rootProject.file("local.properties")
val isAndroidAvailable = localProperties.exists()

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    //  ZMIANA 2: Dodajemy "apply false" - nie włączamy Androida automatycznie
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

// 👇ZMIANA 3: Włączamy plugin Androida TYLKO jeśli mamy środowisko (lokalnie)
if (isAndroidAvailable) {
    apply(plugin = "com.android.application")
}

kotlin {
    //  ZMIANA 4: Konfigurujemy Androida TYLKO w bloku if
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
        // ZMIANA 5: Zależności Androida też wrzucamy w if (dla bezpieczeństwa)
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

//  ZMIANA 6: Cała sekcja konfiguracji Androida w klamrze if
if (isAndroidAvailable) {
    android {
        namespace = "org.pracainzynierska.sportbooking"
        compileSdk = libs.versions.android.compileSdk.get().toInt()

        defaultConfig {
            applicationId = "org.pracainzynierska.sportbooking"
            minSdk = libs.versions.android.minSdk.get().toInt()
            targetSdk = libs.versions.android.targetSdk.get().toInt()
            versionCode = 1
            versionName = "1.0"
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
    //  ZMIANA 7: Zależność debugowa też tylko dla Androida
    if (isAndroidAvailable) {
        debugImplementation(compose.uiTooling)
    }
}