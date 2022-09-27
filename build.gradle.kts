/*
 * This file is part of the gradle-release plugin.
 *
 * (c) F43nd1r
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 *
 */

plugins {
    kotlin("jvm") version "1.7.10"
    `java-gradle-plugin`
    idea
    `maven-publish`
    id("com.calincosma.gradle-release") version "0.0.27"
//    id("com.gradle.plugin-publish") version "1.0.0"
//    id("com.faendir.gradle.release") version "3.3.1"
}

group="com.calincosma"

repositories {
    mavenLocal()
    mavenCentral()
//    maven {
//        url = uri("https://plugins.gradle.org/m2/")
//    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.7")
    val junitVersion: String by project
    val striktVersion: String by project
    val mockkVersion: String by project
    val jgitVersion: String by project
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testImplementation("io.strikt:strikt-core:$striktVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation(gradleKotlinDsl())
    testImplementation("org.eclipse.jgit:org.eclipse.jgit:$jgitVersion")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

gradlePlugin {
    plugins {
        create("releasePlugin") {
            id = "com.calincosma.gradle-release"
            displayName = "Gradle Release Plugin"
            description = "gradle-release is a plugin for providing a Maven-like release process to project using Gradle."
            implementationClass = "net.researchgate.release.ReleasePlugin"
        }
    }
}

//pluginBundle {
//    website = "https://github.com/calincosma/gradle-release"
//    vcsUrl = "https://github.com/calincosma/gradle-release"
//    tags = listOf("release", "git")
//}

publishing {
    publications {
        create<MavenPublication>("plugin") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "localPluginRepository"
            url = uri("../local-plugin-repository")
        }
    }
}

release {
    tagTemplate = "v\$version"
    failOnCommitNeeded = false
    failOnUnversionedFiles = false
    failOnPublishNeeded = false
    git {
        requireBranch = "kotlin"
    }
}

tasks.named("afterReleaseBuild").configure {
    dependsOn("publish")
}

