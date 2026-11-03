# Chunky Idle

**Keep Chunky world generation out of players’ way.** Chunky Idle pauses active Chunky tasks when a player joins and resumes those tasks after the server has been empty for a configurable time. It preserves Chunky’s saved progress and never starts a generation task for you.

Chunky Idle runs on the server. Players do not need to install it on their clients.

<p>
  <a href="https://www.curseforge.com/minecraft/mc-mods/chunky-idle"><img src="https://img.shields.io/badge/Available%20on-CurseForge-F16436?style=for-the-badge&logo=curseforge&logoColor=white" alt="Available on CurseForge"></a>
  <a href="https://modrinth.com/mod/ECodck3H"><img src="https://img.shields.io/badge/Available%20on-Modrinth-1BD96A?style=for-the-badge&logo=modrinth&logoColor=white" alt="Available on Modrinth"></a>
</p>

Install the file that matches both your Minecraft version and loader. Chunky Idle requires [Chunky](https://modrinth.com/mod/chunky) on the server.

## Install

1. Set up a dedicated server with Fabric or NeoForge for a supported Minecraft version.
2. Install Chunky and the matching Chunky Idle jar in the server’s `mods` folder.
3. Start the server. On first startup, Chunky Idle creates `config/chunkyidle/config.yml`.
4. Start or configure world generation with Chunky’s commands. Chunky Idle only manages tasks that Chunky has saved; it does not start tasks itself.

## At a glance

- A player joining pauses Chunky tasks that are running at that time.
- When the last player leaves, an empty-server timer starts. A player joining during the wait cancels it.
- If the server stays empty until the timer expires, Chunky Idle continues the tasks it paused.
- Starting a new Chunky task while players are already online does not pause that task until another player joins.
- Optional restart recovery can continue eligible saved tasks after a graceful restart.

## Compatibility

The Fabric versions below were checked with the latest compatible Chunky release available at the time. Chunky versions were checked against [Chunky’s Modrinth releases](https://modrinth.com/plugin/chunky/versions) on October 31, 2026.

| Minecraft | Loader | Chunky | Status |
| --- | --- | --- | --- |
| 1.21.1 | Fabric | 1.4.23 | Tested |
| 1.21.6–1.21.8 | Fabric | 1.4.40 | Tested |
| 1.21.9–1.21.10 | Fabric | 1.4.52 | Tested |
| 1.21.11 | Fabric | 1.4.55 | Tested |
| 26.1, 26.1.1, 26.1.2, 26.2 | Fabric | 1.5.3 | Tested |

Other configured targets compile in the build matrix, but still need dedicated-server runtime checks before being described as tested:

| Loader | Build targets |
| --- | --- |
| Fabric | 1.20–1.20.6, 1.21–1.21.11, 26.1.x–26.3 |
| NeoForge | 1.21.1, 1.21.6–1.21.11, 26.1.x–26.3 |

See the [version parity and work tracker](docs/CHUNKY_VERSION_PARITY.md) for Chunky release mappings and runtime test progress.

## Commands

All commands require operator permission level 2. Run them in-game as an operator or from the server console.

| Command | Description |
| --- | --- |
| `/chunkyidle status` | Shows loaded settings and the current automation state. In-game, setting labels include hover descriptions. |
| `/chunkyidle reload` | Validates and reloads the YAML configuration. If it is invalid, the last working settings stay active. |
| `/chunkyidle toggle` | Turns player-based automation off or on and saves the setting. |

When automation is turned off, running tasks continue and tasks already paused by Chunky Idle stay paused. Player joins will not automatically pause running tasks while automation is off. Turning automation back on does not start a task.

## Configuration

The default configuration waits 10 seconds after the server becomes empty and enables restart recovery. Edit `config/chunkyidle/config.yml` while the server is stopped, or run `/chunkyidle reload` after editing.

```yaml
automation:
  enabled: true
  wait-after-empty: 10s

server-restart:
  enabled: true
  tasks-to-resume: active-before-shutdown
  wait-before-resume: idle-time
```

| Setting | Description |
| --- | --- |
| `automation.enabled` | Enables or disables player-based pausing and resuming. |
| `automation.wait-after-empty` | Empty-server wait. Use an integer and one lowercase unit: seconds (`s`), minutes (`m`), or hours (`h`), for example `10s`, `5m`, or `1h`. |
| `server-restart.enabled` | Enables recovery of eligible tasks after a graceful restart. |
| `server-restart.tasks-to-resume` | `active-before-shutdown` resumes task worlds Chunky Idle recorded as running or paused by it. `any-saved` can also resume tasks an administrator paused manually. |
| `server-restart.wait-before-resume` | `idle-time` uses `automation.wait-after-empty`; `immediately` resumes when startup is ready; or set a duration such as `10s`, `5m`, or `1h`. |

Durations use a single unit, so `1h30m` is not supported. Existing config values are preserved when updating Chunky Idle. If the YAML file does not exist but the old `config/chunkyidle.properties` file does, Chunky Idle carries over its `idle-seconds` value; the old file is left in place.

### Restart recovery details

- `active-before-shutdown` records worlds Chunky Idle observed running and tasks it paused when the server shuts down gracefully.
- `any-saved` checks saved tasks in loaded worlds and may resume work an administrator paused manually.
- An abrupt crash may not save the latest task snapshot.
- If a player joins during a pending startup recovery wait, that wait is canceled. When the server becomes empty again, the normal `wait-after-empty` timer applies.

## Troubleshooting

- **Chunky Idle says Chunky was not found:** Make sure Chunky is installed and enabled on the server.
- **Settings could not be loaded:** Fix the invalid YAML or unsupported value in `config/chunkyidle/config.yml`, then run `/chunkyidle reload`. If startup config loading fails, automation stays inactive until the config is fixed and reloaded.
- **A task did not resume after restart:** Confirm the server shut down gracefully, both `server-restart.enabled` and `automation.enabled` are `true`, and the selected recovery policy includes that task.
- **A manually paused task resumed:** `any-saved` can include tasks paused by an administrator. Use `active-before-shutdown` to limit recovery to tasks Chunky Idle recorded.

## Development

Build every configured Minecraft version and loader target with:

```sh
./gradlew chiseledBuild
```

On Windows, use `gradlew.bat chiseledBuild`. See the [version parity and work tracker](docs/CHUNKY_VERSION_PARITY.md) for build and runtime test coverage.

## Help and links

- [Report a bug or request a feature](https://github.com/Bikszxc/ChunkyIdle/issues)
- [Source code](https://github.com/Bikszxc/ChunkyIdle)
- [Chunky on Modrinth](https://modrinth.com/mod/chunky)

<p>
  <a href="https://ko-fi.com/bikzxc"><img src="https://ko-fi.com/img/githubbutton_sm.svg" alt="Buy me a coffee at Ko-fi"></a>
</p>

## License

Chunky Idle is distributed under the [MIT License](LICENSE).
