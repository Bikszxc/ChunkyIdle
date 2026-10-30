package dev.bikzxc.chunkyidle.fabric;

import dev.bikzxc.chunkyidle.automation.ChunkyTaskController;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.popcraft.chunky.ChunkyProvider;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/** Fabric adapter for Chunky's public task API. Calls must stay on the server thread. */
final class FabricChunkyTaskController implements ChunkyTaskController {
    private final MinecraftServer server;

    FabricChunkyTaskController(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public Set<String> runningWorlds() {
        var api = ChunkyProvider.get().getApi();
        return StreamSupport.stream(server.getAllLevels().spliterator(), false)
                .map(ServerLevel::dimension)
                .map(key -> key.location().toString())
                .filter(api::isRunning)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean pause(String world) {
        return ChunkyProvider.get().getApi().pauseTask(world);
    }

    @Override
    public boolean resume(String world) {
        return ChunkyProvider.get().getApi().continueTask(world);
    }
}
