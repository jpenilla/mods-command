plugins {
  val indraVersion = "2.1.1"
  id("net.kyori.indra") version indraVersion
  id("net.kyori.indra.git") version indraVersion
  id("net.kyori.indra.checkstyle") version indraVersion
  id("net.kyori.indra.license-header") version indraVersion
  id("quiet-fabric-loom") version "0.12-SNAPSHOT"
}

version = "1.0.9-SNAPSHOT"
  .run { if (endsWith("-SNAPSHOT")) "$this+${indraGit.commit()?.name?.substring(0, 7) ?: error("Could not determine git hash")}" else this }
group = "xyz.jpenilla"
description = "Adds commands to list, search, and get information about installed mods."
val githubUrl = "https://github.com/jpenilla/ModsCommand"

repositories {
  mavenCentral()
  maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") {
    mavenContent { snapshotsOnly() }
  }
  maven("https://oss.sonatype.org/content/repositories/snapshots/") {
    mavenContent { snapshotsOnly() }
  }
  maven("https://maven.fabricmc.net/")
  maven("https://repo.incendo.org/content/repositories/snapshots/") {
    mavenContent {
      snapshotsOnly()
      includeGroup("cloud.commandframework")
    }
  }
}

val minecraftVersion = "1.19"

dependencies {
  minecraft("com.mojang", "minecraft", minecraftVersion)
  mappings(loom.officialMojangMappings())
  modImplementation("net.fabricmc", "fabric-loader", "0.14.8")
  modImplementation("net.fabricmc.fabric-api:fabric-api:0.57.0+1.19")

  modImplementation(include("cloud.commandframework", "cloud-fabric", "1.7.0"))
  implementation(include("cloud.commandframework", "cloud-minecraft-extras", "1.7.0"))

  modImplementation(include("net.kyori", "adventure-platform-fabric", "5.4.0-SNAPSHOT"))

  val configurateVersion = "4.1.2"
  implementation(include("org.spongepowered:configurate-core:$configurateVersion")!!)
  implementation(include("io.leangen.geantyref:geantyref:1.3.13")!!)
  implementation(include("org.spongepowered:configurate-hocon:$configurateVersion")!!)
  implementation(include("com.typesafe:config:1.4.2")!!)
  implementation(include("org.spongepowered:configurate-yaml:$configurateVersion")!!)
  implementation(include("org.yaml", "snakeyaml", "1.+"))

  compileOnly("org.checkerframework", "checker-qual", "3.22.2")
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
  withType<JavaCompile> {
    options.compilerArgs.add("-Xlint:-processing")
  }
}

indra {
  javaVersions {
    target(17)
  }
  github("jpenilla", "ModsCommand")
  apache2License()
}

license {
  header(file("LICENSE_HEADER"))
}
