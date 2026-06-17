import me.modmuss50.mpp.ModPublishExtension
import me.modmuss50.mpp.ReleaseType
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

plugins {
    id("net.fabricmc.fabric-loom") version "1.17.11" apply false
    id("dev.architectury.loom") version "1.13.469" apply false
    id("architectury-plugin") version "3.4.162" apply false
    id("me.modmuss50.mod-publish-plugin") version "1.1.0" apply false
}

apply(from = rootProject.file("common.gradle.kts").path)

data class PlatformDependencies(
        val requires: List<String>,
        val optional: List<String>,
        val incompatible: List<String>,
)

data class PublishTargetDefinition(
        val projectPath: String,
        val loader: String,
        val loaderName: String,
        val modrinth: PlatformDependencies,
        val curseforge: PlatformDependencies,
)

val publishTargetDefinitions = linkedMapOf(
        "fabric" to PublishTargetDefinition(
                projectPath = ":fabric",
                loader = "fabric",
                loaderName = "Fabric",
                modrinth = PlatformDependencies(
                        requires = listOf("yacl", "fabric-api"),
                        optional = listOf("modmenu", "sodium", "iris"),
                        incompatible = listOf("vulkanmod", "immersiveportals"),
                ),
                curseforge = PlatformDependencies(
                        requires = listOf("yacl", "fabric-api"),
                        optional = listOf("modmenu", "sodium", "irisshaders"),
                        incompatible = listOf("vulkanmod", "immersive-portals-mod"),
                ),
        ),
        /*
        "neoforge" to PublishTargetDefinition(
                projectPath = ":neoforge",
                loader = "neoforge",
                loaderName = "NeoForge",
                modrinth = PlatformDependencies(
                        requires = listOf("yacl"),
                        optional = listOf("sodium", "iris"),
                        incompatible = emptyList(),
                ),
                curseforge = PlatformDependencies(
                        requires = listOf("yacl"),
                        optional = listOf("sodium", "irisshaders"),
                        incompatible = emptyList(),
                ),
        ),
        */
)

val parseBooleanGradleProperty: (String, Boolean) -> Provider<Boolean> = { name, defaultValue ->
    providers.gradleProperty(name).map { value ->
        val normalized = value.trim().lowercase(Locale.ROOT)
        if (normalized !in listOf("true", "false")) {
            throw GradleException("Property $name must be either true or false, got '$value'.")
        }
        normalized == "true"
    }.orElse(defaultValue)
}

val isPublishTargetEnabled: (String) -> Boolean = { target ->
    parseBooleanGradleProperty("publish.$target", true).get()
}

val selectedPublishTargets: () -> List<String> = {
    publishTargetDefinitions.keys.filter(isPublishTargetEnabled)
}

val runCommand: (List<String>) -> Unit = { command ->
    val process = ProcessBuilder(command)
            .directory(rootProject.rootDir)
            .inheritIO()
            .start()

    val exitCode = process.waitFor()
    if (exitCode != 0) {
        throw GradleException("Command failed with exit code $exitCode: ${command.joinToString(" ")}")
    }
}

val gitOutput: (List<String>) -> String = { command ->
    val process = ProcessBuilder(command)
            .directory(rootProject.rootDir)
            .redirectErrorStream(true)
            .start()
    val output = process.inputStream.bufferedReader().use { it.readText() }.trim()
    val exitCode = process.waitFor()
    if (exitCode != 0) {
        throw GradleException("Command failed with exit code $exitCode: ${command.joinToString(" ")}")
    }
    output
}

val requirePublishConfirmation: (List<String>) -> Unit = { warningLines ->
    warningLines.forEach(::println)
    println("Press Enter to continue, or Ctrl+C to cancel.")

    val console = System.console()
    if (console != null) {
        console.readLine()
    } else {
        BufferedReader(InputStreamReader(System.`in`)).readLine()
                ?: throw GradleException("Interactive confirmation required. Re-run this publish command in a console and press Enter to continue.")
    }
}

val changelogFile = layout.projectDirectory.file("changelog.md").asFile
val maxChangelogAgeMs = 60L * 60L * 1000L

val validatePublishChangelog: () -> Unit = {
    if (!changelogFile.exists()) {
        throw GradleException("Missing changelog.md. Update it within the last hour before publishing.")
    }

    val changelog = changelogFile.readText(Charsets.UTF_8)
    if (changelog.isBlank()) {
        throw GradleException("changelog.md was blank")
    }

    val changelogAgeMs = System.currentTimeMillis() - changelogFile.lastModified()
    if (changelogAgeMs > maxChangelogAgeMs) {
        val modifiedAt = Date(changelogFile.lastModified())
        throw GradleException("changelog.md must be updated within the last hour before publishing. Last modified: $modifiedAt.")
    }
}

val loadPublishSecrets: () -> Properties = {
    val secrets = Properties()
    val secretsFile = rootProject.file("secrets.properties")
    if (!secretsFile.exists()) {
        throw GradleException("Missing secrets.properties for publishing")
    }

    secretsFile.inputStream().use(secrets::load)
    secrets
}

val releaseTypeEnum: () -> ReleaseType = {
    val releaseType = property("mod.release").toString()
    when (releaseType.substringBefore(".").lowercase(Locale.ROOT)) {
        "release" -> ReleaseType.STABLE
        "beta" -> ReleaseType.BETA
        "alpha" -> ReleaseType.ALPHA
        else -> throw GradleException("Unknown release type: $releaseType")
    }
}

val releaseType = property("mod.release").toString()
val modVersion = property("mod.version").toString()
val mcVersion = findProperty("deps.minecraft").toString()
val releaseVersionString = run {
    val isPrerelease = releaseTypeEnum() != ReleaseType.STABLE
    if (isPrerelease) "$modVersion-$releaseType" else modVersion
}
val buildVersionString: (String) -> String = { loader ->
    "$releaseVersionString+$mcVersion-$loader"
}

extra["releaseType"] = releaseType
extra["modVersion"] = modVersion
extra["mcVersion"] = mcVersion
extra["releaseVersionString"] = releaseVersionString
extra["buildVersionString"] = buildVersionString

val configurePublishProject: (String, PublishTargetDefinition) -> Unit = { target, targetConfig ->
    val targetProject = project(targetConfig.projectPath)
    targetProject.pluginManager.withPlugin("me.modmuss50.mod-publish-plugin") {
        targetProject.extensions.configure<ModPublishExtension>("publishMods") {
            val targetReleaseType = targetProject.property("mod.release").toString()
            val releaseName = targetReleaseType.replaceFirstChar { it.titlecase(Locale.ROOT) }
            val targetModVersion = targetProject.property("mod.version").toString()
            val mcVersions = targetProject.findProperty("deps.minecraft").toString().split(",").map {
                it.trim().replace(".0", "")
            }

            file.set(targetProject.tasks.named<Jar>("jar").flatMap { it.archiveFile })
            changelog.set(rootProject.providers.fileContents(rootProject.layout.projectDirectory.file("changelog.md")).asText)
            displayName.set("$releaseName $targetModVersion for $mcVersion ${targetConfig.loaderName}")
            version.set(buildVersionString(targetConfig.loader))
            type.set(releaseTypeEnum())
            modLoaders.add(targetConfig.loader)

            modrinth {
                accessToken.set(rootProject.providers.provider { loadPublishSecrets().getProperty("MODRINTH") })
                projectId.set("5srFLIaK")
                version.set(releaseVersionString)
                minecraftVersions.addAll(mcVersions)
                targetConfig.modrinth.requires.forEach { requires(it) }
                targetConfig.modrinth.optional.forEach { optional(it) }
                targetConfig.modrinth.incompatible.forEach { incompatible(it) }
                projectDescription.set(rootProject.providers.fileContents(rootProject.layout.projectDirectory.file("README.md")).asText)
            }

            curseforge {
                accessToken.set(rootProject.providers.provider { loadPublishSecrets().getProperty("CURSEFORGE") })
                projectId.set("1285973")
                projectSlug.set("better-clouds")
                minecraftVersions.addAll(mcVersions)
                changelogType.set("markdown")
                javaVersions.add(JavaVersion.VERSION_25)
                clientRequired.set(true)
                serverRequired.set(false)
                targetConfig.curseforge.requires.forEach { requires(it) }
                targetConfig.curseforge.optional.forEach { optional(it) }
                targetConfig.curseforge.incompatible.forEach { incompatible(it) }
            }
        }

        val publishGithubAll = if (rootProject.tasks.names.contains("publishGithubAll")) {
            rootProject.tasks.named("publishGithubAll")
        } else {
            rootProject.tasks.register("publishGithubAll") {
                description = "Publish the mod for this gradle project to GitHub"
                group = "publishing"

                val releaseTargets = selectedPublishTargets().map { enabledTarget ->
                    project(publishTargetDefinitions.getValue(enabledTarget).projectPath)
                }
                dependsOn(releaseTargets.map { releaseProject -> releaseProject.tasks.named("jar") })

                doLast {
                    val githubReleaseType = rootProject.property("mod.release").toString()
                    val githubModVersion = rootProject.property("mod.version").toString()
                    var tag = "v$githubModVersion"
                    val isPrerelease = releaseTypeEnum() != ReleaseType.STABLE
                    if (isPrerelease) {
                        tag += "-$githubReleaseType"
                    }

                    val title = tag
                    val head = gitOutput(listOf("git", "rev-parse", "HEAD"))
                    val jars = releaseTargets.map { releaseProject ->
                        releaseProject.tasks.named<Jar>("jar").get().archiveFile.get().asFile.absolutePath
                    }

                    val releaseCreateCommand = mutableListOf(
                            "gh", "release", "create", tag,
                            "--target", head,
                            "--notes-file", changelogFile.absolutePath,
                            "--title", title,
                    )
                    if (isPrerelease) releaseCreateCommand.add("--prerelease")

                    runCommand(releaseCreateCommand)
                    runCommand(listOf("gh", "release", "upload", tag) + jars + "--clobber")
                }
            }
        }

        val publishGithub = targetProject.tasks.register("publishGithub") {
            description = "Publish all loader variants of the mod in the project into a single release"
            group = "publishing"
            dependsOn(publishGithubAll)
        }

        targetProject.tasks.named("publishMods").configure {
            dependsOn(publishGithub)
        }

        listOf("publishMods", "publishModrinth", "publishCurseforge", "publishGithub").forEach { taskName ->
            targetProject.tasks.named(taskName).configure {
                onlyIf { isPublishTargetEnabled(target) }
                doFirst {
                    validatePublishChangelog()
                }
            }
        }
    }
}

allprojects {
    group = rootProject.property("maven_group").toString()

    repositories {
        mavenCentral()
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        maven {
            name = "TerraformersMC"
            url = uri("https://maven.terraformersmc.com/releases/")
        }
        maven {
            name = "Modrinth"
            url = uri("https://api.modrinth.com/maven")
            content {
                includeGroup("maven.modrinth")
            }
        }
        maven {
            name = "Xander"
            url = uri("https://maven.isxander.dev/releases")
        }
        maven {
            name = "NeoForged"
            url = uri("https://maven.neoforged.net/releases")
            content {
                includeGroup("net.neoforged")
                includeGroup("net.neoforged.fancymodloader")
                includeGroup("cpw.mods")
            }
        }
        maven { url = uri("https://maven.bawnorton.com/releases") }
    }
}

publishTargetDefinitions.forEach(configurePublishProject)

tasks.register("validatePublishChangelog") {
    group = "publishing"
    description = "Validates that changelog.md exists, is non-blank, and was updated within the last hour."
    doLast {
        validatePublishChangelog()
    }
}

val requestedPublishTaskNames = gradle.startParameter.taskNames.filter { requestedTaskName ->
    requestedTaskName.substringAfterLast(":") in listOf("publishMods", "publishModrinth", "publishCurseforge")
}

val requestedPublishModsTaskNames = requestedPublishTaskNames.filter { requestedTaskName ->
    requestedTaskName.substringAfterLast(":") == "publishMods"
}

if (requestedPublishTaskNames.isNotEmpty()) {
    if (selectedPublishTargets().isEmpty()) {
        throw GradleException("No publish targets are enabled. Set publish.fabric=true and/or publish.neoforge=true.")
    }

    if (requestedPublishModsTaskNames.isNotEmpty()) {
        requirePublishConfirmation(
                listOf(
                        "Requested publish task(s): ${requestedPublishModsTaskNames.joinToString(", ")}",
                        if (isPublishTargetEnabled("fabric")) buildVersionString("fabric") else "Not publishing Fabric",
                        if (isPublishTargetEnabled("neoforge")) buildVersionString("neoforge") else "Not publishing Neoforge",
                ),
        )
    } else if (selectedPublishTargets().size != publishTargetDefinitions.size) {
        requirePublishConfirmation(
                listOf(
                        "WARNING: publish.fabric=${isPublishTargetEnabled("fabric")}, publish.neoforge=${isPublishTargetEnabled("neoforge")}",
                        if (isPublishTargetEnabled("fabric")) buildVersionString("fabric") else "Not publishing Fabric",
                        if (isPublishTargetEnabled("neoforge")) buildVersionString("neoforge") else "Not publishing Neoforge",
                ),
        )
    }
}
