plugins {
  `java-library`
  val indraVersion = "2.0.5"
  id("net.kyori.indra") version indraVersion
  id("net.kyori.indra.checkstyle") version indraVersion
  id("net.kyori.indra.license-header") version indraVersion
  id("fabric-loom") version "0.8-SNAPSHOT"
}

version = "1.0.2-SNAPSHOT"
group = "xyz.jpenilla"
description = "Adds commands to list, search, and get information about installed mods."
val githubUrl = "https://github.com/jpenilla/ModsCommand"

repositories {
  mavenCentral()
  maven("https://maven.fabricmc.net/")
  maven("https://oss.sonatype.org/content/repositories/snapshots/")
  maven("https://repo.incendo.org/content/repositories/snapshots")
}

val minecraftVersion = "1.16.5"

dependencies {
  minecraft("com.mojang", "minecraft", minecraftVersion)
  mappings(minecraft.officialMojangMappings())
  modImplementation("net.fabricmc", "fabric-loader", "0.11.1")
  //modImplementation("net.fabricmc.fabric-api", "fabric-api", "0.32.5+1.16")

  modImplementation(include("cloud.commandframework", "cloud-fabric", "1.5.0-SNAPSHOT"))
  implementation(include("cloud.commandframework", "cloud-minecraft-extras", "1.5.0-SNAPSHOT"))

  modImplementation(include("net.kyori", "adventure-platform-fabric", "4.0.0-SNAPSHOT") {
    exclude("ca.stellardrift", "colonel")
  })

  modImplementation(include("ca.stellardrift", "confabricate", "2.0.3"))
  implementation(include("org.spongepowered", "configurate-yaml", "4.0.0"))
  implementation(include("org.yaml", "snakeyaml", "1.+"))

  compileOnly("org.checkerframework", "checker-qual", "3.13.0")
}

tasks {
  processResources {
    filesMatching("fabric.mod.json") {
      expand(
        "version" to project.version,
        "github_url" to githubUrl,
        "description" to project.description
      )
    }
  }
  jar {
    from("LICENSE") {
      rename { "LICENSE_${project.name}" }
    }
  }
  remapJar {
    archiveFileName.set("${project.name}-mc$minecraftVersion-${project.version}.jar")
  }
}

indra {
  javaVersions {
    target(8)
    minimumToolchain(16)
  }
  github("jpenilla", "ModsCommand")
  apache2License()
}

license {
  header(file("LICENSE_HEADER"))
}
