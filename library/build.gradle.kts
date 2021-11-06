dependencies {
  implementation(kotlin("stdlib-jdk8"))
  implementation(libs.bundles.coroutines)

  testImplementation(libs.bundles.koTest)
}

tasks {
  compileKotlin {
    kotlinOptions.moduleName = "yolk-library"
  }
  test {
    useJUnitPlatform()
  }
}
