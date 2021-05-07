import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    kotlin("jvm") version "1.4.32"
    kotlin("kapt") version "1.4.32"
    kotlin("plugin.spring") version "1.4.32"
            ""
    id("org.jmailen.kotlinter") version "3.4.3"
    id("pl.allegro.tech.build.axion-release") version "1.10.2"
    id("com.github.ben-manes.versions") version "0.21.0"
    id("se.svt.oss.gradle-yapp-publisher-plugin") version "0.1.10"
}

group = "se.svt.oss"
project.version = scmVersion.version

tasks.test {
    useJUnitPlatform()
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8", "1.4.32"))
    implementation(kotlin("reflect", "1.4.32"))

    api("com.squareup.okhttp3:mockwebserver:3.10.0")
    implementation("io.github.microutils:kotlin-logging:1.6.26")
    implementation("me.alexpanov:free-port-finder:1.1.1")
    implementation("org.assertj:assertj-core:3.11.1")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    implementation("org.junit.jupiter:junit-jupiter-engine:5.5.2")
    implementation("org.junit.jupiter:junit-jupiter-api:5.5.2")
    implementation("org.junit.jupiter:junit-jupiter-params:5.5.2")
    testImplementation("io.mockk:mockk:1.9.3")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
    gradleVersion = "6.8.3"
}
