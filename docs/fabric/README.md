# Fabric documentation

Version-specific test records and candidate notes are organized under [`versions/`](versions/).

- [1.20.1 server smoke check](versions/1.20.1-smoke-test.md)
- [1.20–1.20.4 shared-jar candidate](versions/1.20-1.20.4-candidate.md)

## Shared candidate builds

Build a candidate from the first Minecraft version in its range, passing the range key shown below:

```powershell
.\gradlew.bat :fabric:<anchor>:candidateJar -PcandidateFabricRange=<range>
```

| Range | Anchor | Range key |
| --- | --- | --- |
| 1.20–1.20.4 | 1.20.1 | `1.20-1.20.4` |
| 1.20.5–1.20.6 | 1.20.5 | `1.20.5-1.20.6` |
| 1.21–1.21.1 | 1.21 | `1.21-1.21.1` |
| 1.21.2–1.21.4 | 1.21.2 | `1.21.2-1.21.4` |
| 1.21.5 | 1.21.5 | `1.21.5` |
| 1.21.6–1.21.8 | 1.21.6 | `1.21.6-1.21.8` |
| 1.21.9–1.21.10 | 1.21.9 | `1.21.9-1.21.10` |
| 1.21.11 | 1.21.11 | `1.21.11` |
| 26.1–26.3 | 26.1 | `26.1-26.3` |

Artifacts are collected in `build/candidates/fabric/<range>/` (for example, `build/candidates/fabric/1.20-1.20.4/chunkyidle-fabric-0.1.1-mc1.20.1.jar`). All configured Fabric shared candidates have passed runtime testing and remain unpublished pending release packaging. See the [compatibility matrix](../CHUNKY_VERSION_PARITY.md#fabric) for status by range.
