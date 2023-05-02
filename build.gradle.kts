import java.util.*

plugins {
    id("maven-publish")
    kotlin("multiplatform") version "1.8.21"
    kotlin("plugin.serialization") version "1.8.21"
    id("org.jlleitschuh.gradle.ktlint") version "11.3.2"
}

allprojects {
    group = "de.urbanistic"
    version = "2.2.4"
}

repositories {
    mavenCentral()
}
kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }

    js(IR) { // or: LEGACY, BOTH see:https://kotlinlang.org/docs/reference/js-ir-compiler.html
        useCommonJs()
        binaries.library()
        browser {
            binaries.executable()
            testTask {
                enabled = true
                useKarma {
                    useChromeHeadless()
                }
            }
        }
        generateTypeScriptDefinitions()
    }

    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
            }
        }
        val jvmMain by getting
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
        val jsMain by getting
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
        val nativeMain by getting
        val nativeTest by getting
    }
}

publishing {
    repositories {
        maven {
            val local = Properties()
            local.load(rootProject.file("local.properties").inputStream())
            val user_name_repsy = local.getProperty("repsy.user")
            val user_pw_repsy = local.getProperty("repsy.pw")
            val maven_url_repsy = local.getProperty("repsy.url")

            name = "repsi"
            url = uri(maven_url_repsy)
            credentials(PasswordCredentials::class) {
                username = user_name_repsy
                password = user_pw_repsy
            }
        }
    }
}
