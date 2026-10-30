package dev.bikzxc.chunkyidle.fabric;

import dev.bikzxc.chunkyidle.ChunkyIdle;
import dev.bikzxc.chunkyidle.ChunkyIdleLog;
import dev.bikzxc.chunkyidle.automation.IdleAutomation;
import dev.bikzxc.chunkyidle.automation.IdleAutomationConfig;
import dev.bikzxc.chunkyidle.automation.IdleAutomationConfigFile;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ChunkyIdleFabric implements ModInitializer {
    private static final Path DIRECTORY = FabricLoader.getInstance().getConfigDir().resolve("chunkyidle");
    private static final Path CONFIG = DIRECTORY.resolve("config.yml");
    private static final Path LEGACY = FabricLoader.getInstance().getConfigDir().resolve("chunkyidle.properties");
    private static final FabricResumeStateStore RESUME_STATE = new FabricResumeStateStore(DIRECTORY.resolve("resume-state.txt"));
    @Nullable private static IdleAutomation automation;
    @Nullable private static IdleAutomationConfig settings;
    @Nullable private static MinecraftServer currentServer;
    private static boolean checkForEmptyServer;

    @Override public void onInitialize() {
        ChunkyIdle.init();
        ServerLifecycleEvents.SERVER_STARTED.register(server -> start(server, false));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (automation != null) {
                boolean canceledWait = automation.isResumePending();
                automation.onPlayerJoined();
                if (canceledWait) ChunkyIdleLog.info("A player joined. The pending resume wait was canceled.");
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> checkForEmptyServer = true);
        ServerTickEvents.END_SERVER_TICK.register(ChunkyIdleFabric::tick);
        ServerLifecycleEvents.SERVER_STOPPING.register(ChunkyIdleFabric::stopping);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> { automation = null; settings = null; currentServer = null; checkForEmptyServer = false; });
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, environment) -> dispatcher.register(
                Commands.literal("chunkyidle").requires(source ->
                        //? <1.21.11 {
                        source.hasPermission(2)
                        //?} else {
                        /*source.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)*/
                        //?}
                )
                        .then(Commands.literal("reload").executes(ctx -> reload(ctx.getSource())))
                        .then(Commands.literal("status").executes(ctx -> status(ctx.getSource())))
                        .then(Commands.literal("toggle").executes(ctx -> toggle(ctx.getSource())))));
    }

    private static void start(MinecraftServer server, boolean reloaded) {
        currentServer = server;
        checkForEmptyServer = false;
        try {
            settings = IdleAutomationConfigFile.load(CONFIG, LEGACY);
            if (!FabricLoader.getInstance().isModLoaded("chunky")) {
                automation = null;
                ChunkyIdleLog.info("Chunky was not found, so automatic generation is off.");
                return;
            }
            automation = new IdleAutomation(new FabricChunkyTaskController(server), settings);
            if (!reloaded && settings.enabled() && settings.restartEnabled()) {
                Set<String> worlds = settings.resumeScope() == IdleAutomationConfig.ResumeScope.ANY_SAVED
                        ? StreamSupport.stream(server.getAllLevels().spliterator(), false)
                            .map(level -> FabricChunkyTaskController.worldId(level.dimension())).collect(Collectors.toSet())
                        : RESUME_STATE.read();
                long delay = switch (settings.resumeWait().kind()) {
                    case IMMEDIATELY -> 0;
                    case IDLE_TIME -> settings.idleSeconds();
                    case DURATION -> settings.resumeWait().seconds();
                };
                if (!worlds.isEmpty()) {
                    automation.recoverOnStartup(worlds, delay);
                    if (delay == 0) ChunkyIdleLog.info("The server is ready. Saved Chunky generation will resume now.");
                    else ChunkyIdleLog.info("Saved Chunky generation will resume after the server has been empty for {}.", ChunkyIdleMessages.formatDuration(delay));
                }
                else RESUME_STATE.clear();
            }
            if (reloaded) ChunkyIdleLog.info("Settings reloaded.");
            else ChunkyIdleLog.info("Settings loaded. ChunkyIdle will manage generation when the server is empty.");
        } catch (Exception exception) {
            automation = null;
            ChunkyIdleLog.error("Settings could not be loaded. Automatic generation is off until config.yml is fixed and reloaded.", exception);
        }
    }

    private static void stopping(MinecraftServer server) {
        if (automation == null || settings == null || !settings.enabled() || !settings.restartEnabled()) {
            try { RESUME_STATE.clear(); } catch (IOException e) { ChunkyIdleLog.warn("Could not clear saved restart tasks.", e); }
            return;
        }
        try {
            RESUME_STATE.write(automation.worldsToSave());
        } catch (IOException e) { ChunkyIdleLog.warn("Saved tasks may not be available after restart.", e); }
    }

    private static int reload(net.minecraft.commands.CommandSourceStack source) {
        try {
            IdleAutomationConfig fresh = IdleAutomationConfigFile.load(CONFIG, LEGACY);
            if (currentServer != null && FabricLoader.getInstance().isModLoaded("chunky")) {
                if (automation == null) automation = new IdleAutomation(new FabricChunkyTaskController(currentServer), fresh);
                else automation.applyConfig(fresh);
                if (fresh.enabled() && !currentServer.getPlayerList().getPlayers().isEmpty()) automation.onPlayerJoined();
                else if (fresh.enabled()) automation.onServerBecameEmpty();
            }
            settings = fresh;
            if (ChunkyIdleMessages.isPlayerSource(source)) ChunkyIdleLog.info("Settings reloaded.");
            ChunkyIdleMessages.reply(source, ChunkyIdleMessages.Kind.SUCCESS,
                    "Settings reloaded.", "The new settings are now active.");
            return 1;
        } catch (Exception e) {
            ChunkyIdleMessages.reply(source, ChunkyIdleMessages.Kind.ERROR, "Could not reload settings.",
                    "The last working settings remain active. Check config.yml, fix the issue, and run /chunkyidle reload again.");
            ChunkyIdleLog.warn("Settings could not be reloaded. The current settings are still in use.", e);
            return 0;
        }
    }

    private static int toggle(net.minecraft.commands.CommandSourceStack source) {
        if (settings == null || automation == null) return fail(source, "ChunkyIdle is not ready.", "Check that Chunky is installed and the config has loaded.");
        boolean enabled = !settings.enabled();
        try {
            IdleAutomationConfigFile.setEnabledPreservingComments(CONFIG, enabled);
            settings = settings.withEnabled(enabled);
            automation.toggleEnabled(enabled);
            if (enabled && currentServer != null && !currentServer.getPlayerList().getPlayers().isEmpty()) automation.onPlayerJoined();
            else if (enabled) automation.onServerBecameEmpty();
            String title = enabled ? "Automation turned on." : "Automation turned off.";
            String detail;
            if (enabled) {
                detail = "Player activity will control Chunky generation.";
            } else if (!ChunkyIdleMessages.isPlayerSource(source) && automation.hasRunningTasks()) {
                detail = "Chunky is currently generating and will continue until paused manually. Players joining will not pause it while automation is off.";
            } else {
                detail = "Paused generation will stay paused.";
            }
            if (ChunkyIdleMessages.isPlayerSource(source)) ChunkyIdleLog.info(title + " " + detail);
            ChunkyIdleMessages.reply(source, ChunkyIdleMessages.Kind.SUCCESS, title, detail);
            return 1;
        } catch (IOException e) {
            ChunkyIdleLog.warn("Could not save the automation setting. It was not changed.", e);
            return fail(source, "Could not update config.yml.", "Automation was not changed. Check file permissions and try again.");
        }
    }

    private static int status(net.minecraft.commands.CommandSourceStack source) {
        ChunkyIdleMessages.status(source, settings, automation, FabricLoader.getInstance().isModLoaded("chunky"), currentServer);
        return 1;
    }

    private static int fail(net.minecraft.commands.CommandSourceStack source, String title, String detail) {
        ChunkyIdleMessages.reply(source, ChunkyIdleMessages.Kind.ERROR, title, detail);
        return 0;
    }

    private static void tick(MinecraftServer server) {
        if (automation == null) { checkForEmptyServer = false; return; }
        boolean hasPlayers = !server.getPlayerList().getPlayers().isEmpty();
        if (checkForEmptyServer) {
            checkForEmptyServer = false;
            if (!hasPlayers) {
                boolean pending = automation.isResumePending();
                automation.onServerBecameEmpty();
                if (!pending && automation.isResumePending()) {
                    ChunkyIdleLog.info("Server is empty. Chunky will resume in {}.", ChunkyIdleMessages.formatDuration(settings.idleSeconds()));
                }
            }
        }
        long completionCount = automation.resumeCompletionCount();
        automation.onServerTick(hasPlayers);
        if (automation.resumeCompletionCount() != completionCount) {
            long waited = automation.completedResumeDelaySeconds();
            if (waited > 0) ChunkyIdleLog.info("Server has been empty for {}. Resuming Chunky.", ChunkyIdleMessages.formatDuration(waited));
            else ChunkyIdleLog.info("The server is empty. Resuming saved Chunky generation.");
            try { RESUME_STATE.clear(); } catch (IOException e) { ChunkyIdleLog.warn("Could not clear completed restart tasks.", e); }
        }
    }
}
