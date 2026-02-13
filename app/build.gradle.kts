plugins {
    application
    java
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    mainClass.set("ru.sobol.course.app.CourseApp")
}

javafx {
    version = "21.0.4"
    modules = listOf("javafx.controls", "javafx.web")
}

dependencies {
    implementation(project(":engine"))
    implementation(project(":content"))

    implementation("com.vladsch.flexmark:flexmark-all:0.64.8")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
}

tasks.shadowJar {
    archiveBaseName.set("aqa-course-app")
    archiveClassifier.set("all")
    mergeServiceFiles()
}
