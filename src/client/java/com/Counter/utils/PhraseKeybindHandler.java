
package com.Counter.utils;

import com.Counter.CounterMod;
import com.Counter.config.CounterConfig;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Sends one assigned phrase, or batches modifier-held presses into one chat message. */
public class PhraseKeybindHandler implements ClientModInitializer {
    private static final int MAX_CHAT_LENGTH = 256;

    private static final String CATEGORY = "key.categories.counter";

    private static int pendingPresses;
    private static boolean wasBatchModifierDown;
    private static boolean sending;

    // Used by the existing sendChat mixin to avoid re-counting and auto-appending our message.
    public static boolean isSending() {
        return sending;
    }

    @Override
    public void onInitializeClient() {
        KeyMapping key = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.counter.send_assigned_phrase",
                InputConstants.Type.KEYSYM,
                82, // R (GLFW key code)
                CATEGORY
        ));

        KeyMapping batchModifier = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.counter.batch_modifier",
                InputConstants.Type.KEYSYM,
                340, // Left Shift (GLFW key code)
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.getConnection() == null) {
                pendingPresses = 0;
                wasBatchModifierDown = false;
                while (key.consumeClick()) {
                    // Discard presses outside a world.
                }
                return;
            }

            boolean batchModifierDown = batchModifier.isDown();

            // Never activate a gameplay keybind while a screen is open.
            if (client.screen == null) {
                while (key.consumeClick()) {
                    if (batchModifierDown) {
                        pendingPresses++;
                    } else {
                        send(client, 1);
                    }
                }
            } else {
                while (key.consumeClick()) {
                    // Discard GUI presses.
                }
            }

            // Releasing the configured modifier sends the batch.
            if (wasBatchModifierDown && !batchModifierDown && pendingPresses > 0) {
                send(client, pendingPresses);
                pendingPresses = 0;
            }

            wasBatchModifierDown = batchModifierDown;
        });
    }

    /** Build the message without changing counters, then commit only after sending. */
    private static void send(Minecraft client, int presses) {
        CounterConfig config = CounterMod.configManager.getConfig();
        String assigned = config.keybindPhrase;
        if (assigned == null || assigned.isBlank() || presses <= 0) {
            return;
        }

        String tracked = null;
        if (config.enable) {
            for (String phrase : config.phrases) {
                if (phrase.equalsIgnoreCase(assigned)) {
                    tracked = phrase;
                    break;
                }
            }
        }

        int initialCount = 0;
        if (tracked != null) {
            Map<String, Integer> counters = config.counters == null
                    ? null : config.counters.get(UUIDHandler.getUUID());
            if (counters != null) {
                initialCount = Math.max(0, counters.getOrDefault(tracked, 0));
            }
        }

        List<String> entries = new ArrayList<>();
        for (int i = 1; i <= presses; i++) {
            entries.add(tracked == null ? assigned : assigned + " X" + (initialCount + i));
        }
        String message = joinEntries(entries);
        if (message.length() > MAX_CHAT_LENGTH) {
            client.player.sendSystemMessage(Component.literal(
                    "Keybind message exceeds 256 characters and was not sent."
            ).withStyle(ChatFormatting.RED));
            return;
        }

        // sendChat is intercepted by ModifyMessageMixin; bypass its normal transformations.
        sending = true;
        try {
            client.getConnection().sendChat(message);
        } finally {
            sending = false;
        }

        if (tracked != null) {
            config.performCountersChecks(tracked, true);
            config.counters.get(UUIDHandler.getUUID()).put(tracked, initialCount + presses);
            CounterMod.saveConfig();
        }
    }

    private static String joinEntries(List<String> entries) {
        if (entries.size() == 1) {
            return entries.get(0);
        }
        if (entries.size() == 2) {
            return entries.get(0) + " and " + entries.get(1);
        }
        return String.join(", ", entries.subList(0, entries.size() - 1))
                + ", and " + entries.get(entries.size() - 1);
    }
}
