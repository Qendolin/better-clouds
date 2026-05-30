plugins {
    id("net.fabricmc.fabric-loom")
    `java-library`
}

base {
    archivesName.set("${property("archives_base_name")}-common")
}

val mcVersion = rootProject.extra["mcVersion"].toString()

version = "${property("mod.version")}+${property("deps.minecraft")}-common"

loom {
    accessWidenerPath.set(file("src/main/resources/betterclouds.accesswidener"))
}

dependencies {
    "minecraft"("com.mojang:minecraft:$mcVersion")

    compileOnly("org.jetbrains:annotations:26.0.2")
    compileOnly("net.fabricmc:sponge-mixin:0.17.0+mixin.0.8.7")
    compileOnly("com.github.bawnorton.mixinsquared:mixinsquared-common:${property("deps.mixinsquared")}")
    compileOnly("io.github.llamalad7:mixinextras-common:${property("deps.mixin_extras")}")

    annotationProcessor("io.github.llamalad7:mixinextras-common:${property("deps.mixin_extras")}")

    compileOnly("maven.modrinth:1eAoo2KR:${property("deps.yacl")}")
    compileOnly("maven.modrinth:YL57xq9U:${property("deps.iris")}-fabric")
    compileOnly("maven.modrinth:4lDrPSXX:${property("deps.longview")}")
    compileOnly("gs.mclo:api:${property("deps.mclo_api")}")
    compileOnly("maven.modrinth:Xs0XTOVv:${property("deps.distanthorizons_api")}")

    testImplementation(platform("org.junit:junit-bom:5.12.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("maven.modrinth:1eAoo2KR:${property("deps.yacl")}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}

tasks.withType<Test>().configureEach {
    failOnNoDiscoveredTests = false
    useJUnitPlatform()
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

val commonJava by configurations.creating {
    isCanBeResolved = false
    isCanBeConsumed = true
}
val commonResources by configurations.creating {
    isCanBeResolved = false
    isCanBeConsumed = true
}

artifacts {
    add(commonJava.name, sourceSets.main.get().java.sourceDirectories.singleFile)
    add(commonResources.name, sourceSets.main.get().resources.sourceDirectories.singleFile)
}
