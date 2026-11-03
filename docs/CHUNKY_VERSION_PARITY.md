# Minecraft Version Parity and Progress

Use this page to see which Chunky releases match each loader, which Minecraft targets ChunkyIdle builds for, and what still needs runtime verification. Chunky's compatibility range alone does not mean ChunkyIdle supports every point release in that range.

Last updated: 2026-11-04

## Current status

- **Build matrix:** Pass. `:chiseledBuild -x test` built every currently configured Fabric, NeoForge, and Forge target on this date. Test tasks were skipped.
- **Shared candidates:** Fabric 1.20–1.20.4 and NeoForge 1.20.2–1.20.4 each use one candidate jar across the listed range. Neither candidate is released.
- **Runtime coverage:** See the loader tables below. Build success is recorded separately from in-game checks.

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
| 1.20–1.20.4 | 1.3.146 | Shared candidate | Pass | Pass: startup, join pause, and idle resume on all five versions; commands and restart recovery checked on 1.20.4 | Package and release the candidate when release checks are complete. [Test record](fabric/versions/1.20-1.20.4-candidate.md) |
| 1.20.5–1.20.6 | 1.4.10 | Per-version targets | Pass | Pending | Run server checks on both versions. |
| 1.21 | 1.4.23 | Per-version target | Pass | Pending | Run server checks. |
| 1.21.1 | 1.4.23 | Per-version target | Pass | Pass | No current runtime work recorded. |
| 1.21.2–1.21.4 | 1.4.27 | Per-version targets | Pass | Pending | Run server checks on each version. |
| 1.21.5 | 1.4.36 | Per-version target | Pass | Pending | Run server checks. |
| 1.21.6–1.21.8 | 1.4.40 | Per-version targets | Pass | Pass | No current runtime work recorded. |
| 1.21.9–1.21.10 | 1.4.52 | Per-version targets | Pass | Pass | No current runtime work recorded. |
| 1.21.11 | 1.4.55 | Per-version target | Pass | Pass | No current runtime work recorded. |
| 26.1, 26.1.1, 26.1.2, 26.2 | 1.5.3 | Per-version targets | Pass | Pass | No current runtime work recorded. |
| 26.3 | 1.5.3 | Per-version target | Pass | Pending | Run server checks. |

## NeoForge

| Minecraft | Chunky | ChunkyIdle target | Build | Runtime | Remaining work |
| --- | --- | --- | --- | --- | --- |
| 1.20.2–1.20.4 | 1.3.146 | Shared candidate | Pass: candidate built from 1.20.2; 1.20.4 target also builds | Partial: the bot logged in, joined, and left on all three versions | Verify pause-on-join, idle resume, commands, and restart recovery against the NeoForge integration. 1.20.3 has no standalone build target. [Test record](neoforge/versions/1.20.2-1.20.4-candidate.md) |
| 1.20.5–1.20.6 | 1.4.10 | No target | — | Pending | Add NeoForge build targets only if coverage is expanded. |
| 1.21 | 1.4.23 | No target | — | Pending | Add a target only if coverage is expanded. |
| 1.21.1 | 1.4.23 | Per-version target | Pass | Pass | No current runtime work recorded. |
| 1.21.2–1.21.5 | 1.4.27–1.4.36 | No target | — | Pending | Add targets only if coverage is expanded. |
| 1.21.6–1.21.8 | 1.4.40 | Per-version targets | Pass | Pass | No current runtime work recorded. |
| 1.21.9–1.21.10 | 1.4.52 | Per-version targets | Pass | Pass | No current runtime work recorded. |
| 1.21.11 | 1.4.54 | Per-version target | Pass | Pass | No current runtime work recorded. |
| 26.1, 26.1.1, 26.1.2, 26.2 | 1.5.4 | Per-version targets | Pass | Pass | No current runtime work recorded. |
| 26.3 | 1.5.4 | Per-version target | Pass | Pending | Run server checks. |

## Forge

| Minecraft | Chunky | ChunkyIdle target | Build | Runtime | Remaining work |
| --- | --- | --- | --- | --- | --- |
| 1.20.1 | 1.3.146 | Per-version target | Pass | Pass | No current runtime work recorded. |
| Other Chunky-supported ranges | See compatibility table below | No target | — | Pending | Forge coverage is currently limited to 1.20.1. |

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

### Forge

| Chunky | Minecraft |
| --- | --- |
| 1.3.146 | 1.20–1.20.1 |
| 1.4.23 | 1.21–1.21.1 |
| 1.4.27 | 1.21.2–1.21.4 |
| 1.4.36 | 1.21.5 |
| 1.4.40 | 1.21.6–1.21.8 |
| 1.4.52 | 1.21.9–1.21.10 |
| 1.4.55 | 1.21.11 |
| 1.5.4 | 26.1.x–26.3 |

## Shared-jar policy

Keep Minecraft-specific jars by default. Share one jar across a range only after the same artifact passes the required runtime checks on every Minecraft version in its metadata. Candidate jars remain unpublished until the release checks are complete.

## Release checklist

- [x] All configured loader/version targets build successfully.
- [x] Fabric 1.20–1.20.4 candidate passes startup, join-pause, and idle-resume checks on the full range.
- [x] NeoForge 1.20.2–1.20.4 candidate accepts the test client's login/join/leave cycle on the full range.
- [ ] Verify NeoForge pause, idle resume, commands, and restart recovery on 1.20.2–1.20.4 after the NeoForge integration changes.
- [ ] Run runtime checks for rows marked Pending.
- [ ] Complete release packaging and update download/install guidance before publishing either shared candidate.
