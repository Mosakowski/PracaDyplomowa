plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application
}

group = "org.pracainzynierska.sportbooking"
version = "1.0.0"
application {
    mainClass.set("org.pracainzynierska.sportbooking.ApplicationKt")
    
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
    implementation("org.postgresql:postgresql:42.7.2") //sterownik bazy
    implementation("org.jetbrains.exposed:exposed-java-time:0.53.0")
    implementation("org.jetbrains.exposed:exposed-core:0.53.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.53.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.53.0") // opcjonalnie, dla podejścia obiektowego
    implementation("org.jetbrains.exposed:exposed-json:0.53.0") // obsługa JSONB dla grafiku
}