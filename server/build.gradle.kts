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
    implementation("io.ktor:ktor-server-content-negotiation:3.3.1")
    implementation("io.ktor:ktor-server-core:3.3.1")
    implementation("io.ktor:ktor-server-core:3.3.1")
    implementation("io.ktor:ktor-serialization-gson:3.3.1")
    implementation("io.ktor:ktor-server-content-negotiation:3.3.1")
    implementation("io.ktor:ktor-server-core:3.3.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.9")
    implementation("io.ktor:ktor-server-cors:2.3.9")
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
    implementation("org.postgresql:postgresql:42.7.2") //sterownik bazy
    implementation("org.jetbrains.exposed:exposed-java-time:0.53.0")
    implementation("org.jetbrains.exposed:exposed-core:0.53.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.53.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.53.0") // opcjonalnie, dla podejścia obiektowego
    implementation("org.jetbrains.exposed:exposed-json:0.53.0") // obsługa JSONB dla grafiku
    implementation("org.mindrot:jbcrypt:0.4") //bcrypt, algorytm, który zamienia hasło w ciąg znaków niemożliwy do odwrócenia.
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
}