package dev.bikzxc.chunkyidle.automation;

import java.util.Set;

/** Loader-specific access to currently running Chunky generation tasks. */
public interface ChunkyTaskController {
    /** Returns stable world identifiers for worlds with active generation tasks. */
    Set<String> runningWorlds();

    /** Pauses an active task and saves its progress. */
    boolean pause(String world);

    /** Resumes a paused task if Chunky still has one saved. */
    boolean resume(String world);
}
