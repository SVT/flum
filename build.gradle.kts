plugins {
    idea
    kotlin("jvm") version "1.6.21"
    kotlin("kapt") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
    id("org.jmailen.kotlinter") version "3.10.0"
    id("pl.allegro.tech.build.axion-release") version "1.13.7"
    id("se.ascp.gradle.gradle-versions-filter") version "0.1.16"
    id("se.svt.oss.gradle-yapp-publisher") version "0.1.18"
}

scmVersion.tag.prefix = "release"
scmVersion.tag.versionSeparator = "-"

group = "se.svt.oss"
project.version = scmVersion.version

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation(kotlin("reflect"))

    api("com.squareup.okhttp3:mockwebserver:3.14.9")
    implementation("io.github.microutils:kotlin-logging:2.1.23")
    implementation("me.alexpanov:free-port-finder:1.1.1")
    implementation("org.assertj:assertj-core:3.22.0")

    testImplementation(kotlin("test-junit5"))
    implementation("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    implementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    implementation("org.junit.jupiter:junit-jupiter-params:5.8.2")
    testImplementation("io.mockk:mockk:1.12.4")
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
    gradleVersion = "7.4.2"
}
