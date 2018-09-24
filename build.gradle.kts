import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.0-rc-57"
//    kotlin("jvm") version "1.3-M2"
}

group = "co.thecodewarrior.logisimcpu"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("http://dl.bintray.com/kotlin/kotlin-eap")
}

dependencies {
    compile(kotlin("stdlib-jdk8", "1.3.0-rc-57"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs = listOf("-Xuse-experimental=kotlin.ExperimentalUnsignedTypes")
}