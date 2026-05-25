import java.io.File

val modifyModVersion: (File, Int) -> Unit = { file, delta ->
    val versionPattern = Regex("""mod\.version\s*=\s*(\d+)\.(\d+)\.(\d+)(-.+)?""")
    val content = file.readText()

    // Preserve the current properties file line ending when rewriting its version.
    val lineEnding = if (content.contains("\r\n")) "\r\n" else "\n"
    val lines = content.split(Regex("""\r?\n""")).dropLastWhile { it.isEmpty() }
    val matches = lines.filter { versionPattern.containsMatchIn(it) }
    if (matches.size != 1) {
        throw GradleException("Expected exactly one mod.version in $file, found ${matches.size}")
    }

    val updatedLines = lines.map { line ->
        val match = versionPattern.find(line)
        if (match == null) {
            line
        } else {
            val (major, minor, patch, suffixMatch) = match.destructured
            val newPatch = patch.toInt() + delta
            if (newPatch < 0) {
                throw GradleException("Patch version underflow in $file: cannot decrement below zero")
            }

            val newVersion = "$major.$minor.$newPatch$suffixMatch"
            println("Updated $file from ${match.value} to mod.version=$newVersion")
            line.replaceFirst(versionPattern, "mod.version=$newVersion")
        }
    }

    file.writeText(updatedLines.joinToString(lineEnding))
}

tasks.register("incrementModVersions") {
    group = "publishing"
    description = "Increments the patch version of mod.version in gradle.properties."
    doLast {
        val file = rootProject.file("gradle.properties")
        try {
            modifyModVersion(file, 1)
        } catch (e: Exception) {
            println("Error processing $file: ${e.message}")
            throw e
        }
    }
}

tasks.register("decrementModVersions") {
    group = "publishing"
    description = "Decrements the patch version of mod.version in gradle.properties."
    doLast {
        val file = rootProject.file("gradle.properties")
        try {
            modifyModVersion(file, -1)
        } catch (e: Exception) {
            println("Error processing $file: ${e.message}")
            throw e
        }
    }
}
