import com.palantir.gradle.gitversion.VersionDetails
import groovy.lang.Closure
import java.lang.String.format
import java.lang.String.valueOf

plugins {
    id("java")
    id("net.kyori.blossom") version "1.3.1"
    id("com.palantir.git-version") version "3.1.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

val versionDetails: Closure<VersionDetails> by extra
fun projectVersion(): String = if (versionDetails().branchName == "ver/latest")
    valueOf(project.version) else format("%s (git/%s)", project.version, versionDetails().gitHash)

project.group = project.parent?.group!!
project.version = project.parent?.version!!

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

blossom {
    replaceToken("{version}", projectVersion())
    replaceToken("{gitBranch}", versionDetails().branchName)
    replaceToken("{gitCommitHash}", versionDetails().gitHashFull)
}

// Pastikan SNAPSHOT dari JitPack selalu fresh, tidak di-cache
configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.viaversion.com")
    maven("https://repo.tcoded.com/releases")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.panda-lang.org/releases/")
    maven("https://repo.opencollab.dev/maven-releases/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://storehouse.okaeri.eu/repository/maven-public/")
}

dependencies {

    implementation(project(":api"))

    /* Minecraft */
    compileOnly("com.destroystokyo.paper:paper-api:${project.parent?.property("minecraft.version")}")

    /* Lite Commands */
    implementation("dev.rollczi:litecommands-core:${project.parent?.property("litecommands.version")}")
    implementation("dev.rollczi:litecommands-bukkit:${project.parent?.property("litecommands.version")}")

    /* Okaeri Configs */
    implementation("eu.okaeri:okaeri-configs-yaml-bukkit:${project.parent?.property("okaeri.configs.version")}")
    implementation("eu.okaeri:okaeri-configs-serdes-bukkit:${project.parent?.property("okaeri.configs.version")}")

    /* Commons (via JitPack dari fork MarvinSur/java-commons) */
    implementation("com.github.MarvinSur:java-commons:${project.parent?.property("mrstudios.commons.version")}")

    /* Kyori Adventure */
    implementation("net.kyori:adventure-api:${project.parent?.property("kyori.adventure.version")}")
    implementation("net.kyori:adventure-text-minimessage:${project.parent?.property("kyori.adventure.version")}")

    /* Kyori Adventure Platform */
    implementation("net.kyori:adventure-platform-bukkit:${project.parent?.property("kyori.adventure.platform.version")}")
    implementation("net.kyori:adventure-text-serializer-bungeecord:${project.parent?.property("kyori.adventure.platform.version")}")

    /* Apache Commons IO */
    implementation("commons-io:commons-io:${project.parent?.property("apache.commons.io.version")}")

    /* Zip4J */
    implementation("net.lingala.zip4j:zip4j:${project.parent?.property("zip4j.version")}")

    /* Protocol Sidebar (via JitPack dari CatCoderr/ProtocolSidebar) */
    implementation("com.github.CatCoderr:ProtocolSidebar:${project.parent?.property("protocol.sidebar.version")}") {
        isChanging = true
    }

    /* WorldEdit */
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:${project.parent?.property("worldedit.version")}")

    /* JetBrains Annotations */
    compileOnly("org.jetbrains:annotations:${project.parent?.property("jetbrains.annotations.version")}")
    annotationProcessor("org.jetbrains:annotations:${project.parent?.property("jetbrains.annotations.version")}")

}

tasks {

    processResources {
        inputs.property("version", projectVersion())
        expand(inputs.properties)
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    jar {
        dependsOn(shadowJar)
    }

    shadowJar {

        archiveFileName.set("${project.parent?.name}-${project.name}-${project.version}.jar")

        exclude("META-INF/**")
        dependencies {
            isEnableRelocation = true
            relocationPrefix = "${project.parent?.group}.libraries"
        }

        relocate("pl.mrstudios.deathrun.api", "pl.mrstudios.deathrun.api")

    }

    build {
        finalizedBy("finalize")
    }

    register("finalize") {
        doLast {
            file("build/libs/${project.name}-${project.version}.jar").delete()
        }
    }

}
