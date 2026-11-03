# Chunky Idle

**Chunky Idle** is a small server-side companion for [Chunky](https://modrinth.com/mod/chunky). When a player joins, it pauses Chunky generation tasks that are currently running. After the server has been empty for the configured time, it continues the tasks ChunkyIdle paused.

It preserves Chunky's saved progress and never creates or starts a generation task. Optional restart recovery can continue eligible saved tasks after a graceful server restart.

## Compatibility

ChunkyIdle runs on dedicated servers and does not need to be installed on clients. The versions below have been tested with the latest Chunky release available for each Minecraft version and loader. Chunky versions were checked against [Chunky's Modrinth releases](https://modrinth.com/plugin/chunky/versions) on October 31, 2026.

## Tested Versions

| Minecraft Version(s) | Loader | Chunky Version | Compatible? |
| --- | --- | --- | --- |
| 1.20–1.20.4 | Fabric | 1.3.146 | ✅ Shared-jar candidate runtime-verified; not released |
| 1.20.2–1.20.4 | NeoForge | 1.3.146 | ✅ Shared-jar candidate tested; not released |
| 1.21.1 | Fabric, NeoForge | 1.4.23 | ✅ Tested |
| 1.21.6–1.21.8 | Fabric, NeoForge | 1.4.40 | ✅ Tested |
| 1.21.9–1.21.10 | Fabric, NeoForge | 1.4.52 | ✅ Tested |
| 1.21.11 | Fabric | 1.4.55 | ✅ Tested |
| 1.21.11 | NeoForge | 1.4.54 | ✅ Tested |
| 26.1, 26.1.1, 26.1.2, 26.2 | Fabric | 1.5.3 | ✅ Tested |
| 26.1, 26.1.1, 26.1.2, 26.2 | NeoForge | 1.5.4 | ✅ Tested |

The Fabric 1.20–1.20.4 row was checked using the same shared-jar candidate on all five Minecraft versions. Startup, join pause, and idle resume passed on each. Commands and restart recovery were also checked on 1.20.4. The NeoForge 1.20.2–1.20.4 shared-jar candidate was used on all three versions, and the test bot joined and left successfully on each. Neither candidate is a release artifact yet.

The following other targets now compile in the build matrix, but still need dedicated-server runtime checks before being advertised as tested:

| Loader | Configured Minecraft targets | Chunky release line |
| --- | --- | --- |
| Fabric | 1.20–1.20.6, 1.21–1.21.11, 26.1.x–26.3 | See the [version parity and work tracker](docs/CHUNKY_VERSION_PARITY.md) |
| NeoForge | 1.21.1, 1.21.6–1.21.11, 26.1.x–26.3 | See the [version parity and work tracker](docs/CHUNKY_VERSION_PARITY.md) |

Builds normally produce version-specific jars with exact Minecraft metadata. The Fabric 1.20–1.20.4 and NeoForge 1.20.2–1.20.4 shared-jar candidates remain unpublished pending release packaging. See the [Fabric candidate test record](docs/fabric/versions/1.20-1.20.4-candidate.md) and [NeoForge candidate test record](docs/neoforge/versions/1.20.2-1.20.4-candidate.md).

## Install

1. Set up a dedicated server for a tested Minecraft version using a loader supported by that release.
2. Install the latest Chunky release available for that Minecraft version.
3. Place the matching ChunkyIdle loader jar in the server's `mods` folder.
4. Start the server. ChunkyIdle creates `config/chunkyidle/config.yml` on first startup.

ChunkyIdle is inactive when Chunky is not installed. It does not automatically start a Chunky task; start or configure generation with Chunky's own commands.

## How it works

- When a player joins, ChunkyIdle pauses Chunky tasks that are running at that time.
- When the last player leaves, the empty-server timer begins. A new join cancels the timer.
- If the server remains empty until the timer expires, ChunkyIdle continues the tasks it paused.
- It only resumes saved tasks. It does not create a task or change a task's world or selection.
- If an operator starts a new Chunky task while players are already online, ChunkyIdle does not automatically pause that new task unless another player joins.

## Configuration

Edit `config/chunkyidle/config.yml` while the server is stopped, or run `/chunkyidle reload` after editing. Newly generated configs use a 10-second empty-server wait. Existing config values are kept as-is when updating ChunkyIdle.

```yaml
#========================================#
#              ChunkyIdle                #
#                v0.1.0                  #
#               by Bikzxc                 #
#========================================#
#
# Automatically manages Chunky world generation based on server activity.
# Support: https://github.com/Bikszxc/ChunkyIdle
#

#----------------------------------------#
#              AUTOMATION                #
#----------------------------------------#

automation:
  # Pause running Chunky tasks when a player joins, then continue them after the server is empty.
  enabled: true

  # Time the server must remain empty before Chunky continues saved tasks.
  # Use an integer and one lowercase unit: s (seconds), m (minutes), or h (hours).
  # Examples: 10s, 5m, 1h
  wait-after-empty: 10s


#----------------------------------------#
#            SERVER RESTART              #
#----------------------------------------#

server-restart:
  # Continue eligible saved tasks after a graceful server restart.
  enabled: true

  # active-before-shutdown resumes tasks running or paused by ChunkyIdle at shutdown.
  # any-saved may also resume tasks an administrator paused manually.
  tasks-to-resume: active-before-shutdown

  # idle-time uses automation.wait-after-empty.
  # immediately resumes as the server starts.
  # Or use a duration such as 10s, 5m, or 1h, measured from startup.
  wait-before-resume: idle-time
```

The only accepted restart policies are `active-before-shutdown` and `any-saved`. The restart delay accepts `idle-time`, `immediately`, or a duration. Durations must use a single unit, so `1h30m` is not supported.

If `config/chunkyidle/config.yml` does not exist but the old `config/chunkyidle.properties` file does, ChunkyIdle carries over its `idle-seconds` value to the new YAML config. The old file is left in place. Once a YAML config exists, its values are not automatically changed.

### Restart recovery notes

- By default, `active-before-shutdown` uses the task worlds ChunkyIdle observed running, plus the tasks it paused, when the server shuts down gracefully.
- `any-saved` tries saved tasks in loaded worlds and may resume work an administrator paused manually.
- An abrupt crash may not save the latest task snapshot.
- If a player joins during a pending startup recovery wait, that wait is canceled. When the server becomes empty again, the normal `wait-after-empty` timer applies.

## Commands

These commands require operator permission level 2. They can be run in-game by an OP or from the server console.

| Command | What it does |
| --- | --- |
| `/chunkyidle status` | Shows the loaded settings and current automation state. In-game, setting labels have hover descriptions. |
| `/chunkyidle reload` | Validates and reloads the entire YAML config. If the config is invalid, the last working settings remain active. |
| `/chunkyidle toggle` | Turns player-based automation off or on and saves that setting in the YAML file. |

When automation is toggled off, tasks already paused by ChunkyIdle stay paused. Any tasks Chunky is currently running continue, and player joins will not automatically pause them while automation is off. Toggling automation back on does not start a new task.

## Console messages

ChunkyIdle messages include a visible `[ChunkyIdle]` prefix. For example:

```text
[ChunkyIdle] Server is empty. Chunky will resume in 10 seconds.
[ChunkyIdle] Server has been empty for 10 seconds. Resuming Chunky.
```

If a player joins during the wait, ChunkyIdle reports that the pending resume wait was canceled. `/chunkyidle status` prints a plain, prefixed summary in the console and a styled dashboard in-game.

## Troubleshooting

- **ChunkyIdle says Chunky was not found:** check that Chunky is installed in the server's `mods` folder and enabled for the server.
- **Settings could not be loaded:** fix the invalid YAML or unsupported value in `config/chunkyidle/config.yml`, then run `/chunkyidle reload`. On startup with an invalid config, automation stays inactive until the config is fixed and loaded.
- **A saved task did not resume after a restart:** check that the shutdown was graceful, `server-restart.enabled` and `automation.enabled` are both `true`, and the selected policy includes the saved task.
- **A task continues after `any-saved` is selected:** this policy may also resume a task paused manually by an administrator. Use `active-before-shutdown` to limit recovery to tasks ChunkyIdle recorded.

## License

ChunkyIdle is distributed under the [MIT License](LICENSE).

## Links

- [Source code and issue tracker](https://github.com/Bikszxc/ChunkyIdle)
- [Chunky on Modrinth](https://modrinth.com/mod/chunky)
