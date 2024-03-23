import me.modmuss50.mpp.ReleaseType

plugins {
  val indraVersion = "3.1.3"
  id("net.kyori.indra") version indraVersion
  id("net.kyori.indra.git") version indraVersion
  id("net.kyori.indra.checkstyle") version indraVersion
  id("net.kyori.indra.licenser.spotless") version indraVersion
  id("quiet-fabric-loom") version "1.6-SNAPSHOT"
  id("me.modmuss50.mod-publish-plugin") version "0.4.5"
}

version = "1.1.5-SNAPSHOT"
  .run { if (endsWith("-SNAPSHOT")) "$this+${indraGit.commit()?.name?.substring(0, 7) ?: error("Could not determine git hash")}" else this }
group = "xyz.jpenilla"
description = "Adds commands to list, search, and get information about installed mods."
val githubUrl = "https://github.com/jpenilla/mods-command"

repositories {
  mavenCentral()
  sonatype.s01Snapshots()
  sonatype.ossSnapshots()
  maven("https://maven.fabricmc.net/")
  maven("https://maven.terraformersmc.com/releases/")
}

val minecraftVersion = "1.20.4"

val bom: Configuration by configurations.creating
listOf(configurations.implementation, configurations.include, configurations.modImplementation)
  .forEach { it { extendsFrom(bom) } }

dependencies {
  minecraft("com.mojang", "minecraft", minecraftVersion)
  mappings(loom.officialMojangMappings())
  modImplementation("net.fabricmc:fabric-loader:0.15.7")
  modImplementation("net.fabricmc.fabric-api:fabric-api:0.92.1+1.20.4")

  bom(platform("org.incendo:cloud-bom:2.0.0-beta.4"))
  bom(platform("org.incendo:cloud-minecraft-bom:2.0.0-beta.5"))
  modImplementation("org.incendo:cloud-fabric:2.0.0-beta.4")
  include("org.incendo:cloud-fabric:2.0.0-beta.4")
  implementation("org.incendo:cloud-minecraft-extras")
  include("org.incendo:cloud-minecraft-extras")

  modImplementation(include("net.kyori", "adventure-platform-fabric", "5.11.0"))

  bom(platform("org.spongepowered:configurate-bom:4.2.0-SNAPSHOT"))
  implementation(include("org.spongepowered", "configurate-core"))
  implementation(include("io.leangen.geantyref:geantyref:1.3.13")!!)
  implementation(include("org.spongepowered", "configurate-hocon"))
  implementation(include("org.spongepowered", "configurate-yaml"))

  compileOnly("org.checkerframework", "checker-qual", "3.42.0")

  modImplementation("com.terraformersmc:modmenu:9.0.0")
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

indraSpotlessLicenser {
  licenseHeaderFile(rootProject.file("LICENSE_HEADER"))
}

publishMods.modrinth {
  projectId = "PExmWQV8"
  type = ReleaseType.STABLE
  file = tasks.remapJar.flatMap { it.archiveFile }
  changelog = providers.environmentVariable("RELEASE_NOTES")
  accessToken = providers.environmentVariable("MODRINTH_TOKEN")
  modLoaders.add("fabric")
  minecraftVersions.add(minecraftVersion)
}
