# Fabric 1.20–1.20.4 shared-jar candidate

The candidate is built from the Fabric 1.20.1 project and declares Minecraft support from 1.20 through 1.20.4. The exact same jar passed server startup, join-pause, and idle-resume checks on all five versions. It remains a test artifact, not a released jar.

## Build

From the repository root, run:

```powershell
.\gradlew.bat :fabric:1.20.1:candidateJar -PcandidateFabric120To1204=true
```

The artifact is written to:

```text
build/candidates/fabric/chunkyidle-fabric-0.1.0+1.20.1-candidate-1.20-1.20.4.jar
```

Install this exact jar on each Fabric dedicated server version: 1.20, 1.20.1, 1.20.2, 1.20.3, and 1.20.4. Pair each with Chunky 1.3.146 and a compatible Fabric API. Do not substitute that version's regular per-Minecraft ChunkyIdle jar during the checks.

## Checks for each version

- Start the server and confirm ChunkyIdle loads without dependency or metadata errors.
- Start a Chunky task, join as a player, and confirm generation pauses with progress saved.
- Leave the server and confirm the task resumes after the configured empty-server delay.
- Rejoin before the delay expires; confirm the pending resume is canceled, then leave again and confirm a fresh delay resumes the task.
- Check `/chunkyidle status`, `/chunkyidle reload`, and both `/chunkyidle toggle` states, including permission behavior.
- Test restart recovery with `active-before-shutdown` and `any-saved` policies and confirm eligible work resumes after a graceful restart.

## Runtime test record

Candidate tested: `chunkyidle-fabric-0.1.0+1.20.1-candidate-1.20-1.20.4.jar`

Fabric Loader: 0.19.3

Chunky: 1.3.146
Server: existing test server, temporarily set to offline mode for the whitelisted test bot; online mode, secure profiles, and whitelist-off were restored afterward.

| Minecraft | Fabric API | Startup | Bot join pauses tasks | Empty-server delay resumes tasks |
| --- | --- | --- | --- | --- |
| 1.20 | 0.83.0+1.20 | Pass | Pass | Pass |
| 1.20.1 | 0.92.11+1.20.1 | Pass | Pass | Pass |
| 1.20.2 | 0.91.6+1.20.2 | Pass | Pass | Pass |
| 1.20.3 | 0.91.1+1.20.3 | Pass | Pass | Pass |
| 1.20.4 | 0.97.3+1.20.4 | Pass | Pass | Pass |

On 1.20.4, `/chunkyidle status`, `/chunkyidle reload`, and both toggle states were checked. Graceful restart recovery resumed the saved tasks. This does not claim those additional checks were repeated on every version in the range.

The existing test world originated on Minecraft 1.21.10. Downgrading it produced expected old-world data warnings (including unknown trial-chambers data and random-sequence data); the server still started and the candidate checks passed. Use a world created on the target Minecraft version for clean compatibility testing.
