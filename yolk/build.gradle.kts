plugins {
  id(libs.plugins.kotlinMultiplatform.get().pluginId)
  id(libs.plugins.dokka.get().pluginId) apply false
  `maven-publish`
}

kotlin {
  targets {
    jvm {
      compilations.all {
        kotlinOptions {
          jvmTarget = "1.8"
          moduleName = "yolk-library"
        }
      }
    }
    ios()
    iosSimulatorArm64()
  }

  sourceSets {
    all {
      languageSettings.optIn("kotlin.time.ExperimentalTime")
    }
    val commonMain by getting {
      dependencies {
        api(libs.bundles.coroutines)
        api(libs.kotlinDateTime)
      }
    }

    val commonTest by getting {
      dependencies {
        implementation(libs.bundles.koTestCommon)
      }
    }

    val jvmMain by getting {
      dependencies {
        implementation(kotlin("stdlib-jdk8"))
      }
    }

    val jvmTest by getting {
      dependencies {
        implementation(libs.bundles.koTestJvm)
      }
    }

    val iosMain by getting {
      dependencies {
      }
    }
    val iosTest by getting {
      dependencies {
      }
    }
    val iosSimulatorArm64Main by getting {
      dependsOn(iosMain)
    }
  }
}

tasks.named<Test>("jvmTest") {
  useJUnitPlatform()
  testLogging {
    showExceptions = true
    showStandardStreams = true
    events = setOf(
      org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
      org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
    )
    exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
  }
}
