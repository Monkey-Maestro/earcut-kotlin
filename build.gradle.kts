import java.util.*
import org.gradle.api.publish.PublishingExtension
import java.lang.System.getenv

plugins {
    id("maven-publish")
    kotlin("multiplatform") version "1.5.0"
}


allprojects {
    group = "de.urbanistic"
    version = System.getenv("GITHUB_REF")?.split('/')?.last() ?: "development"
    //version = "1.0.1"
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
    js(IR) {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                    webpackConfig.cssSupport.enabled = true
                }
            }
        }
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

//load local propertie file
val local = Properties()
val localProperties: File = rootProject.file("local.properties")
if (localProperties.exists()) {
    localProperties.inputStream().use { local.load(it) }
}




publishing {
    val gitLabPrivateToken: String = local.getProperty("gitLabPrivateToken")
    repositories {
        maven {
            url = uri("https://gitlab.com/api/v4/projects/21979444/packages/maven")
            name = "GitLab"
            credentials(HttpHeaderCredentials::class) {
                name = "Private-Token"
                value = gitLabPrivateToken
            }
            authentication {
                create<HttpHeaderAuthentication>("header")
            }
        }
    }
}



getenv("GITHUB_REPOSITORY")?.let {
    publishing {
        repositories {
            maven {
                name = "github"
                url = uri("https://maven.pkg.github.com/$it")
                credentials(PasswordCredentials::class){}
            }
        }
    }
}