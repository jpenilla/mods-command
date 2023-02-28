plugins {
  val indraVersion = "3.0.1"
  id("net.kyori.indra") version indraVersion
  id("net.kyori.indra.git") version indraVersion
  id("net.kyori.indra.checkstyle") version indraVersion
  id("net.kyori.indra.license-header") version indraVersion
  id("quiet-fabric-loom") version "1.1-SNAPSHOT"
  id("com.modrinth.minotaur") version "2.7.2"
}

version = "1.1.1"
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

val minecraftVersion = "1.19.3"

val cloudBom: Configuration by configurations.creating
listOf(configurations.implementation, configurations.include, configurations.modImplementation)
  .forEach { it { extendsFrom(cloudBom) } }

dependencies {
  minecraft("com.mojang", "minecraft", minecraftVersion)
  mappings(loom.officialMojangMappings())
  modImplementation("net.fabricmc", "fabric-loader", "0.14.11")
  modImplementation("net.fabricmc.fabric-api:fabric-api:0.68.1+1.19.3")

  cloudBom(platform("cloud.commandframework:cloud-bom:1.8.2"))
  modImplementation(include("cloud.commandframework", "cloud-fabric"))
  implementation(include("cloud.commandframework", "cloud-minecraft-extras"))

  modImplementation(include("net.kyori", "adventure-platform-fabric", "5.6.1"))

  val configurateVersion = "4.1.2"
  implementation(include("org.spongepowered:configurate-core:$configurateVersion")!!)
  implementation(include("io.leangen.geantyref:geantyref:1.3.13")!!)
  implementation(include("org.spongepowered:configurate-hocon:$configurateVersion")!!)
  implementation(include("com.typesafe:config:1.4.2")!!)
  implementation(include("org.spongepowered:configurate-yaml:$configurateVersion")!!)
  implementation(include("org.yaml", "snakeyaml", "1.+"))

  compileOnly("org.checkerframework", "checker-qual", "3.28.0")

  modImplementation("com.terraformersmc:modmenu:5.0.2")
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
  gameVersions.set(listOf(minecraftVersion))
  loaders.set(listOf("fabric"))
  changelog.set(providers.environmentVariable("RELEASE_NOTES"))
  token.set(providers.environmentVariable("MODRINTH_TOKEN"))
}
