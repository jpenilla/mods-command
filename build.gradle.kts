import me.modmuss50.mpp.ReleaseType
import xyz.jpenilla.resourcefactory.fabric.Environment

plugins {
  val indraVersion = "3.1.3"
  id("net.kyori.indra") version indraVersion
  id("net.kyori.indra.git") version indraVersion
  id("net.kyori.indra.checkstyle") version indraVersion
  id("net.kyori.indra.licenser.spotless") version indraVersion
  id("quiet-fabric-loom") version "1.6-SNAPSHOT"
  id("me.modmuss50.mod-publish-plugin") version "0.5.2"
  id("xyz.jpenilla.resource-factory-fabric-convention") version "1.1.2"
}

decorateVersion()

repositories {
  mavenCentral()
  sonatype.s01Snapshots()
  sonatype.ossSnapshots()
  maven("https://maven.fabricmc.net/")
  maven("https://maven.terraformersmc.com/releases/")
}

val bom: Configuration by configurations.creating
listOf(configurations.implementation, configurations.include, configurations.modImplementation)
  .forEach { it { extendsFrom(bom) } }

dependencies {
  minecraft(libs.minecraft)
  mappings(loom.officialMojangMappings())
  modImplementation(libs.fabricLoader)
  modImplementation(libs.fabricApi)

  bom(platform(libs.cloudBom))
  bom(platform(libs.cloudMinecraftBom))
  modImplementation(libs.cloudFabric)
  include(libs.cloudFabric)
  implementation(libs.cloudMinecraftExtras)
  include(libs.cloudMinecraftExtras)

  modImplementation(libs.adventureFabric)
  include(libs.adventureFabric)

  bom(platform(libs.configurateBom))
  implementation(libs.configurateCore)
  include(libs.configurateCore)
  implementation(libs.configurateHocon)
  include(libs.configurateHocon)
  implementation(libs.configurateYaml)
  include(libs.configurateYaml)

  modImplementation(libs.modmenu)
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
  depends("fabric", "*")
  depends("fabricloader", ">=${libs.versions.fabricLoader.get()}")
  depends("minecraft", "~1.21")
  depends("cloud", "*")
  depends("adventure-platform-fabric", "*")
}

tasks {
  jar {
    from("LICENSE") {
      rename { "LICENSE_${project.name}" }
    }
  }
  remapJar {
    archiveFileName.set("${project.name}-mc${libs.versions.minecraft.get()}-${project.version}.jar")
  }
  withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:-processing")
  }
}

indra {
  javaVersions {
    target(21)
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
  minecraftVersions.add(libs.versions.minecraft)
}

fun decorateVersion() {
  val versionString = version as String
  val decorated = if (versionString.endsWith("-SNAPSHOT")) {
    "$versionString+${indraGit.commit()?.name?.substring(0, 7) ?: error("Could not determine git hash")}"
  } else {
    versionString
  }
  version = decorated
}
