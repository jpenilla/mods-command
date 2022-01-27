plugins {
  `java-library`
  val indraVersion = "2.0.6"
  id("net.kyori.indra") version indraVersion
  id("net.kyori.indra.git") version indraVersion
  id("net.kyori.indra.checkstyle") version indraVersion
  id("net.kyori.indra.license-header") version indraVersion
  id("quiet-fabric-loom") version "0.11-SNAPSHOT"
}

version = "1.0.6-SNAPSHOT"
  .run { if (endsWith("-SNAPSHOT")) "$this+${indraGit.commit()?.name?.substring(0, 7) ?: error("Could not determine git hash")}" else this }
group = "xyz.jpenilla"
description = "Adds commands to list, search, and get information about installed mods."
val githubUrl = "https://github.com/jpenilla/ModsCommand"

repositories {
  mavenCentral()
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

val minecraftVersion = "1.18.1"

dependencies {
  minecraft("com.mojang", "minecraft", minecraftVersion)
  mappings(loom.officialMojangMappings())
  modImplementation("net.fabricmc", "fabric-loader", "0.12.12")
  modImplementation("net.fabricmc.fabric-api:fabric-api:0.45.0+1.18")

  modImplementation(include("cloud.commandframework", "cloud-fabric", "1.6.1"))
  implementation(include("cloud.commandframework", "cloud-minecraft-extras", "1.6.1"))

  modImplementation(include("net.kyori", "adventure-platform-fabric", "5.0.0") {
    exclude("ca.stellardrift", "colonel")
  })

  modImplementation(include("ca.stellardrift", "confabricate", "2.1.0"))
  implementation(include("org.spongepowered", "configurate-yaml", "4.1.2"))
  implementation(include("org.yaml", "snakeyaml", "1.+"))

  compileOnly("org.checkerframework", "checker-qual", "3.21.1")
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
