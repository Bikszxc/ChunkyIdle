# Chunky Idle

**Chunky Idle** is a small server-side companion for [Chunky](https://modrinth.com/mod/chunky). When a player joins, it pauses Chunky generation tasks that are currently running. After the server has been empty for the configured time, it continues the tasks ChunkyIdle paused.

It preserves Chunky's saved progress and never creates or starts a generation task. Optional restart recovery can continue eligible saved tasks after a graceful server restart.

## Compatibility

- Minecraft Java Edition **1.20.1**
- **Fabric** dedicated servers
- **Fabric API**
- **Chunky 1.3.146**

ChunkyIdle runs on the server; players do not need to install it on their clients. The initial release supports Fabric 1.20.1 only.

## Install

1. Set up a Fabric 1.20.1 server with Fabric API.
2. Install Chunky 1.3.146 on the server.
3. Place the ChunkyIdle Fabric jar in the server's `mods` folder.
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
