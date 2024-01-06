pluginManagement {
  repositories {
    gradlePluginPortal()
    maven("https://maven.fabricmc.net/")
    maven("https://repo.jpenilla.xyz/snapshots/")
  }
}

buildscript {
  configurations.all {
    resolutionStrategy {
      eachDependency {
        if (requested.group == "com.google.code.gson" && requested.name == "gson") {
          useVersion("2.10.1")
          because("project plugins need newer version than foojay-resolver-convention")
        }
      }
    }
  }
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}

rootProject.name = "mods-command"
