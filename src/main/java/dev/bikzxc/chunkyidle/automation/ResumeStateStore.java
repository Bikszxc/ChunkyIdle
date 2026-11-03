package dev.bikzxc.chunkyidle.automation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.stream.Collectors;

/** Persists the world identifiers eligible for restart recovery. */
public final class ResumeStateStore {
    private final Path path;

    public ResumeStateStore(Path path) {
        this.path = path;
    }

    public Set<String> read() throws IOException {
        if (Files.notExists(path)) return Set.of();
        return Files.readAllLines(path, StandardCharsets.UTF_8).stream()
                .filter(line -> !line.isBlank())
                .collect(Collectors.toSet());
    }

    public void write(Set<String> worlds) throws IOException {
        Path directory = path.toAbsolutePath().getParent();
        Files.createDirectories(directory);
        Path temporary = Files.createTempFile(directory, path.getFileName().toString(), ".tmp");
        try {
            Files.write(temporary, worlds.stream().sorted().toList(), StandardCharsets.UTF_8);
            try {
                Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    public void clear() throws IOException {
        Files.deleteIfExists(path);
    }
}
