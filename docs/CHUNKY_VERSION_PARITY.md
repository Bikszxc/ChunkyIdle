# Minecraft Version Parity and Progress

Use this page to see which Chunky releases match each loader, which Minecraft targets ChunkyIdle builds for, and what still needs runtime verification. Chunky's compatibility range alone does not mean ChunkyIdle supports every point release in that range.

Last updated: 2026-11-04

## Current status

- **Build matrix:** Pass. `:chiseledBuild -x test` built every currently configured Fabric and NeoForge target on this date. Test tasks were skipped.
- **Shared candidates:** Every configured Fabric and NeoForge target is covered by shared candidate jars grouped by Chunky's compatibility ranges. All configured candidates for both loaders passed runtime testing and remain unpublished.
- **Runtime coverage:** Every shared candidate listed below passed runtime testing. Build success is recorded separately from in-game checks.

### Status key

| Status | Meaning |
| --- | --- |
| Pass | The stated build or runtime check completed successfully. |
| Partial | Some checks passed, but the full behavior set is not verified for the range. |
| Pending | No runtime verification has been recorded yet. |
| No target | Chunky supports the range, but ChunkyIdle does not currently build for it on this loader. |

## Fabric

| Minecraft | Chunky | ChunkyIdle target | Build | Runtime | Remaining work |
| --- | --- | --- | --- | --- | --- |
| 1.20–1.20.4 | 1.3.146 | Shared candidate | Pass | Pass | Passed runtime tests; unpublished pending release packaging. [Test record](fabric/versions/1.20-1.20.4-candidate.md) |
| 1.20.5–1.20.6 | 1.4.10 | Shared candidate | Pass | Pass | Passed runtime tests; unpublished pending release packaging. |
| 1.21 | 1.4.23 | Shared candidate | Pass | Pass | Passed runtime tests; unpublished pending release packaging. |
| 1.21.1 | 1.4.23 | Shared candidate | Pass | Pass | Passed runtime tests; unpublished pending release packaging. |
| 1.21.2–1.21.4 | 1.4.27 | Shared candidate | Pass | Pass | Passed runtime tests; unpublished pending release packaging. |
| 1.21.5 | 1.4.36 | Shared candidate | Pass | Pass | Passed runtime tests; unpublished pending release packaging. |
| 1.21.6–1.21.8 | 1.4.40 | Shared candidate | Pass | Pass | Passed runtime tests; unpublished pending release packaging. |
| 1.21.9–1.21.10 | 1.4.52 | Shared candidate | Pass | Pass | Passed runtime tests; unpublished pending release packaging. |
| 1.21.11 | 1.4.55 | Shared candidate | Pass | Pass | Passed runtime tests; unpublished pending release packaging. |
| 26.1, 26.1.1, 26.1.2, 26.2 | 1.5.3 | Shared candidate | Pass | Pass | Passed runtime tests; unpublished pending release packaging. |
| 26.3 | 1.5.3 | Shared candidate | Pass | Pass | Passed runtime tests; unpublished pending release packaging. |

## NeoForge

| Minecraft | Chunky | ChunkyIdle target | Build | Runtime | Remaining work |
| --- | --- | --- | --- | --- | --- |
| 1.20.2–1.20.4 | 1.3.146 | Shared candidate | Pass; 1.20.3 has no standalone target | Pass | Passed runtime tests; unpublished pending release packaging. [Test record](neoforge/versions/1.20.2-1.20.4-candidate.md) |
| 1.20.5–1.20.6 | 1.4.10 | Shared candidate | Pass | Pass | Passed runtime tests; unpublished pending release packaging. |
| 1.21–1.21.1 | 1.4.23 | Shared candidate | Pass | Pass | Passed runtime tests; unpublished pending release packaging. |
| 1.21.2–1.21.4 | 1.4.27 | Shared candidate | Pass | Pass | Passed runtime tests; unpublished pending release packaging. |
| 1.21.5 | 1.4.36 | Shared candidate | Pass | Pass | Passed runtime tests; unpublished pending release packaging. |
| 1.21.6–1.21.8 | 1.4.40 | Shared candidate | Pass | Pass | Passed runtime tests; unpublished pending release packaging. |
| 1.21.9–1.21.10 | 1.4.52 | Shared candidate | Pass | Pass | Passed runtime tests; unpublished pending release packaging. |
| 1.21.11 | 1.4.54 | Shared candidate | Pass | Pass | Passed runtime tests; unpublished pending release packaging. |
| 26.1–26.3 | 1.5.4 | Shared candidate | Pass | Pass | Passed runtime tests; unpublished pending release packaging. |

## Chunky compatibility mappings

These mappings describe Chunky releases. They do not expand ChunkyIdle's build targets above.

### Fabric

| Chunky | Minecraft |
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

| Chunky | Minecraft |
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

## Shared-jar policy

Keep Minecraft-specific jars by default. Share one jar across a range only after the same artifact passes the required runtime checks on every Minecraft version in its metadata. Candidate jars remain unpublished until the release checks are complete.

## Release checklist

- [x] All configured loader/version targets build successfully.
- [x] Fabric 1.20–1.20.4 candidate passes startup, join-pause, and idle-resume checks on the full range.
- [x] All configured Fabric shared candidates pass runtime testing.
- [x] All configured NeoForge shared candidates pass runtime testing.
- [ ] Complete release packaging and update download/install guidance before publishing either shared candidate.
