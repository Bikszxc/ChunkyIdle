package dev.bikzxc.chunkyidle.automation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

/** Reads, creates, migrates, and updates the on-disk automation config. */
public final class IdleAutomationConfigFile {
    private static final String DEFAULT_IDLE_SETTING = "wait-after-empty: 10s";

    private IdleAutomationConfigFile() {
    }

    public static IdleAutomationConfig load(Path path, Path legacyPath) throws IOException {
        if (Files.notExists(path)) {
            createDefault(path, legacyPath);
        }
        return IdleAutomationConfig.parse(Files.readString(path, StandardCharsets.UTF_8));
    }

    public static void setEnabledPreservingComments(Path path, boolean enabled) throws IOException {
        String[] lines = Files.readString(path, StandardCharsets.UTF_8).split("\\R", -1);
        boolean inAutomationSection = false;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (!line.startsWith(" ") && line.trim().endsWith(":")) {
                inAutomationSection = line.trim().equals("automation:");
            }
            if (inAutomationSection && line.matches("\\s+enabled\\s*:\\s*(true|false)(\\s+#.*)?")) {
                lines[i] = line.replaceFirst("(enabled\\s*:\\s*)(true|false)", "$1" + enabled);
                writeAtomically(path, String.join(System.lineSeparator(), lines));
                return;
            }
        }

        throw new IOException("Could not find automation.enabled; settings were not changed.");
    }

    private static void createDefault(Path path, Path legacyPath) throws IOException {
        Files.createDirectories(path.getParent());
        String yaml = IdleAutomationConfig.defaultYaml();
        if (Files.exists(legacyPath)) {
            yaml = migrateIdleDelay(yaml, legacyPath);
        }
        Files.writeString(path, yaml, StandardCharsets.UTF_8);
    }

    private static String migrateIdleDelay(String yaml, Path legacyPath) throws IOException {
        Properties legacy = new Properties();
        try (var input = Files.newInputStream(legacyPath)) {
            legacy.load(input);
        }

        String seconds = legacy.getProperty("idle-seconds", "300").trim();
        try {
            long idleSeconds = Long.parseLong(seconds);
            return yaml.replace(DEFAULT_IDLE_SETTING, "wait-after-empty: " + idleSeconds + "s");
        } catch (NumberFormatException exception) {
            throw new IOException("The old idle-seconds setting is invalid.", exception);
        }
    }

    private static void writeAtomically(Path path, String contents) throws IOException {
        Path temporaryPath = path.resolveSibling(path.getFileName() + ".tmp");
        Files.writeString(temporaryPath, contents, StandardCharsets.UTF_8);
        try {
            Files.move(temporaryPath, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (java.nio.file.AtomicMoveNotSupportedException exception) {
            Files.move(temporaryPath, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
