package com.Counter.utils;

import net.minecraft.client.MinecraftClient;

public class UUIDGenerator {
    public static String generateUUID() {
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
