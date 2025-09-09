package com.Counter.command;

import java.util.ArrayList;

// Used for both the suggestor and to parse commands
public class ModCommand {
    public enum ArgType {
        LITERAL,    // Fixed string
        STRING,     // Single word
        INTEGER,
        DOUBLE,
        GREEDY      // Consumes the rest of the input
    }

    public String name;     // Name for literal or placeholder for arguments
    public ArgType type;
    public ArrayList<ModCommand> children = new ArrayList<>();   // Stores child arguments
    public CommandAction action;    // Executable for the command

    public ModCommand(String name, ArgType type) {
        this.name = name;
        this.type = type;
    }

    public ModCommand then(ModCommand child) {
        // Error checks
        if (child == null) {
            throw new IllegalArgumentException("Child command cannot be null.");
        }

        if (child == this) {
            throw new IllegalArgumentException("A command cannot be its own child.");
        }

        if (type == ArgType.GREEDY) {
            throw new IllegalArgumentException("A GREEDY argument cannot have child commands.");
        }

        // Rule: If one child is non-literal, then it must be the only child
        boolean childIsLiteral = child.type == ArgType.LITERAL;
        if (!childIsLiteral && !children.isEmpty()) {
            throw new IllegalArgumentException("Non-literal child '" +
                    child.name + "' cannot be added because this command already has children.");
        }

        if (childIsLiteral && children.stream().anyMatch(c -> c.type != ArgType.LITERAL)) {
            throw new IllegalArgumentException("Cannot add literal child '" +
                    child.name + "' because a non-literal child already exists.");
        }

        // Can't add children if this is a leaf node (has an action)
        if (action != null) {
            throw new IllegalArgumentException("Cannot add a child if this is a leaf node (has an action).");
        }

        children.add(child);
        return this;
    }

    public ModCommand executes(CommandAction action) {
        if (children.size() > 0) {
            throw new IllegalArgumentException("Only leaf commands can have an action.");
        }
        this.action = action;
        return this;
    }
}
