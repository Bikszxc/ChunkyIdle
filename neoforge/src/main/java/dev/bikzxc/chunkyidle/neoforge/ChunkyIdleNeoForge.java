package dev.bikzxc.chunkyidle.neoforge;

import com.mojang.brigadier.CommandDispatcher;
import dev.bikzxc.chunkyidle.ChunkyIdle;
import dev.bikzxc.chunkyidle.ChunkyIdleLog;
import dev.bikzxc.chunkyidle.automation.IdleAutomation;
import dev.bikzxc.chunkyidle.automation.IdleAutomationConfig;
import dev.bikzxc.chunkyidle.automation.IdleAutomationConfigFile;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
//? >=1.21.1 {
import net.neoforged.neoforge.event.tick.ServerTickEvent;
//?} else {
/*import net.neoforged.neoforge.event.TickEvent;*/
//?}
import net.neoforged.bus.api.SubscribeEvent;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Mod(ChunkyIdle.MOD_ID)
public final class ChunkyIdleNeoForge {
    private static final Path DIRECTORY = FMLPaths.CONFIGDIR.get().resolve("chunkyidle");
    private static final Path CONFIG = DIRECTORY.resolve("config.yml");
    private static final Path LEGACY = FMLPaths.CONFIGDIR.get().resolve("chunkyidle.properties");
    private static final NeoForgeResumeStateStore RESUME_STATE = new NeoForgeResumeStateStore(DIRECTORY.resolve("resume-state.txt"));

    private IdleAutomation automation;
    private IdleAutomationConfig settings;
    private MinecraftServer currentServer;
    private boolean checkForEmptyServer;

    public ChunkyIdleNeoForge() {
        ChunkyIdle.init();
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("chunkyidle")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("reload").executes(context -> reload(context.getSource())))
                .then(Commands.literal("status").executes(context -> status(context.getSource())))
                .then(Commands.literal("toggle").executes(context -> toggle(context.getSource()))));
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        currentServer = event.getServer();
        checkForEmptyServer = false;
        try {
            settings = IdleAutomationConfigFile.load(CONFIG, LEGACY);
            if (!ModList.get().isLoaded("chunky")) {
                automation = null;
                ChunkyIdleLog.info("Chunky was not found, so automatic generation is off.");
                return;
            }
            automation = new IdleAutomation(new NeoForgeChunkyTaskController(currentServer), settings);
            if (settings.enabled() && settings.restartEnabled()) recoverSavedTasks(currentServer, settings, automation);
            ChunkyIdleLog.info("Settings loaded. ChunkyIdle will manage generation when the server is empty.");
        } catch (Exception exception) {
            automation = null;
            ChunkyIdleLog.error("Settings could not be loaded. Automatic generation is off until config.yml is fixed and reloaded.", exception);
        }
    }

    @SubscribeEvent
    public void onPlayerJoined(PlayerEvent.PlayerLoggedInEvent event) {
        if (automation == null || !(event.getEntity() instanceof ServerPlayer)) return;
        boolean canceledWait = automation.isResumePending();
        automation.onPlayerJoined();
        if (canceledWait) ChunkyIdleLog.info("A player joined. The pending resume wait was canceled.");
    }

    @SubscribeEvent
    public void onPlayerLeft(PlayerEvent.PlayerLoggedOutEvent event) {
        checkForEmptyServer = true;
    }

    //? >=1.21.1 {
    @SubscribeEvent public void onServerTick(ServerTickEvent.Post event) { tick(event.getServer()); }
    //?} else {
    /*@SubscribeEvent public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) tick(event.getServer());
    }*/
    //?}

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        if (automation == null || settings == null || !settings.enabled() || !settings.restartEnabled()) {
            try { RESUME_STATE.clear(); } catch (IOException e) { ChunkyIdleLog.warn("Could not clear saved restart tasks.", e); }
            return;
        }
        try {
            RESUME_STATE.write(automation.worldsToSave());
        } catch (IOException e) { ChunkyIdleLog.warn("Saved tasks may not be available after restart.", e); }
    }

    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event) {
        automation = null;
        settings = null;
        currentServer = null;
        checkForEmptyServer = false;
    }

    private void recoverSavedTasks(MinecraftServer server, IdleAutomationConfig config,
                                   IdleAutomation automation) throws IOException {
        Set<String> worlds = config.resumeScope() == IdleAutomationConfig.ResumeScope.ANY_SAVED
                ? StreamSupport.stream(server.getAllLevels().spliterator(), false)
                    .map(level -> NeoForgeChunkyTaskController.worldId(level.dimension())).collect(Collectors.toSet())
                : RESUME_STATE.read();
        if (worlds.isEmpty()) {
            RESUME_STATE.clear();
            return;
        }
        long delay = config.resumeDelaySeconds();
        automation.recoverOnStartup(worlds, delay);
        if (delay == 0) ChunkyIdleLog.info("The server is ready. Saved Chunky generation will resume now.");
        else ChunkyIdleLog.info("Saved Chunky generation will resume after the server has been empty for {}.", duration(delay));
    }

    private int reload(CommandSourceStack source) {
        try {
            IdleAutomationConfig fresh = IdleAutomationConfigFile.load(CONFIG, LEGACY);
            if (currentServer != null && ModList.get().isLoaded("chunky")) {
                if (automation == null) automation = new IdleAutomation(new NeoForgeChunkyTaskController(currentServer), fresh);
                else automation.applyConfig(fresh);
                if (fresh.enabled() && !currentServer.getPlayerList().getPlayers().isEmpty()) automation.onPlayerJoined();
                else if (fresh.enabled()) automation.onServerBecameEmpty();
            }
            settings = fresh;
            ChunkyIdleLog.info("Settings reloaded.");
            reply(source, ChatFormatting.GREEN, "Settings reloaded. The new settings are now active.");
            return 1;
        } catch (Exception exception) {
            reply(source, ChatFormatting.RED, "Could not reload settings. The last working settings remain active; check config.yml.");
            ChunkyIdleLog.warn("Settings could not be reloaded. The current settings are still in use.", exception);
            return 0;
        }
    }

    private int toggle(CommandSourceStack source) {
        if (settings == null || automation == null) return fail(source, "ChunkyIdle is not ready. Check that Chunky is installed and the config has loaded.");
        boolean enabled = !settings.enabled();
        try {
            IdleAutomationConfigFile.setEnabledPreservingComments(CONFIG, enabled);
            settings = settings.withEnabled(enabled);
            automation.toggleEnabled(enabled);
            if (enabled && currentServer != null && !currentServer.getPlayerList().getPlayers().isEmpty()) automation.onPlayerJoined();
            else if (enabled) automation.onServerBecameEmpty();
            String message = enabled ? "Automation turned on. Player activity will control Chunky generation."
                    : "Automation turned off. Paused generation will stay paused.";
            ChunkyIdleLog.info(message);
            reply(source, ChatFormatting.GREEN, message);
            return 1;
        } catch (IOException exception) {
            ChunkyIdleLog.warn("Could not save the automation setting. It was not changed.", exception);
            return fail(source, "Could not update config.yml. Automation was not changed.");
        }
    }

    private int status(CommandSourceStack source) {
        boolean player = source.getEntity() instanceof ServerPlayer;
        String automationStatus = settings == null ? "Unavailable" : !ModList.get().isLoaded("chunky") ? "Chunky not found" : settings.enabled() ? "Enabled" : "Disabled";
        String wait = settings == null ? "Unavailable" : duration(settings.idleSeconds());
        String paused = automation == null ? "Unavailable" : Integer.toString(automation.pausedWorlds().size());
        String restart = settings == null ? "Unavailable" : settings.restartEnabled() && settings.enabled() ? "Enabled" : "Disabled";
        String message = "ChunkyIdle v" + ChunkyIdle.VERSION + " | Automation: " + automationStatus
                + " | Empty wait: " + wait + " | Paused tasks: " + paused + " | Restart recovery: " + restart;
        if (player) source.sendSuccess(() -> Component.literal(message).withStyle(ChatFormatting.AQUA), false);
        else ChunkyIdleLog.info(message);
        return 1;
    }

    private int fail(CommandSourceStack source, String message) {
        reply(source, ChatFormatting.RED, message);
        return 0;
    }

    private void reply(CommandSourceStack source, ChatFormatting color, String message) {
        if (source.getEntity() instanceof ServerPlayer) source.sendSuccess(() -> Component.literal("[ChunkyIdle] " + message).withStyle(color), false);
        else ChunkyIdleLog.info(message);
    }

    private void tick(MinecraftServer server) {
        if (automation == null) { checkForEmptyServer = false; return; }
        boolean hasPlayers = !server.getPlayerList().getPlayers().isEmpty();
        if (checkForEmptyServer) {
            checkForEmptyServer = false;
            if (!hasPlayers) {
                boolean pending = automation.isResumePending();
                automation.onServerBecameEmpty();
                if (!pending && automation.isResumePending()) ChunkyIdleLog.info("Server is empty. Chunky will resume in {}.", duration(settings.idleSeconds()));
            }
        }
        long completionCount = automation.resumeCompletionCount();
        automation.onServerTick(hasPlayers);
        if (automation.resumeCompletionCount() != completionCount) {
            long waited = automation.completedResumeDelaySeconds();
            if (waited > 0) ChunkyIdleLog.info("Server has been empty for {}. Resuming Chunky.", duration(waited));
            else ChunkyIdleLog.info("The server is empty. Resuming saved Chunky generation.");
            try { RESUME_STATE.clear(); } catch (IOException e) { ChunkyIdleLog.warn("Could not clear completed restart tasks.", e); }
        }
    }

    private static String duration(long seconds) {
        if (seconds >= 3600 && seconds % 3600 == 0) return unit(seconds / 3600, "hour");
        if (seconds >= 60 && seconds % 60 == 0) return unit(seconds / 60, "minute");
        return unit(seconds, "second");
    }

    private static String unit(long count, String name) { return count + " " + name + (count == 1 ? "" : "s"); }
}
