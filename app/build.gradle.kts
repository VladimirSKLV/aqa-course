import org.gradle.internal.os.OperatingSystem

plugins {
    application
    java
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.beryx.runtime") version "2.0.1"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    mainClass.set("ru.vlsklv.course.app.Launcher")
}

javafx {
    version = "17.0.16"
    modules = listOf("javafx.controls", "javafx.web")
}

val javafxPlatform: String = when {
    OperatingSystem.current().isWindows -> "win"
    OperatingSystem.current().isLinux -> "linux"
    OperatingSystem.current().isMacOsX -> "mac"
    else -> error("Unsupported OS for JavaFX runtime")
}

dependencies {
    implementation(project(":engine"))
    implementation(project(":content"))

    implementation("com.vladsch.flexmark:flexmark-all:0.64.8")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")

    runtimeOnly("org.openjfx:javafx-base:${javafx.version}:$javafxPlatform")
    runtimeOnly("org.openjfx:javafx-graphics:${javafx.version}:$javafxPlatform")
    runtimeOnly("org.openjfx:javafx-controls:${javafx.version}:$javafxPlatform")
    runtimeOnly("org.openjfx:javafx-web:${javafx.version}:$javafxPlatform")

    implementation("org.fxmisc.richtext:richtextfx:0.11.7")
}

runtime {
    distDir = file("$buildDir/install/${project.name}-shadow")

    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))

    modules.set(
        listOf(
            "java.desktop",
            "java.logging",
            "java.xml",
            "java.naming",
            "java.net.http",
            "java.compiler",
            "jdk.compiler"
        )
    )

    jpackage {
        mainJar = "app-all.jar"
        mainClass = application.mainClass.get()

        imageName = "AQA-Course"
        installerName = "AQA-Course"
        appVersion = project.version.toString()

        imageOptions = listOf("--vendor", "VlSKLV")

        installerType = "exe"
        installerOptions = listOf("--vendor", "VlSKLV", "--win-menu", "--win-shortcut")
    }
}

tasks.named("runtime") { dependsOn("installShadowDist") }
tasks.named("jpackageImage") { dependsOn("installShadowDist") }
tasks.named("jpackage") { dependsOn("installShadowDist") }

tasks.shadowJar {
    configurations = listOf(project.configurations.runtimeClasspath.get())
    isZip64 = true
    manifest { attributes["Main-Class"] = application.mainClass.get() }

    archiveBaseName.set(project.name)
    archiveClassifier.set("all")
    archiveVersion.set("")
    mergeServiceFiles()
}