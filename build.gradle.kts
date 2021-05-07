plugins {
  `java-library`
  val indraVersion = "2.0.5"
  id("net.kyori.indra") version indraVersion
  id("net.kyori.indra.git") version indraVersion
  id("net.kyori.indra.checkstyle") version indraVersion
  id("net.kyori.indra.license-header") version indraVersion
  id("fabric-loom") version "0.8-SNAPSHOT"
}

version = "1.0.3-SNAPSHOT"
  .run { if (this.endsWith("-SNAPSHOT")) "$this+${indraGit.commit()?.name?.substring(0, 7) ?: error("Could not determine git hash")}" else this }
group = "xyz.jpenilla"
description = "Adds commands to list, search, and get information about installed mods."
val githubUrl = "https://github.com/jpenilla/ModsCommand"

repositories {
  mavenCentral()
  maven("https://maven.fabricmc.net/")
  maven("https://oss.sonatype.org/content/repositories/snapshots/")
  maven("https://repo.incendo.org/content/repositories/snapshots")
  maven("https://repo.jpenilla.xyz/snapshots/")
}

val minecraftVersion = "21w18a"

dependencies {
  minecraft("com.mojang", "minecraft", minecraftVersion)
  mappings(minecraft.officialMojangMappings())
  modImplementation("net.fabricmc", "fabric-loader", "0.11.1")
  modImplementation("net.fabricmc.fabric-api", "fabric-api", "0.34.2+1.17")

  modImplementation(include("cloud.commandframework", "cloud-fabric", "1.5.0-SNAPSHOT"))
  implementation(include("cloud.commandframework", "cloud-minecraft-extras", "1.5.0-SNAPSHOT"))

  modImplementation(include("net.kyori", "adventure-platform-fabric", "4.0.0+1.17-SNAPSHOT") {
    exclude("ca.stellardrift", "colonel")
  })

  modImplementation(include("ca.stellardrift", "confabricate", "2.1.0"))
  implementation(include("org.spongepowered", "configurate-yaml", "4.1.1"))
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
  withType<JavaCompile> {
    options.compilerArgs.add("-Xlint:-processing")
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
