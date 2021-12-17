import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.targets

plugins {
  alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
  targets {
    jvm {
      compilations.all {
        kotlinOptions {
          jvmTarget = "1.8"
        }
      }
    }
    ios()
  }

  sourceSets {
    all {
      languageSettings.optIn("kotlin.time.ExperimentalTime")
    }
    val commonMain by getting {
      dependencies {
        implementation(libs.bundles.coroutines)
        implementation(libs.kotlinDateTime)
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
