import java.util.*
import org.gradle.api.publish.PublishingExtension
import java.lang.System.getenv


//We need to differentiate if we are building local or if Github Actions are used to build the Project!
var user_name : String
var user_pat : String
var maven_url : String
var current_spacecenter_tag : String = "1.0.0"
if(System.getenv("GITHUB_REPOSITORY") != null){
    //if we are in github we use the environmental variables provided by github
    user_name = System.getenv("GITHUB_ACTOR")
    user_pat = System.getenv("GITHUB_TOKEN")
    maven_url = "https://maven.pkg.github.com/${System.getenv("GITHUB_TOKEN")}"
    current_spacecenter_tag = System.getenv("GITHUB_TAG")
}else{
    //else we need to use our cutsom credentials stored in the local.properties file
    val local = Properties()
    local.load(rootProject.file("local.properties").inputStream())
    user_name = local.getProperty("github.user") //
    user_pat = local.getProperty("github.token") // pat with scope: read packages
    maven_url = local.getProperty("maven.url")
}


plugins {
    id("maven-publish")
    kotlin("multiplatform") version "1.5.0"
}


allprojects {
    group = "de.urbanistic"
    version = current_spacecenter_tag
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


publishing {
    repositories {
        maven {
            name = "github"
            url = uri(maven_url)
            credentials(PasswordCredentials::class) {
                username = user_name
                password = user_pat
            }
        }
    }
}
