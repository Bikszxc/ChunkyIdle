package dev.bikzxc.chunkyidle.automation;

import java.util.HashSet;
import java.util.Set;

/** Shared player-aware pause/resume state machine. Loader entry points forward events here. */
public final class IdleAutomation {
    private static final long TICKS_PER_SECOND = 20;
    private final ChunkyTaskController chunky;
    private final Set<String> pausedByUs = new HashSet<>();
    private final Set<String> lastObservedRunningWorlds = new HashSet<>();
    private long idleDelaySeconds;
    private long idleDelayTicks;
    private long ticksUntilResume = -1;
    private long currentResumeDelaySeconds;
    private long completedResumeDelaySeconds;
    private long resumeCompletionCount;
    private boolean enabled = true;

    public IdleAutomation(ChunkyTaskController chunky, int idleSeconds) {
        this(chunky, new IdleAutomationConfig(true, idleSeconds, false,
                IdleAutomationConfig.ResumeScope.ACTIVE_BEFORE_SHUTDOWN,
                new IdleAutomationConfig.ResumeWait(IdleAutomationConfig.WaitKind.IDLE_TIME, 0)));
    }

    public IdleAutomation(ChunkyTaskController chunky, IdleAutomationConfig config) {
        this.chunky = chunky;
        applyConfig(config);
    }

    public void applyConfig(IdleAutomationConfig config) {
        enabled = config.enabled();
        idleDelaySeconds = config.idleSeconds();
        idleDelayTicks = Math.multiplyExact(config.idleSeconds(), TICKS_PER_SECOND);
        if (!enabled) ticksUntilResume = -1;
        else if (ticksUntilResume >= 0) {
            ticksUntilResume = idleDelayTicks;
            currentResumeDelaySeconds = idleDelaySeconds;
        }
    }

    public void onPlayerJoined() {
        refreshRunningWorldSnapshot();
        ticksUntilResume = -1;
        if (!enabled) return;
        for (String world : Set.copyOf(lastObservedRunningWorlds)) if (chunky.pause(world)) pausedByUs.add(world);
    }

    public void onServerBecameEmpty() {
        if (enabled && !pausedByUs.isEmpty()) {
            ticksUntilResume = idleDelayTicks;
            currentResumeDelaySeconds = idleDelaySeconds;
        }
    }

    /** Candidates were saved at graceful shutdown and are resumed only after an empty-server wait. */
    public void recoverOnStartup(Set<String> worlds, long delaySeconds) {
        if (!enabled) return;
        pausedByUs.addAll(worlds);
        ticksUntilResume = Math.multiplyExact(delaySeconds, TICKS_PER_SECOND);
        currentResumeDelaySeconds = delaySeconds;
    }

    public Set<String> worldsToSave() {
        Set<String> worlds = new HashSet<>(lastObservedRunningWorlds);
        worlds.addAll(pausedByUs);
        return Set.copyOf(worlds);
    }

    public void onServerTick(boolean hasPlayers) {
        refreshRunningWorldSnapshot();
        if (hasPlayers) { ticksUntilResume = -1; return; }
        if (!enabled || ticksUntilResume < 0) return;
        if (ticksUntilResume > 0) ticksUntilResume--;
        if (ticksUntilResume == 0) {
            completedResumeDelaySeconds = currentResumeDelaySeconds;
            resumeCompletionCount++;
            for (String world : Set.copyOf(pausedByUs)) chunky.resume(world);
            pausedByUs.clear();
            ticksUntilResume = -1;
        }
    }

    public boolean isResumePending() { return ticksUntilResume >= 0; }
    public long ticksUntilResume() { return ticksUntilResume; }
    public long completedResumeDelaySeconds() { return completedResumeDelaySeconds; }
    public long resumeCompletionCount() { return resumeCompletionCount; }
    public boolean isEnabled() { return enabled; }
    public Set<String> pausedWorlds() { return Set.copyOf(pausedByUs); }
    public void toggleEnabled(boolean value) {
        enabled = value;
        ticksUntilResume = -1;
    }

    private void refreshRunningWorldSnapshot() {
        try {
            Set<String> running = chunky.runningWorlds();
            lastObservedRunningWorlds.clear();
            lastObservedRunningWorlds.addAll(running);
        } catch (IllegalStateException ignored) {
            // Chunky's provider can disappear during shutdown. Keep the last server-tick snapshot.
        }
    }
}
