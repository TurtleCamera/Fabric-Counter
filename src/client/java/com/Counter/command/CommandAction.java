package com.Counter.command;

@FunctionalInterface
public interface CommandAction {
    // This method will be called when a command is executed
    void execute(CommandContext context);
}
