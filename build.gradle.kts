import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `maven-publish`
    signing
    idea
    kotlin("jvm") version "1.3.41"
    kotlin("kapt") version "1.3.41"
    kotlin("plugin.spring") version "1.3.41"
    id("org.jmailen.kotlinter") version "1.25.2"
    id("pl.allegro.tech.build.axion-release") version "1.10.2"
    id("com.github.ben-manes.versions") version "0.21.0"
}

val PUBLISH_GROUP_ID by extra("se.svt.oss")
val PUBLISH_VERSION by extra(scmVersion.version)
val PUBLISH_ARTIFACT_ID by extra("flum")

group = PUBLISH_GROUP_ID
project.version = PUBLISH_VERSION

//TO-DO Move to seperate publishing file, kotlin dsl
val signKeyId by extra(System.getenv("SIGNING_KEY_ID") ?: findProperty("signing.keyId") ?: "")
val signPassword by extra(System.getenv("SIGNING_PWD") ?: findProperty("signing.password") ?: "")
val signSecretKeyRingFile by extra(keyfile())
val sonatypeUsername by extra(System.getenv("SONATYPE_USER") ?: findProperty("sonatypeUsername") ?: "")
val sonatypePassword by extra(System.getenv("SONATYPE_PWD") ?: findProperty("sonatypePassword") ?: "")

fun keyfile(): String {

    var key = System.getenv("SIGNING_KEY")
    if (key.isNullOrEmpty()) {
        key = findProperty("signing.secretKeyRingFile")?.toString() ?: ""
    }
    return key
}

apply { from("publishing.gradle") }

tasks.test {
    useJUnitPlatform()
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8", "1.3.41"))
    runtimeOnly(kotlin("reflect", "1.3.41"))

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