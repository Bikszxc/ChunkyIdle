package dev.bikzxc.chunkyidle.automation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class IdleAutomationConfigTest {
    @TempDir Path temp;

    @Test void parsesSupportedDurationsAndRejectsCompoundOrInvalidValues() {
        assertEquals(10, IdleAutomationConfig.parseDuration("10s"));
        assertEquals(60, IdleAutomationConfig.parseDuration("1m"));
        assertEquals(3600, IdleAutomationConfig.parseDuration("1h"));
        assertEquals(0, IdleAutomationConfig.parseDuration("0s"));
        assertThrows(IllegalArgumentException.class, () -> IdleAutomationConfig.parseDuration("1h30m"));
        assertThrows(IllegalArgumentException.class, () -> IdleAutomationConfig.parseDuration("-1m"));
    }

    @Test void generatedDefaultsAreFriendlyAndUseTenSeconds() throws Exception {
        var config = IdleAutomationConfig.load(temp.resolve("chunkyidle/config.yml"), temp.resolve("chunkyidle.properties"));
        assertTrue(config.enabled());
        assertEquals(10, config.idleSeconds());
        assertTrue(config.restartEnabled());
        assertEquals(IdleAutomationConfig.ResumeScope.ACTIVE_BEFORE_SHUTDOWN, config.resumeScope());
        String generated = Files.readString(temp.resolve("chunkyidle/config.yml"));
        assertTrue(generated.contains("any-saved"));
        assertTrue(generated.contains("v" + dev.bikzxc.chunkyidle.ChunkyIdle.VERSION));
        assertTrue(generated.contains("by " + dev.bikzxc.chunkyidle.ChunkyIdle.AUTHORS));
        assertTrue(generated.contains("https://github.com/Bikszxc/ChunkyIdle"));
        assertTrue(generated.contains("wait-after-empty: 10s"));
    }

    @Test void migratesLegacyIdleSeconds() throws Exception {
        Path legacy = temp.resolve("chunkyidle.properties");
        Files.writeString(legacy, "idle-seconds=42\n");
        Path configPath = temp.resolve("chunkyidle/config.yml");
        assertEquals(42, IdleAutomationConfig.load(configPath, legacy).idleSeconds());
        assertTrue(Files.exists(legacy));
    }

    @Test void validatesWholeYamlAndCanSelectStartupOptions() {
        String yaml = IdleAutomationConfig.defaultYaml().replace("wait-after-empty: 10s", "wait-after-empty: 2m")
                .replace("tasks-to-resume: active-before-shutdown", "tasks-to-resume: any-saved")
                .replace("wait-before-resume: idle-time", "wait-before-resume: immediately");
        var config = IdleAutomationConfig.parse(yaml);
        assertEquals(120, config.idleSeconds());
        assertEquals(IdleAutomationConfig.ResumeScope.ANY_SAVED, config.resumeScope());
        assertEquals(IdleAutomationConfig.WaitKind.IMMEDIATELY, config.resumeWait().kind());
        assertThrows(RuntimeException.class, () -> IdleAutomationConfig.parse("automation: [bad"));
    }

    @Test void togglePreservesYamlCommentsAndOtherSettings() throws Exception {
        Path path = temp.resolve("config.yml");
        Files.writeString(path, IdleAutomationConfig.defaultYaml());
        IdleAutomationConfig.setEnabledPreservingComments(path, false);
        String changed = Files.readString(path);
        assertTrue(changed.contains("# Enable automatic pause and resume based on player activity."));
        assertTrue(changed.contains("enabled: false"));
        assertTrue(changed.contains("wait-after-empty: 10s"));
        assertTrue(changed.contains("server-restart:"));
    }

    @Test void loadingAnExistingConfigDoesNotChangeItsValuesOrComments() throws Exception {
        Path path = temp.resolve("chunkyidle/config.yml");
        Files.createDirectories(path.getParent());
        String existing = IdleAutomationConfig.defaultYaml().replace("wait-after-empty: 10s", "wait-after-empty: 5m")
                .replace("# Enable automatic pause and resume", "# My server's custom comment\n  # Enable automatic pause and resume");
        Files.writeString(path, existing);
        var loaded = IdleAutomationConfig.load(path, temp.resolve("chunkyidle.properties"));
        assertEquals(300, loaded.idleSeconds());
        assertEquals(existing, Files.readString(path));
    }

    @Test void acceptsCustomRestartDelayAndRejectsDurationsThatOverflowTheTickTimer() {
        var config = IdleAutomationConfig.parse(IdleAutomationConfig.defaultYaml()
                .replace("wait-before-resume: idle-time", "wait-before-resume: 10s"));
        assertEquals(IdleAutomationConfig.WaitKind.DURATION, config.resumeWait().kind());
        assertEquals(10, config.resumeWait().seconds());
        assertThrows(IllegalArgumentException.class,
                () -> IdleAutomationConfig.parse(IdleAutomationConfig.defaultYaml()
                        .replace("wait-after-empty: 10s", "wait-after-empty: 999999999999999999s")));
    }
}
