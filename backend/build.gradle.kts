plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    application
}

group = "com.dermai"
version = "1.0.0"

repositories {
    mavenCentral()
}

val ktorVersion = "2.3.8"
val logbackVersion = "1.4.14"

dependencies {

    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")
    // Ktor Core & Netty Engine
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")

    // JSON & Content Negotiation
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")

    // CORS (Android bağlantısı için)
    implementation("io.ktor:ktor-server-cors-jvm:$ktorVersion")

    // Logging
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    // Database (Exposed ORM & H2/SQLite)
    implementation("org.jetbrains.exposed:exposed-core:0.46.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.46.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.46.0")
    implementation("com.h2database:h2:2.2.224")

    // JWT Authentication & Password Hashing
    implementation("io.ktor:ktor-server-auth:2.3.8")
    implementation("io.ktor:ktor-server-auth-jwt:2.3.8")
    implementation("at.favre.lib:bcrypt:0.10.2")


    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    implementation("io.ktor:ktor-client-cio:2.3.8")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.8")


    implementation("org.postgresql:postgresql:42.7.2")

    // Test
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$ktorVersion")
}

application {
    mainClass.set("com.dermai.ApplicationKt")
}
