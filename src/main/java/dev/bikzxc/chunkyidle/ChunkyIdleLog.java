package dev.bikzxc.chunkyidle;

/** Console logging helpers; Minecraft's standard log format omits logger names. */
public final class ChunkyIdleLog {
    private static final String PREFIX = "[ChunkyIdle] ";

    private ChunkyIdleLog() { }

    public static String prefix(String message) { return PREFIX + message; }

    public static void info(String message) { ChunkyIdle.LOGGER.info(prefix(message)); }
    public static void info(String message, Object argument) { ChunkyIdle.LOGGER.info(prefix(message), argument); }
    public static void warn(String message) { ChunkyIdle.LOGGER.warn(prefix(message)); }
    public static void warn(String message, Throwable cause) { ChunkyIdle.LOGGER.warn(prefix(message), cause); }
    public static void error(String message) { ChunkyIdle.LOGGER.error(prefix(message)); }
    public static void error(String message, Throwable cause) { ChunkyIdle.LOGGER.error(prefix(message), cause); }
}
