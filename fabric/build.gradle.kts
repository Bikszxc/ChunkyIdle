// Used for every targeted version through 1.21.11 - see fabric/build.26.gradle.kts for 26.1+
// (confirmed empirically: 1.21.11 - the last version before the year.drop rebrand - still
// builds fine through this regular, remap-based script; it's 26.1's obfuscation removal that
// needs the no-remap variant, not anything about 1.21.11 itself).

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
    "1.20-1.20.4" to Triple("1.20.1", ">=1.20 <=1.20.4", listOf("1.20", "1.20.1", "1.20.2", "1.20.3", "1.20.4")),
    "1.20.5-1.20.6" to Triple("1.20.5", ">=1.20.5 <=1.20.6", listOf("1.20.5", "1.20.6")),
    "1.21-1.21.1" to Triple("1.21", ">=1.21 <=1.21.1", listOf("1.21", "1.21.1")),
    "1.21.2-1.21.4" to Triple("1.21.2", ">=1.21.2 <=1.21.4", listOf("1.21.2", "1.21.3", "1.21.4")),
    "1.21.5" to Triple("1.21.5", "1.21.5", listOf("1.21.5")),
    "1.21.6-1.21.8" to Triple("1.21.6", ">=1.21.6 <=1.21.8", listOf("1.21.6", "1.21.7", "1.21.8")),
    "1.21.9-1.21.10" to Triple("1.21.9", ">=1.21.9 <=1.21.10", listOf("1.21.9", "1.21.10")),
    "1.21.11" to Triple("1.21.11", "1.21.11", listOf("1.21.11")),
)
val candidateRange = providers.gradleProperty("candidateFabricRange").orNull
val candidate = candidateRange?.let(candidateRanges::get)
val fabricCandidate = candidate != null && minecraft == candidate.first
val minecraftMetadata = if (fabricCandidate) candidate!!.second else minecraft

version = "$modVersion+$minecraft"
base { archivesName.set("$modId-fabric") }

architectury {
    platformSetupLoomIde()
    fabric()
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
    named("developmentFabric").get().extendsFrom(commonBundle)
}

repositories {
    maven("https://maven.shedaniel.me/") // Cloth Config
    maven("https://repo.codemc.io/repository/maven-public/") // Chunky's published API
}

loom {
    silentMojangMappingsLicense()
}

// GameTests (fabric/src/gametest/ - see README.md's "Testing" section) only for whichever
// version is currently `stonecutter active` - they verify real in-game *behavior*, not just
// that things compile, so unlike everything else in this template they can't be trivially
// shared across every targeted Minecraft version: running a full dedicated-server boot per
// version, x20 versions, would make every build far slower for comparatively little extra
// coverage, since `common` code (what these tests actually exercise) is identical across
// loaders/versions by construction. `stonecutter.current.isActive` is true for exactly one
// generated project at a time, so this follows `stonecutter active "..."` in
// stonecutter.gradle.kts automatically if that ever changes.
// javaVersion/fabricLoaderVersion/fabricApiVersion/architecturyApiVersion/
// clothConfigVersion live in versions/<mcVersion>/gradle.properties, which -
// unlike this repo's root gradle.properties - is only an ancestor directory
// of the `common` project, not of `fabric`/`neoforge`. Route through
// the sibling `common` project to read them.
val fabricLoaderVersion = common.property("fabricLoaderVersion") as String
val fabricApiVersion = common.property("fabricApiVersion") as String
val architecturyApiVersion = common.property("architecturyApiVersion") as String
val clothConfigVersion = common.property("clothConfigVersion") as String
val chunkyApiVersion = when (minecraft) {
    "1.20", "1.20.1", "1.20.2", "1.20.3", "1.20.4" -> "1.3.146"
    "1.20.5", "1.20.6" -> "1.4.10"
    "1.21", "1.21.1" -> "1.4.23"
    "1.21.2", "1.21.3", "1.21.4" -> "1.4.27"
    "1.21.5" -> "1.4.36"
    "1.21.6", "1.21.7", "1.21.8" -> "1.4.40"
    "1.21.9", "1.21.10" -> "1.4.52"
    "1.21.11" -> "1.4.55"
    "26.1", "26.1.1", "26.1.2", "26.2", "26.3" -> "1.5.3"
    else -> error("No Fabric Chunky API mapping configured for Minecraft $minecraft")
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft")
    mappings(loom.officialMojangMappings())

    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
    modImplementation("dev.architectury:architectury-fabric:$architecturyApiVersion")
    modCompileOnly("org.popcraft:chunky-common:$chunkyApiVersion")
    shadowBundle("org.snakeyaml:snakeyaml-engine:2.10")

    // Optional/soft dependency: compile against it, and have it on the dev
    // run's classpath, but don't bundle it or require it at runtime - see
    // the "cloth-config" entry in fabric.mod.json.
    modCompileOnly("me.shedaniel.cloth:cloth-config-fabric:$clothConfigVersion")
    modLocalRuntime("me.shedaniel.cloth:cloth-config-fabric:$clothConfigVersion")

    commonBundle(project(common.path, "namedElements")) { isTransitive = false }
    shadowBundle(project(common.path, "transformProductionFabric")) { isTransitive = false }
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
        "minecraft_version" to minecraftMetadata,
        "loader_version" to fabricLoaderVersion,
    )
    inputs.properties(props)
    filesMatching("fabric.mod.json") { expand(props) }
}

tasks.shadowJar {
    configurations = listOf(shadowBundle)
    archiveClassifier.set("dev-shadow")
    addMultiReleaseAttribute = false
}

tasks.remapJar {
    input.set(tasks.shadowJar.flatMap { it.archiveFile })
    dependsOn(tasks.shadowJar)
    if (fabricCandidate) {
        archiveFileName.set("chunkyidle-fabric-$modVersion-mc${candidate!!.first}.jar")
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
    into(rootProject.layout.buildDirectory.file("libs/$modVersion/fabric"))
    dependsOn("build")
}

tasks.register<Copy>("candidateJar") {
    group = "versioned"
    description = "Builds a Fabric shared-jar candidate from its range anchor version."
    onlyIf { fabricCandidate }
    from(tasks.remapJar.flatMap { it.archiveFile })
    into(rootProject.layout.buildDirectory.dir("candidates/fabric/$candidateRange"))
    dependsOn(tasks.build)
}

// Publishes this version+loader's jar to Modrinth/CurseForge. Candidate mode widens
// the listed Minecraft versions for its one anchor artifact; it remains a dry run
// unless explicitly configured otherwise.
val modrinthProjectId: String by project
val curseforgeProjectId: String by project

publishMods {
    file.set(tasks.remapJar.flatMap { it.archiveFile })
    changelog.set(providers.environmentVariable("CHANGELOG").orElse("See the commit history."))
    type.set(STABLE)
    modLoaders.add("fabric")
    dryRun.set(providers.gradleProperty("publish.dryRun").map(String::toBoolean).orElse(true))

    modrinth {
        projectId.set(modrinthProjectId)
        accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
        minecraftVersions.addAll(
            if (fabricCandidate) candidate!!.third
            else listOf(minecraft)
        )
        environment.set(SERVER_ONLY)
    }
    curseforge {
        projectId.set(curseforgeProjectId)
        accessToken.set(providers.environmentVariable("CURSEFORGE_TOKEN"))
        minecraftVersions.addAll(
            if (fabricCandidate) candidate!!.third
            else listOf(minecraft)
        )
        client.set(false)
        server.set(true)
    }
}
