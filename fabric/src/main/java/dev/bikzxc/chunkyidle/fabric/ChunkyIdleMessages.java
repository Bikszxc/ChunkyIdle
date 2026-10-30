package dev.bikzxc.chunkyidle.fabric;

import dev.bikzxc.chunkyidle.ChunkyIdle;
import dev.bikzxc.chunkyidle.ChunkyIdleLog;
import dev.bikzxc.chunkyidle.automation.IdleAutomation;
import dev.bikzxc.chunkyidle.automation.IdleAutomationConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

/** Shared presentation for operator replies and the status dashboard. */
final class ChunkyIdleMessages {
    private static final String SEPARATOR = "------------------------------------------------";

    private ChunkyIdleMessages() { }

    enum Kind { SUCCESS, WARNING, ERROR }

    static void reply(CommandSourceStack source, Kind kind, String title, String detail) {
        if (!isPlayerSource(source)) {
            String consoleMessage = title + " " + detail;
            switch (kind) {
                case SUCCESS -> ChunkyIdleLog.info(consoleMessage);
                case WARNING -> ChunkyIdleLog.warn(consoleMessage);
                case ERROR -> ChunkyIdleLog.error(consoleMessage);
            }
            return;
        }
        ChatFormatting color = switch (kind) {
            case SUCCESS -> ChatFormatting.GREEN;
            case WARNING -> ChatFormatting.GOLD;
            case ERROR -> ChatFormatting.RED;
        };
        Component message = Component.literal("[ChunkyIdle] ").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)
                .append(Component.literal(title).withStyle(color, ChatFormatting.BOLD))
                .append(Component.literal("\n  " + detail).withStyle(ChatFormatting.GRAY));
        if (kind == Kind.ERROR) source.sendFailure(message);
        else source.sendSuccess(() -> message, false);
    }

    static void status(CommandSourceStack source, IdleAutomationConfig settings,
                       IdleAutomation automation, boolean chunkyLoaded, MinecraftServer server) {
        MutableComponent output = Component.empty()
                .append(Component.literal("------------ [ ChunkyIdle v" + ChunkyIdle.VERSION + " ] ------------")
                        .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .append(Component.literal("\n\n  AUTOMATION").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        String automationState = automationState(settings, chunkyLoaded, true);
        ChatFormatting automationColor = settings == null || !chunkyLoaded || !settings.enabled()
                ? ChatFormatting.RED : ChatFormatting.GREEN;
        output = row(output, "Status", automationState, "Whether player activity currently controls Chunky generation.", automationColor);
        output = row(output, "Empty Server Wait", settings == null ? "Unavailable" : formatDuration(settings.idleSeconds()),
                "How long the server must remain empty before generation resumes.", ChatFormatting.WHITE);
        output = row(output, "Paused Tasks", automation == null ? "Unavailable" : Integer.toString(automation.pausedWorlds().size()),
                "Number of generation tasks ChunkyIdle is currently tracking for resume.", ChatFormatting.WHITE);
        String timer = resumeTimer(automation);
        output = row(output, "Resume Timer", timer,
                "Time remaining before ChunkyIdle attempts to continue eligible saved generation.", ChatFormatting.WHITE);

        output = output.append(Component.literal("\n\n  SERVER RESTART").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        String restartState = restartState(settings, true);
        ChatFormatting restartColor = settings == null || !settings.restartEnabled() || !settings.enabled()
                ? ChatFormatting.RED : ChatFormatting.GREEN;
        output = row(output, "Status", restartState,
                "Whether saved generation tasks may resume after a restart. Restart recovery also requires automation to be enabled.", restartColor);
        String policy = resumePolicy(settings, true);
        String policyHelp = "Determines which saved generation tasks are eligible for automatic resumption. Any Saved Tasks may resume tasks an administrator paused manually.";
        output = row(output, "Resume Policy", policy, policyHelp, ChatFormatting.WHITE);
        String delay = resumeDelay(settings, true);
        output = row(output, "Resume Delay", delay,
                "Determines when eligible tasks resume after server startup.", ChatFormatting.WHITE);
        output = output.append(Component.literal("\n\n" + SEPARATOR).withStyle(ChatFormatting.DARK_GRAY));

        if (settings == null) {
            output = output.append(Component.literal("\nSettings could not be loaded. Fix config.yml, then run /chunkyidle reload.")
                    .withStyle(ChatFormatting.YELLOW));
        } else if (!chunkyLoaded) {
            output = output.append(Component.literal("\nInstall Chunky to enable generation automation.").withStyle(ChatFormatting.YELLOW));
        } else if (!settings.enabled()) {
            output = output.append(Component.literal("\nAutomation is off; paused generation will stay paused.")
                    .withStyle(ChatFormatting.GRAY));
        } else if (server != null && !server.getPlayerList().getPlayers().isEmpty()) {
            output = output.append(Component.literal("\nPlayers are online; ChunkyIdle will keep eligible tasks paused.")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            output = output.append(Component.literal("\nThe server is empty.").withStyle(ChatFormatting.GRAY));
        }
        if (!isPlayerSource(source)) {
            sendConsoleStatus(settings, automation, chunkyLoaded, server);
            return;
        }
        MutableComponent dashboard = output;
        source.sendSuccess(() -> dashboard, false);
    }

    private static void sendConsoleStatus(IdleAutomationConfig settings, IdleAutomation automation,
                                         boolean chunkyLoaded, MinecraftServer server) {
        ChunkyIdleLog.info("Status | v{}", ChunkyIdle.VERSION);
        ChunkyIdleLog.info("Automation: " + automationState(settings, chunkyLoaded, false));
        ChunkyIdleLog.info("  Empty server wait: " + (settings == null ? "Unavailable" : formatDuration(settings.idleSeconds())));
        ChunkyIdleLog.info("  Paused tasks: " + (automation == null ? "Unavailable" : automation.pausedWorlds().size()));
        ChunkyIdleLog.info("  Resume timer: " + resumeTimer(automation));

        ChunkyIdleLog.info("Server restart: " + restartState(settings, false));
        ChunkyIdleLog.info("  Resume policy: " + resumePolicy(settings, false));
        ChunkyIdleLog.info("  Resume delay: " + resumeDelay(settings, false));

        if (settings == null) ChunkyIdleLog.info("Fix config.yml, then run /chunkyidle reload.");
        else if (!chunkyLoaded) ChunkyIdleLog.info("Install Chunky to enable generation automation.");
        else if (!settings.enabled()) ChunkyIdleLog.info("Automation is off; paused generation will stay paused.");
        else if (server != null && !server.getPlayerList().getPlayers().isEmpty()) ChunkyIdleLog.info("Players are online.");
        else ChunkyIdleLog.info("The server is empty.");
    }

    static String formatDuration(long seconds) {
        if (seconds >= 3600 && seconds % 3600 == 0) return unit(seconds / 3600, "hour");
        if (seconds >= 60 && seconds % 60 == 0) return unit(seconds / 60, "minute");
        return unit(seconds, "second");
    }

    private static String automationState(IdleAutomationConfig settings, boolean chunkyLoaded, boolean styled) {
        if (settings == null) return "Unavailable";
        if (!chunkyLoaded) return styled ? "Chunky Not Found" : "Chunky not found";
        if (settings.enabled()) return styled ? "● Enabled" : "Enabled";
        return styled ? "● Disabled" : "Disabled";
    }

    private static String restartState(IdleAutomationConfig settings, boolean styled) {
        if (settings == null) return "Unavailable";
        if (!settings.restartEnabled()) return styled ? "● Disabled" : "Disabled";
        if (settings.enabled()) return styled ? "● Enabled" : "Enabled";
        return styled ? "● Disabled (automation off)" : "Disabled (automation is off)";
    }

    private static String resumePolicy(IdleAutomationConfig settings, boolean styled) {
        if (settings == null) return "Unavailable";
        return switch (settings.resumeScope()) {
            case ACTIVE_BEFORE_SHUTDOWN -> styled ? "Active Before Shutdown" : "Active before shutdown";
            case ANY_SAVED -> styled ? "Any Saved Tasks" : "Any saved tasks (may resume tasks paused manually)";
        };
    }

    private static String resumeDelay(IdleAutomationConfig settings, boolean styled) {
        if (settings == null) return "Unavailable";
        return switch (settings.resumeWait().kind()) {
            case IDLE_TIME -> (styled ? "Idle Time (" : "Idle time (") + formatDuration(settings.idleSeconds()) + ")";
            case IMMEDIATELY -> "Immediately";
            case DURATION -> formatDuration(settings.resumeWait().seconds());
        };
    }

    private static String resumeTimer(IdleAutomation automation) {
        if (automation == null || !automation.isResumePending()) return "Not running";
        return formatDuration((automation.ticksUntilResume() + 19) / 20) + " remaining";
    }

    private static String unit(long count, String name) { return count + " " + name + (count == 1 ? "" : "s"); }

    private static MutableComponent row(MutableComponent output, String label, String value, String description, ChatFormatting valueColor) {
        return output.append(Component.literal("\n  "))
                .append(hovered(label, ChatFormatting.GRAY, description))
                .append(Component.literal("   "))
                .append(hovered(value, valueColor, description));
    }

    private static MutableComponent hovered(String text, ChatFormatting color, String description) {
        return Component.literal(text).withStyle(style -> style.withColor(color)
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(description))));
    }

    static boolean isPlayerSource(CommandSourceStack source) {
        return source.getEntity() instanceof ServerPlayer;
    }
}
