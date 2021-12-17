pluginManagement {
  repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
  }
}

rootProject.name = "yolk"

include("library")

enableFeaturePreview("VERSION_CATALOGS")
