package dev.bikzxc.chunkyidle.automation;

import dev.bikzxc.chunkyidle.ChunkyIdle;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Validated, user-facing ChunkyIdle settings and comment-preserving file updates. */
public record IdleAutomationConfig(boolean enabled, long idleSeconds, boolean restartEnabled,
                                   ResumeScope resumeScope, ResumeWait resumeWait) {
    private static final Pattern DURATION = Pattern.compile("([0-9]+)([smh])");

    public static String defaultYaml() {
        return """
                #========================================#
                #              ChunkyIdle                #
                #                v%s                  #
                #               by %s                 #
                #========================================#
                #
                # Automatically manages Chunky world generation based on server activity.
                # Generation pauses while players are online and resumes when the server is empty.
                #
                # Support: https://github.com/Bikszxc/ChunkyIdle
                #

                #----------------------------------------#
                #              AUTOMATION                #
                #----------------------------------------#

                automation:
                  # Enable automatic pause and resume based on player activity.
                  #
                  # When enabled:
                  # - Generation pauses when a player joins the server.
                  # - Generation resumes after the server has remained empty
                  #   for the configured amount of time.
                  #
                  # Set to false to disable automatic player-based management.
                  enabled: true

                  # How long the server must remain empty before generation resumes.
                  #
                  # This timer starts when the last player leaves.
                  #
                  # Supported units: s (seconds), m (minutes), h (hours).
                  # Examples: 10s, 5m, 1h
                  wait-after-empty: 10s


                #----------------------------------------#
                #            SERVER RESTART              #
                #----------------------------------------#

                server-restart:
                  # Automatically resume eligible generation tasks after a server restart.
                  #
                  # When enabled, ChunkyIdle checks for saved generation tasks during startup
                  # and resumes those that match the selected resume policy.
                  enabled: true

                  # Choose which saved generation tasks are eligible for automatic resumption.
                  #
                  # active-before-shutdown:
                  #   Resume tasks that were running before shutdown or were automatically
                  #   paused by ChunkyIdle.
                  #
                  # any-saved:
                  #   Attempt every saved generation task in loaded worlds. This may also
                  #   resume tasks an administrator paused manually.
                  #
                  # Recommended: active-before-shutdown
                  tasks-to-resume: active-before-shutdown

                  # Choose when eligible tasks resume after startup.
                  #
                  # idle-time: use the empty-server wait above.
                  # immediately: resume as soon as the server is ready.
                  # Custom duration: wait a specific time after startup (10s, 5m, or 1h).
                  wait-before-resume: idle-time
                """.formatted(ChunkyIdle.VERSION, ChunkyIdle.AUTHORS);
    }

    public enum ResumeScope { ACTIVE_BEFORE_SHUTDOWN, ANY_SAVED }
    public enum WaitKind { IDLE_TIME, IMMEDIATELY, DURATION }
    public record ResumeWait(WaitKind kind, long seconds) { }

    public IdleAutomationConfig withEnabled(boolean value) {
        return new IdleAutomationConfig(value, idleSeconds, restartEnabled, resumeScope, resumeWait);
    }

    public long resumeDelaySeconds() {
        return switch (resumeWait.kind()) {
            case IMMEDIATELY -> 0;
            case IDLE_TIME -> idleSeconds;
            case DURATION -> resumeWait.seconds();
        };
    }

    public static long parseDuration(String value) {
        Matcher matcher = DURATION.matcher(value == null ? "" : value.trim());
        if (!matcher.matches()) throw new IllegalArgumentException("Use a duration such as 10s, 5m, or 1h.");
        try {
            long amount = Long.parseLong(matcher.group(1));
            long factor = switch (matcher.group(2)) { case "s" -> 1; case "m" -> 60; default -> 3600; };
            long seconds = Math.multiplyExact(amount, factor);
            Math.multiplyExact(seconds, 20L); // Ensure the value fits Minecraft's tick countdown.
            return seconds;
        } catch (ArithmeticException | NumberFormatException ex) {
            throw new IllegalArgumentException("That duration is too large for a server timer.", ex);
        }
    }

    public static IdleAutomationConfig parse(String yaml) {
        Object root = new Load(LoadSettings.builder().build()).loadFromString(yaml);
        if (!(root instanceof Map<?, ?> map)) throw new IllegalArgumentException("The config must contain YAML settings.");
        Map<?, ?> automation = section(map, "automation");
        Map<?, ?> restart = section(map, "server-restart");
        boolean enabled = bool(automation, "enabled");
        long idle = parseDuration(string(automation, "wait-after-empty"));
        boolean restartEnabled = bool(restart, "enabled");
        ResumeScope scope = switch (string(restart, "tasks-to-resume")) {
            case "active-before-shutdown" -> ResumeScope.ACTIVE_BEFORE_SHUTDOWN;
            case "any-saved" -> ResumeScope.ANY_SAVED;
            default -> throw new IllegalArgumentException("Choose active-before-shutdown or any-saved for tasks-to-resume.");
        };
        String wait = string(restart, "wait-before-resume");
        ResumeWait resumeWait = switch (wait) {
            case "idle-time" -> new ResumeWait(WaitKind.IDLE_TIME, 0);
            case "immediately" -> new ResumeWait(WaitKind.IMMEDIATELY, 0);
            default -> new ResumeWait(WaitKind.DURATION, parseDuration(wait));
        };
        return new IdleAutomationConfig(enabled, idle, restartEnabled, scope, resumeWait);
    }

    private static Map<?, ?> section(Map<?, ?> map, String key) {
        Object value = map.get(key);
        if (!(value instanceof Map<?, ?> section)) throw new IllegalArgumentException("Add the '" + key + "' settings group.");
        return section;
    }
    private static String string(Map<?, ?> map, String key) {
        Object value = map.get(key);
        if (!(value instanceof String text)) throw new IllegalArgumentException("Set '" + key + "' to a text value.");
        return text.trim();
    }
    private static boolean bool(Map<?, ?> map, String key) {
        Object value = map.get(key);
        if (!(value instanceof Boolean result)) throw new IllegalArgumentException("Set '" + key + "' to true or false.");
        return result;
    }
}
