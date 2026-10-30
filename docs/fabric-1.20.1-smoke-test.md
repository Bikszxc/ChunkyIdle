# Fabric 1.20.1 server smoke check

Use a dedicated Fabric 1.20.1 server with Fabric API, Chunky 1.3.146, and the built ChunkyIdle jar. Set `automation.wait-after-empty: 10s` in `config/chunkyidle/config.yml` for this check.

1. Start a Chunky task over a large enough selection that it will remain active for a few minutes. Record its world and progress with `/chunky progress`.
2. Join the server. Confirm the task pauses and its saved progress is retained. The console should not log a join message unless a resume wait was pending and canceled.
3. Leave the server. Confirm the console reports that Chunky will resume in ten seconds, then reports that the server has been empty for ten seconds and is resuming Chunky. Confirm the same task and progress continue.
4. Start or continue a task, leave, and rejoin before the wait expires. Confirm the console reports cancellation once and no completion message appears. Leave again and confirm a fresh ten-second wait is required.
5. As an OP, run `/chunkyidle status` once in-game and once from the server console. Confirm the player sees the styled dashboard and hover descriptions, while the console gets readable `[ChunkyIdle]`-prefixed plain lines. Confirm `/chunkyidle reload` and both `/chunkyidle toggle` states have clear feedback. Verify non-OPs cannot use the commands.
6. While Chunky is actively generating on an empty server, run `/chunkyidle toggle` from the console to turn automation off. Confirm the message explains that generation continues and player joins will not pause it while automation is off.
7. Confirm every ChunkyIdle console message visibly starts with `[ChunkyIdle]` and uses plain language. Invalid config reloads should explain how to fix the file in chat, while exception details stay in the server log.
8. Test restart recovery with `active-before-shutdown` and `any-saved`, and with `immediately`, `idle-time`, and a custom duration. Confirm manually paused tasks are only eligible under `any-saved` and no new tasks are started.
9. With Chunky actively generating, gracefully stop or restart the server. Confirm shutdown completes without a ChunkyIdle exception and the eligible task can resume after startup.

Restore your normal `automation.wait-after-empty` value after the smoke check.
