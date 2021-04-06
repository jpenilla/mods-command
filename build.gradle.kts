plugins {
  id("fabric-loom") version "0.7-SNAPSHOT"
  `maven-publish`
  `java-library`
}

version = "0.1.0"
group = "xyz.jpenilla"
description = "Adds commands to list and get info about installed mods."
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
  //modImplementation(include("ca.stellardrift", "confabricate", "2.0.3"))
  implementation(include("org.spongepowered", "configurate-core", "4.0.0"))
  implementation(include("org.spongepowered", "configurate-yaml", "4.0.0"))
  implementation(include("org.yaml", "snakeyaml", "1.+"))
  compileOnly("org.checkerframework", "checker-qual", "3.11.0")
}

tasks {
  processResources {
    filesMatching("fabric.mod.json") {
      mapOf(
        "\${VERSION}" to project.version as String,
        "\${GITHUB_URL}" to githubUrl,
        "\${DESCRIPTION}" to project.description as String
      ).entries.forEach { (k, v) ->
        filter { it.replace(k, v) }
      }
    }
  }
  withType<JavaCompile> {
    options.encoding = Charsets.UTF_8.toString()
  }
  jar {
    from("license.txt") {
      rename { "license_${project.name}.txt" }
    }
  }
  remapJar {
    archiveFileName.set("${project.name}-mc$minecraftVersion-${project.version}.jar")
  }
}

java {
  sourceCompatibility = JavaVersion.toVersion(8)
  targetCompatibility = JavaVersion.toVersion(8)
  withSourcesJar()
}
