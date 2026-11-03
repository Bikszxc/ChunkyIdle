# Chunky Minecraft Version Parity

This document separates Chunky's Minecraft compatibility by loader. A matching Chunky release does not by itself prove that a ChunkyIdle binary works on every Minecraft point release in the range.

## Chunky release compatibility

### Fabric

| Chunky version | Minecraft versions |
| --- | --- |
| 1.3.146 | 1.20–1.20.4 |
| 1.4.10 | 1.20.5–1.20.6 |
| 1.4.23 | 1.21–1.21.1 |
| 1.4.27 | 1.21.2–1.21.4 |
| 1.4.36 | 1.21.5 |
| 1.4.40 | 1.21.6–1.21.8 |
| 1.4.52 | 1.21.9–1.21.10 |
| 1.4.55 | 1.21.11 |
| 1.5.3 | 26.1.x–26.3 |

### NeoForge

| Chunky version | Minecraft versions |
| --- | --- |
| 1.3.146 | 1.20.2–1.20.4 |
| 1.4.10 | 1.20.5–1.20.6 |
| 1.4.23 | 1.21–1.21.1 |
| 1.4.27 | 1.21.2–1.21.4 |
| 1.4.36 | 1.21.5 |
| 1.4.40 | 1.21.6–1.21.8 |
| 1.4.52 | 1.21.9–1.21.10 |
| 1.4.54 | 1.21.11 |
| 1.5.4 | 26.1.x–26.3 |

### Forge

| Chunky version | Minecraft versions |
| --- | --- |
| 1.3.146 | 1.20–1.20.1 |
| 1.4.23 | 1.21–1.21.1 |
| 1.4.27 | 1.21.2–1.21.4 |
| 1.4.36 | 1.21.5 |
| 1.4.40 | 1.21.6–1.21.8 |
| 1.4.52 | 1.21.9–1.21.10 |
| 1.4.55 | 1.21.11 |
| 1.5.4 | 26.1.x–26.3 |

## ChunkyIdle build targets

Fabric is built for each point release in the Fabric ranges above. NeoForge and Forge builds remain limited to the loader targets listed in `settings.gradle.kts`; compatibility of Chunky itself does not imply a ChunkyIdle build exists for that loader and version.

At the time of this update, the current NeoForge matrix includes 1.20.2, 1.20.4, 1.21.1, 1.21.6–1.21.11, and 26.1.x–26.3. Forge remains on 1.20.1.

## Shared jar policy

Minecraft-version-specific jars remain the default. A single jar may cover multiple versions only after the same artifact starts and passes the automation smoke checks on every Minecraft version declared by its metadata. Compilation alone is not sufficient evidence for grouping.

## Verification status

Target inclusion in the build matrix means the project is configured to build that version; it does not mean the build or runtime behavior has passed. Only the README's tested-version matrix records completed verification.
