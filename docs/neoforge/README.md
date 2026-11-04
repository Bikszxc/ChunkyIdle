# NeoForge documentation

Version-specific test records and candidate notes are organized under [`versions/`](versions/).

- [1.20.2–1.20.4 shared-jar candidate](versions/1.20.2-1.20.4-candidate.md)
- [Loader-specific Chunky compatibility table](../CHUNKY_VERSION_PARITY.md#neoforge)

## Shared candidate builds

Build each candidate from the first NeoForge target in its range. The build scripts use `candidateNeoForgeRange` to select candidate metadata and artifact naming:

```powershell
.\gradlew.bat :neoforge:<anchor>:candidateJar -PcandidateNeoForgeRange=<range>
```

| Range | Anchor | Range key |
| --- | --- | --- |
| 1.20.2–1.20.4 | 1.20.2 | `1.20.2-1.20.4` |
| 1.20.5–1.20.6 | 1.20.5 | `1.20.5-1.20.6` |
| 1.21–1.21.1 | 1.21 | `1.21-1.21.1` |
| 1.21.2–1.21.4 | 1.21.2 | `1.21.2-1.21.4` |
| 1.21.5 | 1.21.5 | `1.21.5` |
| 1.21.6–1.21.8 | 1.21.6 | `1.21.6-1.21.8` |
| 1.21.9–1.21.10 | 1.21.9 | `1.21.9-1.21.10` |
| 1.21.11 | 1.21.11 | `1.21.11` |
| 26.1–26.3 | 26.1 | `26.1-26.3` |

Artifacts are collected in `build/candidates/neoforge/`. All configured NeoForge shared candidates have passed runtime testing and remain unpublished pending release packaging. See the [compatibility matrix](../CHUNKY_VERSION_PARITY.md#neoforge) for status by range.
