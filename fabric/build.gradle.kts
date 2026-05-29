import net.fabricmc.loom.task.prod.ClientProductionRunTask

plugins {
    id("net.fabricmc.fabric-loom")
    id("me.modmuss50.mod-publish-plugin")
}

base {
    archivesName.set(property("archives_base_name").toString())
}

val loader = "fabric"
val mcVersion = rootProject.extra["mcVersion"].toString()
val buildVersionString: (String) -> String by rootProject.extra
val commonSourceSets = project(":common").extensions.getByType<SourceSetContainer>()

version = buildVersionString(loader)

loom {
    accessWidenerPath.set(project(":common").file("src/main/resources/betterclouds.accesswidener"))
    log4jConfigs.from(rootProject.file("log4j-dev.xml"))

    mods {
        register("betterclouds") {
            sourceSet(commonSourceSets.named("main").get())
            sourceSet(sourceSets.main.get())
        }
    }

    runs {
        named("client") {
            ideConfigGenerated(true)
            runDir("../run/fabric-client")
            vmArgs("-XX:+AllowEnhancedClassRedefinition", "-Xmx2560M")
        }

        named("server") {
            ideConfigGenerated(false)
            runDir("../run/fabric-server")
            vmArgs("-XX:+AllowEnhancedClassRedefinition", "-Xmx2560M")
        }
    }
}

dependencies {
    implementation(project(":common"))
    "minecraft"("com.mojang:minecraft:$mcVersion")

    annotationProcessor("io.github.llamalad7:mixinextras-common:${property("deps.mixin_extras")}")

    implementation("net.fabricmc:fabric-loader:${property("loader.fabric")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")

    implementation("com.terraformersmc:modmenu:${property("deps.modmenu")}")
    implementation("maven.modrinth:1eAoo2KR:${property("deps.yacl")}")

    compileOnly("maven.modrinth:KJe6y9Eu:${property("deps.fabric_seasons")}")

    val mixinSquared = "com.github.bawnorton.mixinsquared:mixinsquared-$loader:${property("deps.mixinsquared")}"
    annotationProcessor(mixinSquared)
    implementation(mixinSquared)
    include(mixinSquared)
    val mixinExtras = "io.github.llamalad7:mixinextras-fabric:${property("deps.mixin_extras")}"
    implementation(mixinExtras)
    include(mixinExtras)

    val mcloApi = "gs.mclo:api:${property("deps.mclo_api")}"
    implementation(mcloApi)
    include(mcloApi)
    compileOnly("maven.modrinth:Xs0XTOVv:${property("deps.distanthorizons_api")}")
    compileOnly("maven.modrinth:e0bNACJD:${property("deps.serene_seasons")}")
    compileOnly("maven.modrinth:2rL16t1O:${property("deps.enhanced_celestials")}-fabric")
    compileOnly("maven.modrinth:z2XEADmE:${property("deps.data_anchor")}-fabric")

    add("productionRuntimeMods", "maven.modrinth:1eAoo2KR:${property("deps.yacl")}")
    add("productionRuntimeMods", "net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}") {
        exclude(group = "net.fabricmc.fabric-api", module = "fabric-client-gametest-api-v1")
    }
}

tasks.register<ClientProductionRunTask>("runGameTest") {
    group = "loom"
    description = "Launches the Fabric client with the Fabric client game test flag."
    jvmArgs.add("-Dfabric.client.gametest")
}

tasks.named<ProcessResources>("processResources") {
    val props = mutableMapOf<String, Any>(
            "version" to project.version,
            "loader" to loader,
    )
    props["mc_version_range"] = findProperty("deps.minecraft").toString()
            .replace("-rc-", "-rc.")
            .replace("-pre-", "-pre.")
            .split(",")
            .joinToString(", ") { "\"~$it\"" }

    inputs.properties(props)

    filesMatching(listOf("fabric.mod.json")) {
        expand(props)
    }

    exclude("assets/**/*.ase")
    exclude("assets/**/*.xcf")
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks.named<Jar>("jar") {
    inputs.property("archivesName", base.archivesName)
    from(commonSourceSets.named("main").get().output)
    from(rootProject.file("LICENSE")) {
        rename { "${it}_${base.archivesName.get()}" }
    }
}

tasks.named<Jar>("sourcesJar") {
    from(commonSourceSets.named("main").get().allSource)
}
