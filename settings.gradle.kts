pluginManagement {
  repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
  }

  resolutionStrategy {
    eachPlugin {
      if (requested.id.id == "com.squareup.sqldelight") {
        useModule("com.squareup.sqldelight:gradle-plugin:${requested.version}")
      }
    }
  }
}

rootProject.name = "yolk"

include("yolk")
include("yolk-sqldelight-extensions")

enableFeaturePreview("VERSION_CATALOGS")
