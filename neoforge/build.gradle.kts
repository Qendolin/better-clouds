plugins {
    `java-library`
    id("net.neoforged.moddev") version "2.0.141"
    id("me.modmuss50.mod-publish-plugin")
}

base {
    archivesName.set(property("archives_base_name").toString())
}

val loader = "neoforge"
val mcVersion = rootProject.extra["mcVersion"].toString()
val buildVersionString: (String) -> String by rootProject.extra
val commonSourceSets = project(":common").extensions.getByType<SourceSetContainer>()

version = buildVersionString(loader)

neoForge {
    version = property("loader.neoforge").toString()

    mods {
        create("betterclouds") {
            sourceSet(commonSourceSets.named("main").get())
            sourceSet(sourceSets.main.get())
        }
    }

    runs {
        create("client") {
            client()
            sourceSet = sourceSets.main.get()
            gameDirectory = layout.projectDirectory.dir("../run")
            systemProperty("forge.logging.console.level", "debug")
            loggingConfigFile = layout.projectDirectory.file("../log4j-dev.xml")
            jvmArgument("-Xmx2560M")
        }

        create("gameTest") {
            client()
            sourceSet = sourceSets.main.get()
            gameDirectory = layout.projectDirectory.dir("../run/neoforge-gametest")
            systemProperty("forge.logging.console.level", "debug")
            systemProperty("neoforge.enableGameTest", "true")
            loggingConfigFile = layout.projectDirectory.file("../log4j-dev.xml")
            jvmArgument("-Xmx2560M")
        }

        create("server") {
            server()
            sourceSet = sourceSets.main.get()
            gameDirectory = layout.projectDirectory.dir("../run")
            loggingConfigFile = layout.projectDirectory.file("../log4j-dev.xml")
            jvmArgument("-Xmx2560M")
        }
    }
}

val commonJava by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}
val commonResources by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

dependencies {
    add(commonJava.name, project(mapOf("path" to ":common", "configuration" to "commonJava")))
    add(commonResources.name, project(mapOf("path" to ":common", "configuration" to "commonResources")))
    implementation(project(":common")) // IDE syntax highlighting can discover :common.

    annotationProcessor("io.github.llamalad7:mixinextras-common:${property("deps.mixin_extras")}")
    compileOnly("org.ow2.asm:asm-tree:9.8")
    runtimeOnly("org.ow2.asm:asm:9.8")
    runtimeOnly("org.ow2.asm:asm-tree:9.8")
    runtimeOnly("org.ow2.asm:asm-commons:9.8")
    runtimeOnly("org.ow2.asm:asm-util:9.8")
    runtimeOnly("org.ow2.asm:asm-analysis:9.8")
    annotationProcessor("org.ow2.asm:asm-tree:9.7.1")
    annotationProcessor("com.google.code.gson:gson:2.13.2")
    annotationProcessor("com.google.guava:guava:33.5.0-jre")
    compileOnly("org.apache.maven:maven-artifact:3.9.9")

    // Compile common GUI code against Yarn-named YACL classes, but run NeoForge with the NeoForge artifact.
    compileOnly("maven.modrinth:1eAoo2KR:${property("deps.yacl")}")
    runtimeOnly("maven.modrinth:1eAoo2KR:${property("deps.yacl_neoforge")}")

    compileOnly(annotationProcessor("com.github.bawnorton.mixinsquared:mixinsquared-common:${property("deps.mixinsquared")}")!!)
    implementation(jarJar("com.github.bawnorton.mixinsquared:mixinsquared-neoforge:0.3.7-beta.1")!!)
    implementation("io.github.llamalad7:mixinextras-neoforge:${property("deps.mixin_extras")}")

    // These should be included with yacl, but it does not work correctly, so adding them here.
    runtimeOnly("org.quiltmc.parsers:json:0.3.0")
    runtimeOnly("org.quiltmc.parsers:gson:0.3.0")

    compileOnly("maven.modrinth:YL57xq9U:${property("deps.iris")}-neoforge")
    compileOnly("maven.modrinth:PtjYWJkn:${property("deps.sodium_extra")}+neoforge")
    compileOnly("maven.modrinth:Es5v4eyq:${property("deps.sodium_options_api_fabric")}")

    if (property("deps.terra_firma_craft").toString().isNotBlank()) {
        compileOnly("maven.modrinth:JaCEZUhg:${property("deps.terra_firma_craft")}")
    }

    implementation("gs.mclo:api:${property("deps.mclo_api")}")
    compileOnly("maven.modrinth:4lDrPSXX:${property("deps.longview")}")
    compileOnly("maven.modrinth:Xs0XTOVv:${property("deps.distanthorizons_api")}")
    compileOnly("maven.modrinth:e0bNACJD:${property("deps.serene_seasons")}")
    compileOnly("maven.modrinth:2rL16t1O:${property("deps.enhanced_celestials")}-neoforge")
    compileOnly("maven.modrinth:z2XEADmE:${property("deps.data_anchor")}-neoforge")
    if (property("deps.sodium").toString().isNotBlank()) {
        compileOnly("maven.modrinth:AANobbMI:${property("deps.sodium")}-neoforge")
    }
}

tasks.named<JavaCompile>("compileJava") {
    dependsOn(commonJava)
    source(
            commonJava.asFileTree.matching {
                exclude(
                        "com/qendolin/betterclouds/compat/EnhancedCelestials2CompatImpl.java",
                        "com/qendolin/betterclouds/mixin/runtime/SodiumGameOptionPagesMixin.java",
                )
            },
    )
}

tasks.named<ProcessResources>("processResources") {
    dependsOn(commonResources)
    from(commonResources)

    val props = mutableMapOf<String, Any>(
            "version" to project.version,
            "loader" to loader,
    )
    props["mc_version_range"] = findProperty("deps.minecraft").toString().split(",").joinToString(",") {
        "[$it,)"
    }
    props["neoforge_version_range"] = findProperty("deps.neoforge").toString()

    inputs.properties(props)

    filesMatching(listOf("fabric.mod.json", "META-INF/mods.toml", "META-INF/neoforge.mods.toml")) {
        expand(props)
    }

    exclude("fabric.mod.json")
    exclude("META-INF/mods.toml")
    exclude("assets/**/*.ase")
    exclude("assets/**/*.xcf")
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
    options.compilerArgs.add("-proc:none")
}

java {
    withSourcesJar()
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks.named<Jar>("jar") {
    inputs.property("archivesName", base.archivesName)
    from(rootProject.file("LICENSE")) {
        rename { "${it}_${base.archivesName.get()}" }
    }
}
