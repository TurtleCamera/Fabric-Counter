package com.Counter.command;

import com.Counter.command.commands.*;

public class CommandsManager {
    // Register all commands
    public void registerCommands() {
        new CounterCommand().register();
    }
}
