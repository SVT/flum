import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `maven-publish`
    idea
    kotlin("jvm") version "1.3.41"
    kotlin("kapt") version "1.3.41"
    kotlin("plugin.spring") version "1.3.41"
    id("org.jmailen.kotlinter") version "1.25.2"
    id("pl.allegro.tech.build.axion-release") version "1.10.2"
    id("com.github.ben-manes.versions") version "0.21.0"
    id("com.jfrog.bintray") version "1.8.4"
}

apply {
    from("bintray.gradle")
    from("publishing.gradle")
}


group = "se.svt.oss"
version = scmVersion.version

tasks.test {
    useJUnitPlatform()
}

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8", "1.3.41"))
    runtimeOnly(kotlin("reflect", "1.3.41"))

    implementation("com.squareup.okhttp3:mockwebserver:3.10.0")
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
    gradleVersion = "5.5.1"
}