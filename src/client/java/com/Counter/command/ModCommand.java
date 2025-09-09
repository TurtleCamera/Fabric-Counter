package com.Counter.command;

import java.util.ArrayList;
import java.util.List;

// Used for both the suggestor and to parse commands
public class ModCommand {
    public enum ArgType {
        LITERAL,    // Fixed string
        STRING,     // Single word
        INTEGER,
        DOUBLE,
        GREEDY      // Consumes remaining of input
    }

    public String name;     // Name for literal or placeholder for arguments
    public ArgType type;
    public ArrayList<ModCommand> children = new ArrayList<>();   // Stores child arguments

    public ModCommand(String name, ArgType type) {
        this.name = name;
        this.type = type;
    }

    public ModCommand then(ModCommand child) {
        children.add(child);
        return this;
    }
}
