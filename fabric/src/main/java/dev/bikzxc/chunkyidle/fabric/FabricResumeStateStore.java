package dev.bikzxc.chunkyidle.fabric;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

/** Persists the world identifiers eligible for restart recovery. */
final class FabricResumeStateStore {
    private final Path path;

    FabricResumeStateStore(Path path) {
        this.path = path;
    }

    Set<String> read() throws IOException {
        if (Files.notExists(path)) return Set.of();
        return Files.readAllLines(path, StandardCharsets.UTF_8).stream()
                .filter(line -> !line.isBlank())
                .collect(Collectors.toSet());
    }

    void write(Set<String> worlds) throws IOException {
        Files.createDirectories(path.getParent());
        Files.write(path, worlds.stream().sorted().toList(), StandardCharsets.UTF_8);
    }

    void clear() throws IOException {
        Files.deleteIfExists(path);
    }
}
