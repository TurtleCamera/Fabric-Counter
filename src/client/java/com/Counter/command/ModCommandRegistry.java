package com.Counter.command;

import java.util.ArrayList;

public class ModCommandRegistry {
    public ArrayList<ModCommand> COMMANDS;

    public ModCommandRegistry() {
        // Initialize commands
        COMMANDS = new ArrayList<>();

        // Create commands
        // .track
        ModCommand track = new ModCommand("track", ModCommand.ArgType.LITERAL)
                .then(new ModCommand("emote", ModCommand.ArgType.STRING));
        // .untrack
        ModCommand untrack = new ModCommand("untrack", ModCommand.ArgType.LITERAL)
                .then(new ModCommand("emote", ModCommand.ArgType.STRING));
        // dummy example
        ModCommand sethome = new ModCommand("sethome", ModCommand.ArgType.LITERAL)
                .then(new ModCommand("name", ModCommand.ArgType.STRING)
                        .then(new ModCommand("x", ModCommand.ArgType.INTEGER)
                            .then(new ModCommand("y", ModCommand.ArgType.INTEGER)
                                .then(new ModCommand("z", ModCommand.ArgType.INTEGER)))));

        // Add all commands
        COMMANDS.add(track);
        COMMANDS.add(untrack);
        COMMANDS.add(sethome);
    }

    // Given an argument, return an index in modCommands if it matches any of the command names
    public static int findMatchIndex(ArrayList<ModCommand> modCommands, String argument) {
        // Check all commands to see if the argument matches any of their conditions
        for (int i  = 0; i < modCommands.size(); i++) {
            ModCommand currentCommand = modCommands.get(i);
            System.out.println(currentCommand.name);
            if (currentCommand.type == ModCommand.ArgType.STRING || currentCommand.type == ModCommand.ArgType.INTEGER || currentCommand.type == ModCommand.ArgType.DOUBLE) {
                // Always match (if it's one of these types, it should be the only child of the parent node)
                // TODO: Perhaps handle the logic of checking the correct type if needed
                return i;
            }
            else if (currentCommand.type == ModCommand.ArgType.GREEDY) {
                // Always match (if it's one of these types, it should be the only child of the parent node)
                return i;
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
}
