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

    // This is used for LITERAL ArgTypes, meaning we need to search all
    // command names to see if the argument matches any of them.
    private String[] literals;
    public int literalIndex = -1;

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
    private boolean parseArg(String arg, ModCommand.ArgType type, String argName) {
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
                // If the list of literals is empty, then set the literalIndex to -1
                if (literals == null) {
                    literalIndex = -1;
                    return false;
                }

                // Return true if the argument matches any of the literal names
                for (int i = 0; i < literals.length; i++) {
                    // Get the name of the literal
                    String name = literals[i];

                    // Does the argument match the node's name?
                    if (arg.equals(name)) {
                        literalIndex = i;
                        return true;
                    }
                }

                // Not found
                literalIndex = -1;
                return false;
            default:
                // Return false by default
                return false;
        }
    }

    // Parses the next argument and returns true if it's valid, false otherwise. Will
    // also return false if it detects more than one space between each argument. The
    // argName is ignored if the ArgType is LITERAL because we'll use the list of
    // literal names instead.
    public boolean processNextArg(ModCommand.ArgType type, String argName) {
        // Strip leading whitespaces before we proceed. Note: vanilla command
        // parsing allows multiple spaces between arguments; however, the
        // suggestor won't suggest anything. It would instead say invalid
        // argument, even though you can still enter the command (assuming
        // the arguments are correct).
        command = command.stripLeading();

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

    // Sets the current list of literals. This is used if the current ArgType is LITERAL,
    // meaning we need to search all command names to see if the argument matches any of them.
    public void setLiterals(String[] literals) {
        this.literals = literals;
    }
}