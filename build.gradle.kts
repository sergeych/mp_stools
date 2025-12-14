//@file:Suppress("UNUSED_VARIABLE")

plugins {
    kotlin("multiplatform") version "2.2.21"
    kotlin("plugin.serialization") version "2.2.21"
    `maven-publish`
    id("org.jetbrains.dokka") version "1.9.20"
}

group = "net.sergeych"
version = "1.6.1"

val serialization_version = "1.9.0"

repositories {
    mavenCentral()
}

kotlin {
    jvm()
    js {
        browser {}
    }

    wasmJs {
        browser()
        binaries.executable()
    }

    macosArm64()
    iosX64()
    iosArm64()
    macosX64()
    iosSimulatorArm64()
    linuxX64()
    linuxArm64()

    wasmJs {
        browser()
        binaries.executable()
    }

    mingwX64() {
        binaries.staticLib {
            baseName = "mp_stools"
        }
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.ExperimentalUnsignedTypes")
            languageSettings.optIn("kotlin.time.ExperimentalTime")

        }
        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:$serialization_version")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
                api("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
            }
        }
        val jvmMain by getting
        val jvmTest by getting
        val jsMain by getting
        val jsTest by getting
//        val nativeMain by getting
//        val nativeTest by getting
        val wasmJsMain by getting
        val wasmJsTest by getting
    }
}

publishing {
    repositories {
        maven {
            val mavenUser: String by project
            val mavenPassword: String by project
            url = uri("https://maven.universablockchain.com/")
            credentials {
                username = mavenUser
                password = mavenPassword
            }
        }

    }
}

tasks.dokkaHtml.configure {
    outputDirectory.set(buildDir.resolve("dokka"))
    dokkaSourceSets {
//        configureEach {
//            includes.from("docs/bipack.md")
//        }
    }
}


