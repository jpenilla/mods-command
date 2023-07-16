plugins {
  val indraVersion = "3.0.1"
  id("net.kyori.indra") version indraVersion
  id("net.kyori.indra.git") version indraVersion
  id("net.kyori.indra.checkstyle") version indraVersion
  id("net.kyori.indra.license-header") version indraVersion
  id("quiet-fabric-loom") version "1.2-SNAPSHOT"
  id("com.modrinth.minotaur") version "2.7.5"
}

version = "1.1.4-SNAPSHOT"
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

val minecraftVersion = "1.20.1"

val bom: Configuration by configurations.creating
listOf(configurations.implementation, configurations.include, configurations.modImplementation)
  .forEach { it { extendsFrom(bom) } }

dependencies {
  minecraft("com.mojang", "minecraft", minecraftVersion)
  mappings(loom.officialMojangMappings())
  modImplementation("net.fabricmc", "fabric-loader", "0.14.21")
  modImplementation("net.fabricmc.fabric-api:fabric-api:0.83.0+1.20.1")

  bom(platform("cloud.commandframework:cloud-bom:1.8.3"))
  modImplementation(include("cloud.commandframework", "cloud-fabric"))
  implementation(include("cloud.commandframework", "cloud-minecraft-extras"))

  modImplementation(include("net.kyori", "adventure-platform-fabric", "5.9.0"))

  bom(platform("org.spongepowered:configurate-bom:4.1.2"))
  implementation(include("org.spongepowered", "configurate-core"))
  implementation(include("io.leangen.geantyref:geantyref:1.3.13")!!)
  implementation(include("org.spongepowered", "configurate-hocon"))
  implementation(include("com.typesafe:config:1.4.2")!!)
  implementation(include("org.spongepowered", "configurate-yaml"))
  implementation(include("org.yaml", "snakeyaml", "1.+"))

  compileOnly("org.checkerframework", "checker-qual", "3.35.0")

  modImplementation("com.terraformersmc:modmenu:7.0.1")
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

modrinth {
  projectId.set("PExmWQV8")
  versionType.set("release")
  file.set(tasks.remapJar.flatMap { it.archiveFile })
  changelog.set(providers.environmentVariable("RELEASE_NOTES"))
  token.set(providers.environmentVariable("MODRINTH_TOKEN"))
}
