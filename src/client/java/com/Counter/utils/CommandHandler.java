package com.Counter.utils;

import com.Counter.CounterMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// Contains various helper functions for handling client-side commands
public class CommandHandler {
    // Use a period as the delimiter because a lot of other mods use "#" instead.
    private static char DELIMITER = '.';

    // Checks if the player's message is a command
    public static boolean isCommand(String content) {
        return content.charAt(0) == DELIMITER;
    }

    // Parses the command (assumes this is a command that starts with the delimiter).
    public static void parseCommand(String content) {
        // Strip the delimiter from the beginning of the string
        content = content.substring(1);

        // Parse the command
        String[] arguments = content.split(" ");

        // Get the player to send messages to
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        // There must be at least one argument (should always be the case)
        if (arguments.length == 0) {
            invalidCommand(player);
            return;
        }

        // Cases for different commands
        if (arguments[0].equals("track")) {
            trackCommand(arguments, player);
        }
        else if (arguments[0].equals("untrack")) {
            untrackCommand(arguments, player);
        }
        else {
            // Everything else is invalid
            invalidCommand(player);
        }
    }

    // Handles invalid commands
    private static void invalidCommand(ClientPlayerEntity player) {
        // Message the player telling them of the invalid command
        MutableText message = Text.literal("Invalid command. Type .help for a list of commands.").styled(style -> style.withColor(Formatting.RED));
        player.sendMessage(message, false);
    }

    // Handles the .track command
    private static void trackCommand(String[] arguments, ClientPlayerEntity player) {
        // The emote should only be one word
        if (arguments.length != 2) {
            MutableText message = Text.literal("There are too many arguments. The \"").styled(style -> style.withColor(Formatting.RED))
                    .append(Text.literal(".track").styled(style -> style.withColor(Formatting.AQUA)))
                    .append(Text.literal(".\" command only takes one argument.").styled(style -> style.withColor(Formatting.RED)));
            player.sendMessage(message, false);

            return;
        }

        // Track the emote
        String emote = arguments[1];
        CounterMod.configManager.getConfig().emotes.add(emote);

        // Emote did exist
        MutableText message = Text.literal("Now tracking\"").styled(style -> style.withColor(Formatting.GREEN))
                .append(Text.literal(emote).styled(style -> style.withColor(Formatting.AQUA)))
                .append(Text.literal(".\"").styled(style -> style.withColor(Formatting.GREEN)));
        player.sendMessage(message, false);

        // Save to configuration
        CounterMod.saveConfig();
    }

    // Handles the .untrack command
    private static void untrackCommand(String[] arguments, ClientPlayerEntity player) {
        // The emote should only be one word
        if (arguments.length != 2) {
            MutableText message = Text.literal("There are too many arguments. The \"").styled(style -> style.withColor(Formatting.RED))
                    .append(Text.literal(".track").styled(style -> style.withColor(Formatting.AQUA)))
                    .append(Text.literal(".\" command only takes one argument.").styled(style -> style.withColor(Formatting.RED)));
            player.sendMessage(message, false);

            return;
        }

        // Try to remove the emote
        String emote = arguments[1];
        CounterMod.configManager.getConfig().emotes.add(emote);

        // Different cases depending on whether the emote is already being tracked.
        if(CounterMod.configManager.getConfig().emotes.remove(emote)) {
            // Emote did exist
            MutableText message = Text.literal("No longer tracking\"").styled(style -> style.withColor(Formatting.GREEN))
                    .append(Text.literal(emote).styled(style -> style.withColor(Formatting.AQUA)))
                    .append(Text.literal(".\"").styled(style -> style.withColor(Formatting.GREEN)));
            player.sendMessage(message, false);

            // Save to configuration
            CounterMod.saveConfig();
        }
        else {
            // Emote didn't exist
            MutableText message = Text.literal("\"").styled(style -> style.withColor(Formatting.RED))
                    .append(Text.literal(emote).styled(style -> style.withColor(Formatting.AQUA)))
                    .append(Text.literal("\" wasn't being tracked.").styled(style -> style.withColor(Formatting.RED)));
            player.sendMessage(message, false);
        }
    }
}