package dev.bikzxc.chunkyidle.fabric;

import dev.bikzxc.chunkyidle.automation.ChunkyTaskController;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
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
                .map(FabricChunkyTaskController::worldId)
                .filter(api::isRunning)
                .collect(Collectors.toUnmodifiableSet());
    }

    static String worldId(ResourceKey<Level> key) {
        //? <1.21.11 {
        return key.location().toString();
        //?} else {
        /*return key.identifier().toString();*/
        //?}
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
