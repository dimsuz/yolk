plugins {
  id(libs.plugins.kotlinMultiplatform.get().pluginId)
  id(libs.plugins.dokka.get().pluginId)
  `maven-publish`
}

kotlin {
  targets {
    jvm {
      compilations.all {
        kotlinOptions {
          jvmTarget = "1.8"
          moduleName = "yolk-sqldelight-extensions"
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
        api(project(":yolk"))
        api(libs.sqlDelightRuntime)
        implementation(libs.bundles.coroutines)
      }
    }

    val commonTest by getting {
      dependencies {
        implementation(libs.bundles.koTestCommon)
        implementation(libs.turbine)
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
