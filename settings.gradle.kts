pluginManagement {
  repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
  }
}

rootProject.name = "yolk"

include("yolk")

enableFeaturePreview("VERSION_CATALOGS")
