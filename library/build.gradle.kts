dependencies {
  implementation(kotlin("stdlib-jdk8"))
  implementation(libs.bundles.coroutines)
}

tasks {
  compileKotlin {
    kotlinOptions.moduleName = "yolk-library"
  }
}
