// Used for every targeted version through 1.21.11 - there is no NeoForge build for 1.20.1
// (see settings.gradle.kts) and 26.1+ (the year.drop rebrand) uses build.26.gradle.kts
// instead - see fabric/build.gradle.kts's header comment for why the cutoff is there, not
// at 1.21.11 itself.

plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
    id("com.gradleup.shadow")
    id("me.modmuss50.mod-publish-plugin") version "2.2.0"
}

val minecraft = stonecutter.current.version
val common = requireNotNull(stonecutter.node?.sibling("")?.project) { "No common project for $project" }

val modId: String by project
val modVersion: String by project
val candidateRanges = mapOf(
    "1.20.2-1.20.4" to Triple("1.20.2", "[1.20.2,1.20.5)", listOf("1.20.2", "1.20.3", "1.20.4")),
    "1.20.5-1.20.6" to Triple("1.20.5", "[1.20.5,1.20.7)", listOf("1.20.5", "1.20.6")),
    "1.21-1.21.1" to Triple("1.21", "[1.21,1.21.2)", listOf("1.21", "1.21.1")),
    "1.21.2-1.21.4" to Triple("1.21.2", "[1.21.2,1.21.5)", listOf("1.21.2", "1.21.3", "1.21.4")),
    "1.21.5" to Triple("1.21.5", "[1.21.5,1.21.6)", listOf("1.21.5")),
    "1.21.6-1.21.8" to Triple("1.21.6", "[1.21.6,1.21.9)", listOf("1.21.6", "1.21.7", "1.21.8")),
    "1.21.9-1.21.10" to Triple("1.21.9", "[1.21.9,1.21.11)", listOf("1.21.9", "1.21.10")),
    "1.21.11" to Triple("1.21.11", "[1.21.11,)", listOf("1.21.11")),
)
val candidateRange = providers.gradleProperty("candidateNeoForgeRange").orNull
val candidate = candidateRange?.let(candidateRanges::get)
val neoForgeCandidate = candidate != null && minecraft == candidate.first
val legacyNeoForgeMetadata = minecraft == "1.20.2" || minecraft == "1.20.4"

version = "$modVersion+$minecraft"
base { archivesName.set("$modId-neoforge") }

architectury {
    platformSetupLoomIde()
    neoForge()
}

val commonBundle: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}
val shadowBundle: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}
configurations {
    compileClasspath.get().extendsFrom(commonBundle)
    runtimeClasspath.get().extendsFrom(commonBundle)
    named("developmentNeoForge").get().extendsFrom(commonBundle)
}

repositories {
    maven("https://maven.neoforged.net/releases/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://maven.shedaniel.me/") // Cloth Config
}

loom {
    silentMojangMappingsLicense()
}

// These live in versions/1.21.1/gradle.properties, only an ancestor of
// `common` - see the same comment in fabric/build.gradle.kts.
val neoforgeVersion = common.property("neoforgeVersion") as String
val architecturyApiVersion = (common.findProperty("neoforgeArchitecturyApiVersion")
    ?: common.property("architecturyApiVersion")) as String
val clothConfigVersion = common.property("clothConfigVersion") as String
val chunkyApiVersion = when (minecraft) {
    "1.20.2", "1.20.4" -> "1.3.146"
    "1.20.5", "1.20.6" -> "1.4.10"
    "1.21", "1.21.1" -> "1.4.23"
    "1.21.2", "1.21.3", "1.21.4" -> "1.4.27"
    "1.21.5" -> "1.4.36"
    "1.21.6", "1.21.7", "1.21.8" -> "1.4.40"
    "1.21.9", "1.21.10" -> "1.4.52"
    "1.21.11" -> "1.4.54"
    "26.1", "26.1.1", "26.1.2", "26.2", "26.3" -> "1.5.4"
    else -> error("No NeoForge Chunky API mapping configured for Minecraft $minecraft")
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft")
    mappings(loom.officialMojangMappings())

    "neoForge"("net.neoforged:neoforge:$neoforgeVersion")
    modImplementation("dev.architectury:architectury-neoforge:$architecturyApiVersion")
    modCompileOnly("org.popcraft:chunky-common:$chunkyApiVersion")
    shadowBundle("org.snakeyaml:snakeyaml-engine:2.10")

    // Optional/soft dependency: compile against it, and have it on the dev
    // run's classpath, but don't bundle it or require it at runtime - see
    // the "cloth-config" entry in neoforge.mods.toml.
    modCompileOnly("me.shedaniel.cloth:cloth-config-neoforge:$clothConfigVersion")
    modLocalRuntime("me.shedaniel.cloth:cloth-config-neoforge:$clothConfigVersion")

    commonBundle(project(common.path, "namedElements")) { isTransitive = false }
    shadowBundle(project(common.path, "transformProductionNeoForge")) { isTransitive = false }
}

java {
    withSourcesJar()
    val javaVersion = common.property("javaVersion") as String
    val java = JavaVersion.toVersion(javaVersion)
    toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion.toInt()))
    sourceCompatibility = java
    targetCompatibility = java
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.processResources {
    val modName: String by project
    val modDescription: String by project
    val modAuthors: String by project
    val modLicense: String by project

    val props = mapOf(
        "mod_id" to modId,
        "mod_version" to modVersion,
        "mod_name" to modName,
        "mod_description" to modDescription,
        "mod_authors" to modAuthors,
        "mod_license" to modLicense,
        "loader_version" to if (legacyNeoForgeMetadata) "[1,)" else "[2,)",
        "minecraft_version" to minecraft,
        "minecraft_version_range" to if (neoForgeCandidate) candidate!!.second else "[$minecraft,)",
    )
    inputs.properties(props)
    filesMatching("META-INF/neoforge.mods.toml") { expand(props) }
    if (legacyNeoForgeMetadata) {
        eachFile {
            if (path == "META-INF/neoforge.mods.toml") path = "META-INF/mods.toml"
        }
    }
}

tasks.shadowJar {
    configurations = listOf(shadowBundle)
    archiveClassifier.set("dev-shadow")
    addMultiReleaseAttribute = false
    exclude("fabric.mod.json", "architectury.common.json")
}

tasks.remapJar {
    input.set(tasks.shadowJar.flatMap { it.archiveFile })
    dependsOn(tasks.shadowJar)
    if (neoForgeCandidate) {
        archiveFileName.set("chunkyidle-neoforge-$modVersion-mc${candidate!!.first}.jar")
    }
}

tasks.jar {
    archiveClassifier.set("dev")
}

tasks.build {
    group = "versioned"
    description = "Must run through 'chiseledBuild' - see stonecutter.gradle.kts"
}

tasks.register<Copy>("buildAndCollect") {
    group = "versioned"
    from(tasks.remapJar.get().archiveFile, tasks.remapSourcesJar.get().archiveFile)
    into(rootProject.layout.buildDirectory.file("libs/$modVersion/neoforge"))
    dependsOn("build")
}

tasks.register<Copy>("candidateJar") {
    group = "build"
    description = "Builds a NeoForge shared-jar candidate from its range anchor version."
    onlyIf { neoForgeCandidate }
    from(tasks.remapJar.flatMap { it.archiveFile })
    into(rootProject.layout.buildDirectory.dir("candidates/neoforge/$candidateRange"))
    dependsOn(tasks.remapJar)
}

// Publishes this exact version+loader's jar to Modrinth/CurseForge - see
// stonecutter.gradle.kts's publishAllMods for running every variant's at once, and
// gradle.properties for the project IDs/dry-run switch this reads.
val modrinthProjectId: String by project
val curseforgeProjectId: String by project

publishMods {
    file.set(tasks.remapJar.flatMap { it.archiveFile })
    changelog.set(providers.environmentVariable("CHANGELOG").orElse("See the commit history."))
    type.set(STABLE)
    modLoaders.add("neoforge")
    dryRun.set(providers.gradleProperty("publish.dryRun").map(String::toBoolean).orElse(true))

    modrinth {
        projectId.set(modrinthProjectId)
        accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
        minecraftVersions.addAll(if (neoForgeCandidate) candidate!!.third else listOf(minecraft))
    }
    curseforge {
        projectId.set(curseforgeProjectId)
        accessToken.set(providers.environmentVariable("CURSEFORGE_TOKEN"))
        minecraftVersions.addAll(if (neoForgeCandidate) candidate!!.third else listOf(minecraft))
        client.set(true)
        server.set(true)
    }
}
