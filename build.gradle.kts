plugins {
    kotlin("jvm") version "1.8.0"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-runner-junit5:5.6.2")
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.6.2")

    implementation("com.sksamuel.tribune:tribune-ktor-jvm:1.2.4")
    implementation("com.sksamuel.tribune:tribune-core-jvm:1.2.4")
    implementation("com.sksamuel.tribune:tribune-datetime:1.2.4")

    implementation("io.arrow-kt:arrow-core:1.1.5")
    implementation("io.arrow-kt:arrow-fx-coroutines:1.1.5")
    implementation("io.arrow-kt:arrow-fx-stm:1.1.5")
    implementation("io.arrow-kt:suspendapp-jvm:0.4.1-alpha.5")
    implementation("io.arrow-kt:suspendapp-ktor-jvm:0.4.1-alpha.5")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}