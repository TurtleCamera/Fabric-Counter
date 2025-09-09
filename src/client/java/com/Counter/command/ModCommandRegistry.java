package com.Counter.command;

import java.util.ArrayList;

public class ModCommandRegistry {
    public ArrayList<ModCommand> COMMANDS;

    public ModCommandRegistry() {
        // Initialize commands
        COMMANDS = new ArrayList<>();

        // Create commands
        // .track
        ModCommand track = new ModCommand(".track", ModCommand.ArgType.LITERAL)
                .then(new ModCommand("<emote>", ModCommand.ArgType.STRING));
        // .untrack
        ModCommand untrack = new ModCommand(".untrack", ModCommand.ArgType.LITERAL)
                .then(new ModCommand("<emote>", ModCommand.ArgType.STRING));
        // dummy example 1
        ModCommand sethome = new ModCommand(".sethome", ModCommand.ArgType.LITERAL)
                .then(new ModCommand("name", ModCommand.ArgType.STRING)
                        .then(new ModCommand("<x>", ModCommand.ArgType.INTEGER)
                            .then(new ModCommand("<y>", ModCommand.ArgType.INTEGER)
                                .then(new ModCommand("<z>", ModCommand.ArgType.INTEGER)))));
        // dummy example 2
        ModCommand greedy = new ModCommand(".greedy", ModCommand.ArgType.LITERAL)
                .then(new ModCommand("test", ModCommand.ArgType.STRING)
                        .then(new ModCommand("consume", ModCommand.ArgType.GREEDY)));

        // Invalid example 1
//        ModCommand invalid_1 = new ModCommand("greedy", ModCommand.ArgType.LITERAL)
//                .then(new ModCommand("test", ModCommand.ArgType.STRING)
//                        .then(new ModCommand("consume", ModCommand.ArgType.GREEDY)));
//        // Invalid example 2
//        ModCommand invalid_1 = new ModCommand(".greedy", ModCommand.ArgType.LITERAL)
//                .then(new ModCommand("test", ModCommand.ArgType.STRING)
//                        .then(new ModCommand("consume", ModCommand.ArgType.GREEDY))
//                            .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL)));
//        // Invalid example 3
//        ModCommand invalid_1 = new ModCommand(".greedy", ModCommand.ArgType.LITERAL)
//                .then(new ModCommand("test", ModCommand.ArgType.STRING)
//                        .then(new ModCommand("consume", ModCommand.ArgType.GREEDY)
//                                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))));
//        // Invalid example 4
//        ModCommand invalid_1 = new ModCommand(".greedy", ModCommand.ArgType.LITERAL)
//                .then(new ModCommand("test", ModCommand.ArgType.STRING))
//                .then(new ModCommand("consume", ModCommand.ArgType.GREEDY))
//                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL));
        // Valid example
        ModCommand valid = new ModCommand(".greedy", ModCommand.ArgType.LITERAL)
                .then(new ModCommand("test", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("consume", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL))
                .then(new ModCommand("<x>", ModCommand.ArgType.LITERAL));

        // Add all commands
        register(track);
        register(untrack);
        register(sethome);
        register(greedy);
        register(valid);
    }

    // Registers the command
    private void register(ModCommand command) {
        // Make sure everything is valid before registering the command
        validateRoot(command);
        COMMANDS.add(command);
    }

    // Checks if the root of the mod command is valid
    private void validateRoot(ModCommand command) {
        // Error checks for the root
        if (command.type != ModCommand.ArgType.LITERAL) {
            throw new IllegalArgumentException("Root commands must be LITERAL: " +
                    command.name + ".");
        }

        if (!command.name.startsWith(".")) {
            throw new IllegalArgumentException("Root commands must start with '.': " +
                    command.name + ".");
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
}
