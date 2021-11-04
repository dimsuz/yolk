plugins {
    kotlin("jvm") version "1.5.31"
}

allprojects {
    repositories {
        mavenCentral()
    }
}

val ktlint: Configuration by configurations.creating

dependencies {
    ktlint(libs.ktlint)
}

val outputDir = "${project.buildDir}/reports/ktlint/"
val inputFiles = project.fileTree(mapOf("dir" to ".", "include" to "**/*.kt"))

val ktlintCheck by tasks.creating(JavaExec::class) {
    inputs.files(inputFiles)
    outputs.dir(outputDir)

    description = "Check Kotlin code style."
    classpath = ktlint
    group = "verification"
    main = "com.pinterest.ktlint.Main"
    args = listOf("**/*.kt")
}

val ktlintFormat by tasks.creating(JavaExec::class) {
    inputs.files(inputFiles)
    outputs.dir(outputDir)

    description = "Fix Kotlin code style deviations."
    classpath = ktlint
    group = "verification"
    main = "com.pinterest.ktlint.Main"
    args = listOf("-F", "**/*.kt")
}

subprojects {
    apply(plugin = "kotlin")

    tasks {
        compileKotlin {
            kotlinOptions.jvmTarget = "1.8"
        }
        compileTestKotlin {
            kotlinOptions.jvmTarget = "1.8"
        }
    }
}
