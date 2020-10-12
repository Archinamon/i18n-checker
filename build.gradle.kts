plugins {
    kotlin("multiplatform") version "1.4.0"
}

group = "me.archinamon"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvm()
    macosX64 {
        binaries {
            executable {
                entryPoint("${project.group}.i18n.runner.main")
            }
        }
    }

    /*
    *  Targets configuration omitted.
    *  To find out how to configure the targets, please follow the link:
    *  https://kotlinlang.org/docs/reference/building-mpp-with-gradle.html#setting-up-targets
    */

    sourceSets {
        all {
            languageSettings.enableLanguageFeature("InlineClasses")
        }

        @Suppress("UNUSED_VARIABLE")
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
    }

    targets.all {
        compilations.all {
            kotlinOptions {
                freeCompilerArgs = listOf(
                    "-XXLanguage:+InlineClasses",
                    "-Xjsr305=strict",
                    "-Xuse-experimental=kotlin.Experimental"
                )
            }
        }
    }
}

tasks {
    create("runJvm", JavaExec::class) {
        dependsOn("compileKotlinJvm")

        group = "run"
        main = "${project.group}.i18n.runner.RunnerKt"
        classpath = configurations["jvmRuntimeClasspath"] +
            project.tasks["compileKotlinJvm"].outputs.files +
            project.tasks["jvmProcessResources"].outputs.files
        workingDir = buildDir

        args("-i", "/Users/archinamon/Downloads/Telegram Desktop/New localization v2020 EN_RU_VI_ES_IT - в работе.csv")
    }
}