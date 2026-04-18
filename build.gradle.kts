import me.modmuss50.mpp.ReleaseType
import xyz.jpenilla.resourcefactory.fabric.Environment

plugins {
  val indraVersion = "4.0.0"
  id("net.kyori.indra") version indraVersion
  id("net.kyori.indra.git") version indraVersion
  id("net.kyori.indra.checkstyle") version indraVersion
  id("net.kyori.indra.licenser.spotless") version indraVersion
  id("xyz.jpenilla.quiet-fabric-loom") version "1.16-SNAPSHOT"
  id("me.modmuss50.mod-publish-plugin") version "1.1.0"
  id("xyz.jpenilla.resource-factory-fabric-convention") version "1.3.1"
}

decorateVersion()

repositories {
  mavenCentral {
    mavenContent { releasesOnly() }
  }
  maven("https://repo.jpenilla.xyz/snapshots/") {
    mavenContent {
      snapshotsOnly()
      includeGroup("xyz.jpenilla")
    }
  }
  maven("https://central.sonatype.com/repository/maven-snapshots/") {
    mavenContent { snapshotsOnly() }
  }
  maven("https://maven.fabricmc.net/")
  maven("https://maven.terraformersmc.com/releases/")
}

val bom: Configuration by configurations.creating
listOf(configurations.implementation, configurations.include)
  .forEach { it { extendsFrom(bom) } }

dependencies {
  minecraft(libs.minecraft)
  implementation(libs.fabricLoader)
  implementation(libs.fabricApi)

  bom(platform(libs.cloudBom))
  bom(platform(libs.cloudMinecraftBom))
  implementation(libs.cloudFabric)
  include(libs.cloudFabric)
  implementation(libs.cloudMinecraftExtras)
  include(libs.cloudMinecraftExtras)

  implementation(libs.adventureFabric)

  bom(platform(libs.configurateBom))
  implementation(libs.configurateCore)
  include(libs.configurateCore)
  implementation(libs.configurateHocon)
  include(libs.configurateHocon)
  implementation(libs.configurateYaml)
  include(libs.configurateYaml)

  implementation(libs.modmenu)
}

fabricModJson {
  name = "Mods Command"
  author("jmp")
  contact {
    val githubUrl = "https://github.com/jpenilla/mods-command"
    homepage = githubUrl
    sources = githubUrl
    issues = "$githubUrl/issues"
  }
  icon("assets/mods-command/icon.png")
  environment = Environment.ANY
  mainEntrypoint("xyz.jpenilla.modscommand.ModsCommandModInitializer")
  clientEntrypoint("xyz.jpenilla.modscommand.ModsCommandClientModInitializer")
  apache2License()
  depends("fabric-api", "*")
  depends("fabricloader", ">=${libs.versions.fabricLoader.get()}")
  depends("minecraft", ">=${libs.versions.minecraft.get()}")
  depends("cloud", "*")
  depends("adventure-platform-fabric", "*")
}

tasks {
  jar {
    val projectName = project.name
    from("LICENSE") {
      rename { "LICENSE_${projectName}" }
    }
    archiveFileName.set("${project.name}-mc${libs.versions.minecraft.get()}-${project.version}.jar")
  }
  withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:-processing")
  }
}

indra {
  javaVersions {
    target(25)
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
  file = tasks.jar.flatMap { it.archiveFile }
  changelog = providers.environmentVariable("RELEASE_NOTES")
  accessToken = providers.environmentVariable("MODRINTH_TOKEN")
  modLoaders.add("fabric")
  minecraftVersions.add(libs.versions.minecraft)
  requires("fabric-api")
  requires("adventure-platform-mod")
}

fun decorateVersion() {
  val versionString = version as String
  val decorated = if (versionString.endsWith("-SNAPSHOT")) {
    "$versionString+${indraGit.commit().orNull?.name?.substring(0, 7) ?: error("Could not determine git hash")}"
  } else {
    versionString
  }
  version = decorated
}
