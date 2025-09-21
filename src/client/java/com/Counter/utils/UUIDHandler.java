package com.Counter.utils;

import net.minecraft.client.MinecraftClient;

public class UUIDHandler {
    // Gets the UUID of the server the player is on (or single player)
    public static String getUUID() {
        MinecraftClient client = MinecraftClient.getInstance();
        String uuid;
        if (client.isInSingleplayer() && client.getServer() != null) {
            // Use the same counter for all single player worlds
            uuid = "single_player";
        }
        else {
            // Use the server address (maybe consider the port)
            uuid = client.getCurrentServerEntry().address;
            uuid = uuid.split(":")[0];
        }

        return uuid;
    }
}
