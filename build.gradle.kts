plugins {
    id("fabric-loom") version "0.6-SNAPSHOT"
    `maven-publish`
    `java-library`
}

version = "0.1.0"
group = "xyz.jpenilla"

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.incendo.org/content/repositories/snapshots")
    jcenter()
}

val minecraftVersion = "1.16.5"

dependencies {
    minecraft("com.mojang", "minecraft", minecraftVersion)
    mappings(minecraft.officialMojangMappings())
    modImplementation("net.fabricmc", "fabric-loader", "0.11.1")
    //modImplementation("net.fabricmc.fabric-api", "fabric-api", "0.32.5+1.16")

    modImplementation(include("cloud.commandframework", "cloud-fabric", "1.5.0-SNAPSHOT")) // todo: jij fapi commands api?
    implementation(include("cloud.commandframework", "cloud-minecraft-extras", "1.5.0-SNAPSHOT"))
    modImplementation(include("net.kyori", "adventure-platform-fabric", "4.0.0-SNAPSHOT") {
        exclude("ca.stellardrift", "colonel")
    })
    modImplementation(include("ca.stellardrift", "confabricate", "2.0.3"))
    compileOnly("org.checkerframework", "checker-qual", "3.11.0")
}

tasks {
    processResources {
        filesMatching("fabric.mod.json") {
            filter { it.replace("\${VERSION}", project.version as String) }
        }
    }
    withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.toString()
    }
    jar {
        from("LICENSE")
    }
    remapJar {
        archiveFileName.set("${project.name}-mc$minecraftVersion-${project.version}.jar")
        archiveBaseName.set(project.name)
    }
}

java {
    sourceCompatibility = JavaVersion.toVersion(8)
    targetCompatibility = JavaVersion.toVersion(8)
    withSourcesJar()
}
