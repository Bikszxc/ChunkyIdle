plugins {
    id("dev.kikugie.stonecutter")
    id("dev.architectury.loom") version "1.17-SNAPSHOT" apply false
    // As of writing, plain `dev.architectury.loom` can't find official
    // mappings for the new year.drop versions (26.1+) - this is a known,
    // still-open gap (architectury/architectury-loom#328). The `-no-remap`
    // variant is the documented workaround, so 26.2 uses that one instead -
    // see the `useNoRemap` branches in each build.gradle.kts, and README.md.
    id("dev.architectury.loom-no-remap") version "1.17-SNAPSHOT" apply false
    id("architectury-plugin") version "3.5-SNAPSHOT" apply false
    id("com.gradleup.shadow") version "9.4.3" apply false
}

// The version opened by default in the IDE / by a bare `./gradlew build`.
// Switch it with the Stonecutter IntelliJ plugin, or target a specific
// version's tasks directly, e.g. `./gradlew :fabric:1.20.1:build`.
stonecutter active "1.21.1" /* [SC] DO NOT EDIT */

// Builds every Minecraft version x loader combo in one go - the jars land in each
// <loader>/versions/<mc>/build/libs/ (the un-classified one, e.g. alotofinterior-fabric-
// 0.1.1+1.21.1.jar, is the real distributable - the others are dev-only intermediates).
tasks.register("chiseledBuild") {
    group = "project"
    description = "Builds every Minecraft version x loader variant."
    dependsOn(stonecutter.tasks.named("build").map { it.values })
}

// Publishes one runtime-tested shared candidate per loader/compatibility range. The anchor
// projects infer their candidate range from this task name, so callers don't need to pass
// per-range properties. Defaults to a dry run until -Ppublish.dryRun=false is passed.
tasks.register("publishAllMods") {
    group = "project"
    description = "Publishes tested shared Fabric and NeoForge candidates to Modrinth/CurseForge."
    dependsOn(
        ":fabric:1.20.1:publishMods",
        ":fabric:1.20.5:publishMods",
        ":fabric:1.21:publishMods",
        ":fabric:1.21.2:publishMods",
        ":fabric:1.21.5:publishMods",
        ":fabric:1.21.6:publishMods",
        ":fabric:1.21.9:publishMods",
        ":fabric:1.21.11:publishMods",
        ":fabric:26.1:publishMods",
        ":neoforge:1.20.2:publishMods",
        ":neoforge:1.20.5:publishMods",
        ":neoforge:1.21:publishMods",
        ":neoforge:1.21.2:publishMods",
        ":neoforge:1.21.5:publishMods",
        ":neoforge:1.21.6:publishMods",
        ":neoforge:1.21.9:publishMods",
        ":neoforge:1.21.11:publishMods",
        ":neoforge:26.1:publishMods",
    )
}
