package com.Counter.command;

import java.util.Map;

public class CommandContext {
    private final Map<String, Object> parsedArgs;

    public CommandContext(Map<String, Object> parsedArgs) {
        this.parsedArgs = parsedArgs;
    }

    public Object getArg(String name) {
        return parsedArgs.get(name);
    }

    public String getString(String name) {
        return (String) parsedArgs.get(name);
    }

    public Integer getInteger(String name) {
        return (Integer) parsedArgs.get(name);
    }

    public Double getDouble(String name) {
        return (Double) parsedArgs.get(name);
    }
}
