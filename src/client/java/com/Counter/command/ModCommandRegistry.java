package com.Counter.command;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ModCommandRegistry {
    public ArrayList<ModCommand> COMMANDS;

    public ModCommandRegistry() {
        // Initialize commands
        COMMANDS = new ArrayList<>();

//        // Create commands
//        // .track
//        ModCommand track = new ModCommand(".track", ModCommand.ArgType.LITERAL)
//                .then(new ModCommand("<emote>", ModCommand.ArgType.STRING));
//        // .untrack
//        ModCommand untrack = new ModCommand(".untrack", ModCommand.ArgType.LITERAL)
//                .then(new ModCommand("<emote>", ModCommand.ArgType.STRING)
//                .executes(context -> {
//                    String emote = context.getString("emote");
//                    for (int i = 0; i < 1000; i ++) {
//                        System.out.println(emote);
//                    }
//                }));
//
//        // Add all commands
//        register(track);
//        register(untrack);

        // Test example
        ModCommand test = new ModCommand(".test", ModCommand.ArgType.LITERAL)
                .then(new ModCommand("<string>", ModCommand.ArgType.STRING)
                        .then(new ModCommand("<integer>", ModCommand.ArgType.INTEGER)
                        .executes(context -> {
                            String string = context.getString("<string>");
                            String integer = context.getString("<integer>");

                            ClientPlayerEntity player =  MinecraftClient.getInstance().player;
                            MutableText message = Text.literal("Success! " + string + " " + integer);
                            player.sendMessage(message, false);
                        })));
        register(test);
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
        else {
            // If this is not a leaf node, then it shouldn't have an action
            if (node.action != null) {
                throw new IllegalArgumentException("Internal nodes cannot have an action: " +
                        node.name + ".");
            }
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
    public static ArrayList<String> createSuggestions(ArrayList<ModCommand> modCommands) {
        ArrayList<String> suggestions = new ArrayList<>();
        for (ModCommand node : modCommands) {
            suggestions.add(node.name);
        }

        return suggestions;
    }

    // This function assumes that the list passed in contains only literal ModCommand nodes.
    // It will return an array of strings consisting of the literal names
    public static String[] generateLiteralList(ArrayList<ModCommand> modCommands) {
        // Array containing the literal names
        String[] names =  new String[modCommands.size()];

        // Loop through all the ModCommand nodes
        for (int i = 0; i <= modCommands.size(); i++) {
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
