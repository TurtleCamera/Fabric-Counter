package com.Counter.utils;

import net.minecraft.client.Minecraft;

public class UUIDHandler {
    // Gets the UUID of the server the player is on (or single player)
    public static String getUUID() {
        Minecraft client = Minecraft.getInstance();
        String uuid;
        if (client.isLocalServer()) {
            // Use the same counter for all single player worlds
            uuid = "single_player";
        }
        else {
            // Use the server address (maybe consider the port)
            uuid = client.getCurrentServer().ip;
            uuid = uuid.split(":")[0];
        }

        return uuid;
    }
}
