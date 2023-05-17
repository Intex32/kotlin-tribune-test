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
    implementation("com.sksamuel.tribune:tribune-ktor-jvm:1.2.4")
    implementation("com.sksamuel.tribune:tribune-core-jvm:1.2.4")
//    implementation("com.sksamuel.tribune:tribune-datetime-jvm:1.2.4")
    implementation("com.sksamuel.tribune:tribune-datetime:1.2.4")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("MainKt")
}