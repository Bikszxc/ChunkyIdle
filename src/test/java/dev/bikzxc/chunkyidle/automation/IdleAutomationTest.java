package dev.bikzxc.chunkyidle.automation;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdleAutomationTest {
    @Test
    void pausesAndResumesOnlyTasksItSuccessfullyPausedAcrossWorlds() {
        FakeChunky chunky = new FakeChunky(Set.of("minecraft:overworld", "minecraft:the_nether"));
        chunky.pauseFailures.add("minecraft:the_nether");
        IdleAutomation automation = new IdleAutomation(chunky, 1);

        automation.onPlayerJoined();
        assertEquals(Set.of("minecraft:overworld"), chunky.pausedWorlds);
        assertEquals(Set.of("minecraft:the_nether"), chunky.runningWorlds());

        automation.onServerBecameEmpty();
        tick(automation, 20, false);

        assertEquals(Set.of("minecraft:overworld", "minecraft:the_nether"), chunky.runningWorlds());
        assertEquals(1, chunky.resumeCalls);
        assertFalse(automation.isResumePending());
    }

    @Test
    void resumesAtDelayExpiryAndNotBefore() {
        FakeChunky chunky = new FakeChunky(Set.of("minecraft:overworld"));
        IdleAutomation automation = new IdleAutomation(chunky, 2);

        automation.onPlayerJoined();
        automation.onServerBecameEmpty();
        tick(automation, 39, false);
        assertTrue(chunky.runningWorlds().isEmpty());
        assertTrue(automation.isResumePending());

        automation.onServerTick(false);

        assertEquals(Set.of("minecraft:overworld"), chunky.runningWorlds());
        assertFalse(automation.isResumePending());
    }

    @Test
    void rejoiningCancelsCountdownUntilAFullNewIdleDelayPasses() {
        FakeChunky chunky = new FakeChunky(Set.of("minecraft:overworld"));
        IdleAutomation automation = new IdleAutomation(chunky, 2);

        automation.onPlayerJoined();
        automation.onServerBecameEmpty();
        tick(automation, 20, false);
        automation.onPlayerJoined();
        tick(automation, 100, true);
        assertTrue(chunky.runningWorlds().isEmpty());
        assertFalse(automation.isResumePending());

        automation.onServerBecameEmpty();
        tick(automation, 39, false);
        assertTrue(chunky.runningWorlds().isEmpty());
        automation.onServerTick(false);

        assertEquals(Set.of("minecraft:overworld"), chunky.runningWorlds());
    }

    @Test
    void zeroSecondDelayResumesOnTheNextServerTick() {
        FakeChunky chunky = new FakeChunky(Set.of("minecraft:overworld"));
        IdleAutomation automation = new IdleAutomation(chunky, 0);

        automation.onPlayerJoined();
        automation.onServerBecameEmpty();
        assertTrue(chunky.runningWorlds().isEmpty());
        automation.onServerTick(false);

        assertEquals(Set.of("minecraft:overworld"), chunky.runningWorlds());
        assertFalse(automation.isResumePending());
    }

    @Test
    void doesNotStartCountdownOrResumeWhenNoTaskWasPaused() {
        FakeChunky chunky = new FakeChunky(Set.of());
        IdleAutomation automation = new IdleAutomation(chunky, 1);

        automation.onPlayerJoined();
        automation.onServerBecameEmpty();
        tick(automation, 40, false);

        assertTrue(chunky.runningWorlds().isEmpty());
        assertEquals(0, chunky.resumeCalls);
        assertFalse(automation.isResumePending());
    }

    @Test
    void doesNotRetryAResumeWhenTheSavedTaskIsNoLongerAvailable() {
        FakeChunky chunky = new FakeChunky(Set.of("minecraft:overworld"));
        IdleAutomation automation = new IdleAutomation(chunky, 0);
        automation.onPlayerJoined();
        chunky.resumeFailures.add("minecraft:overworld");
        automation.onServerBecameEmpty();

        automation.onServerTick(false);
        automation.onServerTick(false);

        assertEquals(1, chunky.resumeCalls);
        assertFalse(automation.isResumePending());
    }

    @Test
    void startupRecoveryResumesOnlyProvidedCandidatesAfterConfiguredDelay() {
        FakeChunky chunky = new FakeChunky(Set.of());
        chunky.pausedWorlds.add("minecraft:overworld");
        chunky.pausedWorlds.add("minecraft:the_nether");
        IdleAutomation automation = new IdleAutomation(chunky, 300);

        automation.recoverOnStartup(Set.of("minecraft:overworld"), 1);
        tick(automation, 19, false);
        assertTrue(chunky.runningWorlds().isEmpty());
        tick(automation, 1, false);

        assertEquals(Set.of("minecraft:overworld"), chunky.runningWorlds());
        assertTrue(chunky.pausedWorlds.contains("minecraft:the_nether"));
    }

    @Test
    void playerJoiningCancelsStartupRecoveryUntilServerBecomesEmptyAgain() {
        FakeChunky chunky = new FakeChunky(Set.of());
        chunky.pausedWorlds.add("minecraft:overworld");
        IdleAutomation automation = new IdleAutomation(chunky, 1);
        automation.recoverOnStartup(Set.of("minecraft:overworld"), 0);

        automation.onPlayerJoined();
        automation.onServerTick(true);
        assertTrue(chunky.runningWorlds().isEmpty());
        assertFalse(automation.isResumePending());

        automation.onServerBecameEmpty();
        tick(automation, 19, false);
        assertTrue(chunky.runningWorlds().isEmpty());
        automation.onServerTick(false);
        assertEquals(Set.of("minecraft:overworld"), chunky.runningWorlds());
    }

    @Test
    void disablingAutomationStopsTimersAndLeavesPausedTasksPaused() {
        FakeChunky chunky = new FakeChunky(Set.of("minecraft:overworld"));
        IdleAutomation automation = new IdleAutomation(chunky, 1);
        automation.onPlayerJoined();
        automation.onServerBecameEmpty();
        automation.toggleEnabled(false);
        tick(automation, 40, false);

        assertTrue(chunky.runningWorlds().isEmpty());
        assertFalse(automation.isResumePending());
        assertEquals(Set.of("minecraft:overworld"), automation.pausedWorlds());
    }

    @Test
    void shutdownSnapshotDoesNotQueryChunkyAfterItsProviderHasUnloaded() {
        FakeChunky chunky = new FakeChunky(Set.of("minecraft:overworld"));
        IdleAutomation automation = new IdleAutomation(chunky, 10);
        automation.onServerTick(false);
        chunky.unavailable = true;

        assertEquals(Set.of("minecraft:overworld"), automation.worldsToSave());
    }

    private static void tick(IdleAutomation automation, int count, boolean hasPlayers) {
        for (int i = 0; i < count; i++) {
            automation.onServerTick(hasPlayers);
        }
    }

    private static final class FakeChunky implements ChunkyTaskController {
        private final Set<String> running = new HashSet<>();
        private final Set<String> pausedWorlds = new HashSet<>();
        private final Set<String> pauseFailures = new HashSet<>();
        private final Set<String> resumeFailures = new HashSet<>();
        private int resumeCalls;
        private boolean unavailable;

        private FakeChunky(Set<String> initialRunningWorlds) {
            running.addAll(initialRunningWorlds);
        }

        @Override
        public Set<String> runningWorlds() {
            if (unavailable) throw new IllegalStateException("Chunky is not loaded.");
            return Set.copyOf(running);
        }

        @Override
        public boolean pause(String world) {
            if (pauseFailures.contains(world) || !running.remove(world)) {
                return false;
            }
            pausedWorlds.add(world);
            return true;
        }

        @Override
        public boolean resume(String world) {
            resumeCalls++;
            if (resumeFailures.contains(world) || !pausedWorlds.remove(world)) {
                return false;
            }
            running.add(world);
            return true;
        }
    }
}
