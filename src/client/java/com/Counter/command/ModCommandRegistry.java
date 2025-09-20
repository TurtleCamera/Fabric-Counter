package com.Counter.command;

import com.Counter.CounterMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;

public class ModCommandRegistry {
    public ArrayList<ModCommand> COMMANDS;

    public ModCommandRegistry() {
        // Initialize commands
        COMMANDS = new ArrayList<>();

        // .track
        ModCommand track = new ModCommand(".track", ModCommand.ArgType.LITERAL)
                .then(new ModCommand("<phrase>", ModCommand.ArgType.STRING)
                        .executes(context -> {
                            // Try adding the phrase
                            String phrase = context.getString("<phrase>");
                            boolean isAdded = addIgnoreCase(CounterMod.configManager.getConfig().phrases, phrase);

                            // An instance of the player, so we can send messages to them
                            ClientPlayerEntity player =  MinecraftClient.getInstance().player;

                            // Different messages depending on whether we're already tracking this phrase
                            if (isAdded) {
                                // Phrase was added
                                MutableText message = Text.literal("Now tracking the phrase \"").styled(style -> style.withColor(Formatting.GREEN))
                                        .append(Text.literal(phrase).styled(style -> style.withColor(Formatting.AQUA)))
                                        .append(Text.literal("\".").styled(style -> style.withColor(Formatting.GREEN)));
                                player.sendMessage(message, false);

                                // Save to configuration
                                CounterMod.saveConfig();
                            }
                            else {
                                // Phrase already tracked
                                MutableText message = Text.literal("Already tracking the phrase \"").styled(style -> style.withColor(Formatting.GREEN))
                                        .append(Text.literal(phrase).styled(style -> style.withColor(Formatting.AQUA)))
                                        .append(Text.literal("\".").styled(style -> style.withColor(Formatting.GREEN)));
                                player.sendMessage(message, false);
                            }
                        }));

        // .untrack
        ModCommand untrack = new ModCommand(".untrack", ModCommand.ArgType.LITERAL)
                .then(new ModCommand("<phrase>", ModCommand.ArgType.STRING)
                        .executes(context -> {
                            // Get the phrase and try removing it
                            String phrase = context.getString("<phrase>");
                            int removedIndex = removeIgnoreCase(CounterMod.configManager.getConfig().phrases, phrase);

                            // An instance of the player, so we can send messages to them
                            ClientPlayerEntity player =  MinecraftClient.getInstance().player;

                            // Different messages depending on whether we're already tracking this phrase
                            if (removedIndex != -1) {
                                // Phrase was removed
                                MutableText message = Text.literal("No longer tracking the phrase \"").styled(style -> style.withColor(Formatting.GREEN))
                                        .append(Text.literal(phrase).styled(style -> style.withColor(Formatting.AQUA)))
                                        .append(Text.literal("\".").styled(style -> style.withColor(Formatting.GREEN)));
                                player.sendMessage(message, false);

                                // Save to configuration
                                CounterMod.saveConfig();
                            }
                            else {
                                // Phrase didn't exist
                                MutableText message = Text.literal("The phrase \"").styled(style -> style.withColor(Formatting.RED))
                                        .append(Text.literal(phrase).styled(style -> style.withColor(Formatting.AQUA)))
                                        .append(Text.literal("\" was not being tracked.").styled(style -> style.withColor(Formatting.RED)));
                                player.sendMessage(message, false);
                            }
                        }));

        // .list
        ModCommand list = new ModCommand(".list", ModCommand.ArgType.LITERAL)
                .executes(context -> {
                    // An instance of the player, so we can send messages to them
                    ClientPlayerEntity player =  MinecraftClient.getInstance().player;

                    // If there are no tracked phrases, print a different message
                    if (CounterMod.configManager.getConfig().phrases.isEmpty()) {
                        MutableText message = Text.literal("There are no tracked phrases.").styled(style -> style.withColor(Formatting.GREEN));
                        player.sendMessage(message, false);
                    }
                    else {
                        // List all tracked phrases
                        player.sendMessage(Text.literal("Listing all tracked phrases: ").styled(style -> style.withColor(Formatting.GREEN)), false);
                        for (String phrase : CounterMod.configManager.getConfig().phrases) {
                            MutableText message = Text.literal("- ").styled(style -> style.withColor(Formatting.GREEN))
                                    .append(Text.literal(phrase).styled(style -> style.withColor(Formatting.AQUA)));
                            player.sendMessage(message, false);
                        }
                    }
                });

        // .autocorrect
        ModCommand autocorrect = new ModCommand(".autocorrect", ModCommand.ArgType.LITERAL)
                .executes(context -> {
                    // An instance of the player, so we can send messages to them
                    ClientPlayerEntity player = MinecraftClient.getInstance().player;

                    // Running this command without specifying an argument should tell the player
                    // if autocorrect is enabled or disabled.
                    if (CounterMod.configManager.getConfig().enableAutocorrect) {
                        // An instance of the player, so we can send messages to them
                        MutableText message = Text.literal("Autocorrect is currently enabled.").styled(style -> style.withColor(Formatting.GREEN));
                        player.sendMessage(message, false);
                    }
                    else {
                        MutableText message = Text.literal("Autocorrect is currently disabled.").styled(style -> style.withColor(Formatting.RED));
                        player.sendMessage(message, false);
                    }
                })
                .then(new ModCommand("enable", ModCommand.ArgType.LITERAL)
                        .executes(context -> {
                            // An instance of the player, so we can send messages to them
                            ClientPlayerEntity player = MinecraftClient.getInstance().player;

                            if (!CounterMod.configManager.getConfig().enableAutocorrect) {
                                // Enable autocorrect
                                CounterMod.configManager.getConfig().enableAutocorrect = true;

                                // An instance of the player, so we can send messages to them
                                MutableText message = Text.literal("Autocorrect is now enabled.").styled(style -> style.withColor(Formatting.GREEN));
                                player.sendMessage(message, false);

                                // Save to config
                                CounterMod.saveConfig();
                            }
                            else {
                                MutableText message = Text.literal("Autocorrect is already enabled.").styled(style -> style.withColor(Formatting.GREEN));
                                player.sendMessage(message, false);
                            }
                        }))
                .then(new ModCommand("disable", ModCommand.ArgType.LITERAL)
                        .executes(context -> {
                            // An instance of the player, so we can send messages to them
                            ClientPlayerEntity player = MinecraftClient.getInstance().player;

                            if (CounterMod.configManager.getConfig().enableAutocorrect) {
                                // Disable autocorrect
                                CounterMod.configManager.getConfig().enableAutocorrect = false;

                                MutableText message = Text.literal("Autocorrect is now disabled.").styled(style -> style.withColor(Formatting.RED));
                                player.sendMessage(message, false);

                                // Save to config
                                CounterMod.saveConfig();
                            }
                            else {
                                MutableText message = Text.literal("Autocorrect is already disabled.").styled(style -> style.withColor(Formatting.RED));
                                player.sendMessage(message, false);
                            }
                        }));

        // Register all commands
        register(track);
        register(untrack);
        register(list);
        register(autocorrect);
    }

    // Helper function to add a phrase. It returns true if it successfully added the phrase
    // and false if a copy of this phrase (ignore case) already exists.
    private boolean addIgnoreCase(ArrayList<String> phrases, String phrase) {
        // Don't add null phrases
        if (phrase == null) {
            return false;
        }

        // Does this phrase (ignore case) already exist in the list?
        for (String current : phrases) {
            if (current != null && current.equalsIgnoreCase(phrase)) {
                return false;
            }
        }

        // Can add the phrase to the list
        phrases.add(phrase);
        return true;
    }

    // Helper function to remove a phrase (ignore case)
    private int removeIgnoreCase(ArrayList<String> phrases, String phrase) {
        // Can't match null phrases
        if (phrase == null) {
            return -1;
        }

        // Find if the phrase (ignore cases) exists already
        for (int i = 0; i < phrases.size(); i++) {
            String current = phrases.get(i);
            if (current != null && current.equalsIgnoreCase(phrase)) {
                phrases.remove(i);
                return i;
            }
        }

        // Not found
        return -1;
    }

    // Registers the command
    private void register(ModCommand command) {
        // Make sure everything is valid before registering the command
        validateRoot(command);
        validateLeaves(command);
        COMMANDS.add(command);
    }

    // Checks if the root of the mod command is valid
    private void validateRoot(ModCommand root) {
        // Error checks for the root
        if (root.type != ModCommand.ArgType.LITERAL) {
            throw new IllegalArgumentException("Root commands must be LITERAL: " +
                    root.name + ".");
        }

        if (!root.name.startsWith(".")) {
            throw new IllegalArgumentException("Root commands must start with '.': " +
                    root.name + ".");
        }
    }

    // Checks if all leaf nodes have actions
    private void validateLeaves(ModCommand node) {
        // If this is a leaf node, check if it has an action
        if (node.children.isEmpty()) {
            if (node.action == null) {
                throw new IllegalArgumentException("Leaf nodes must have an action: " +
                        node.name + ".");
            }

            return;
        }

        // Loop through all the children
        for (ModCommand child : node.children) {
            validateLeaves(child);
        }
    }

    // Given an argument, return an index in modCommands if it matches any of the command names
    public static int findMatchIndex(ArrayList<ModCommand> modCommands, String argument) {
        // Check all commands to see if the argument matches any of their conditions
        for (int i  = 0; i < modCommands.size(); i++) {
            ModCommand currentCommand = modCommands.get(i);
            if (currentCommand.type == ModCommand.ArgType.STRING || currentCommand.type == ModCommand.ArgType.INTEGER || currentCommand.type == ModCommand.ArgType.DOUBLE) {
                // Always match (if it's one of these types, it should be the only child of the parent node)
                // TODO: Perhaps handle the logic of checking the correct type if needed
                return i;
            }

            if (currentCommand.type == ModCommand.ArgType.GREEDY) {
                // Greedy arguments consume the rest of the input, so stop here
                return -1;
            }

            if (currentCommand.type == ModCommand.ArgType.LITERAL && currentCommand.name.equals(argument)) {
                // Matches the literal
                return i;
            }
        }

        // Not found or the list was empty
        return -1;
    }

    // Given a list of mod commands, generate an ArrayList of command names as suggestions
    public static ArrayList<String> createSuggestions(ArrayList<ModCommand> modCommands, String prefix) {
        ArrayList<String> suggestions = new ArrayList<>();

        // Handle null modCommands
        if (modCommands == null) {
            return suggestions;
        }

        // Normalize prefix (treat null as "")
        if (prefix == null) {
            prefix = "";
        }

        for (ModCommand node : modCommands) {
            if (node != null && node.name != null) {
                if (node.name.startsWith(prefix)) {
                    suggestions.add(node.name);
                }
            }
        }

        return suggestions;
    }

    // This function assumes that the list passed in contains only literal ModCommand nodes.
    // It will return an array of strings consisting of the literal names
    public static String[] generateLiteralList(ArrayList<ModCommand> modCommands) {
        // Array containing the literal names
        String[] names =  new String[modCommands.size()];

        // Loop through all the ModCommand nodes
        for (int i = 0; i < modCommands.size(); i++) {
            // Get the current node
            ModCommand node = modCommands.get(i);

            // This shouldn't happen, but print an error message to the player if we,
            // for some reason, passed in a list of ModCommands that aren't literals.
            if (!node.type.equals(ModCommand.ArgType.LITERAL)) {
                ClientPlayerEntity player =  MinecraftClient.getInstance().player;
                MutableText message = Text.literal("An error occurred in the searchLiterals() " +
                        "function because a list of ModCommands with non-literal argument " +
                        "types was passed in. Please contact TurtleCamera about this issue.")
                        .styled(style -> style.withColor(Formatting.RED));
                player.sendMessage(message, false);

                return null;
            }

            names[i] = node.name;
        }

        return names;
    }
}
