package com.Counter.command;

import java.util.HashMap;
import java.util.Map;

// Contains various helper functions for handling client-side commands
public class CommandParser {
    // Use a period as the delimiter because a lot of other mods use "#" instead.
    private static String DELIMITER = ".";

    // Stores the command string that we'll parse
    private String command;

    // Stores any arguments we parsed
    public Map<String, Object> parsedArgs;

    public CommandParser(String command) {
        this.command = command;
        parsedArgs = new HashMap<>();
    }

    public boolean isCommand() {
        return command != null && command.startsWith(DELIMITER);
    }

    // Checks if there's still an argument. This is used to check if the player typed
    // another argument past a leaf ModCommand node, which would make this command invalid.
    public boolean hasNext() {
        String check = command.trim();
        return check.length() > 0;
    }

    // Checks if the argument is of the correct type
    public boolean parseArg(String arg, ModCommand.ArgType type, String argName) {
        // Safety check, but this should not happen
        if (arg == null || arg.isEmpty()) {
            return false;
        }

        // Different cases for each type
        switch (type) {
            case ModCommand.ArgType.STRING:
                // The argument being passed is a string, so this is always true
                parsedArgs.put(argName, arg);
                return true;
            case ModCommand.ArgType.INTEGER:
                try {
                    int intArg = Integer.parseInt(arg);
                    parsedArgs.put(argName, intArg);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            case ModCommand.ArgType.DOUBLE:
                try {
                    double doubleArg = Double.parseDouble(arg);
                    parsedArgs.put(argName, doubleArg);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            case ModCommand.ArgType.GREEDY:
                parsedArgs.put(argName, arg);
                return true;
            case ModCommand.ArgType.LITERAL:
                // Nothing to store because this is a literal. This check should
                // have happened in the mixin, but still check just in case.
                return arg.equals(argName);
            default:
                // Return false by default
                return false;
        }
    }

    // Parses the next argument and returns true if it's valid, false otherwise. Will
    // also return false if it detects more than one space between each argument.
    public boolean processNextArg(ModCommand.ArgType type, String argName) {
        // Strip leading whitespaces before we proceed
        int oldLength = command.length();
        command = command.stripLeading();
        int newLength = command.length();

        // Vanilla commands don't allow more than one space between arguments
        if (oldLength - newLength > 1) {
            return false;
        }

        // Note: If this is a greedy argument, just parse the rest of the command
        String arg;
        if (type == ModCommand.ArgType.GREEDY) {
            arg = command;
            command = "";
            return parseArg(arg, type, argName);
        }

        // How far should we extract?
        int endIndex = command.indexOf(' ');    // Find the next space

        // If no space was found, then return the rest of the argument
        if (endIndex == -1) {
            arg = command;
            command = "";
        }
        else {
            // Otherwise, strip to the end index
            arg = command.substring(0, endIndex);
            command = command.substring(endIndex + 1);
        }

        // Parse the argument and return if this was successful
        return parseArg(arg, type, argName);
    }
}