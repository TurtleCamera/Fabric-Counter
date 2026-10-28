
package com.Counter;

import com.Counter.config.CounterConfig;
import com.Counter.utils.UUIDHandler;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Sends one assigned phrase, or batches Shift-modified presses into one chat message. */
public class PhraseKeybindHandler implements ClientModInitializer {
    private static final int MAX_CHAT_LENGTH = 256;

    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(
                    Identifier.fromNamespaceAndPath("counter", "phrase_counter")
            );

    private static final List<String> pending = new ArrayList<>();
    private static boolean wasShiftDown;
    private static boolean sending;

    // Used by the existing sendChat mixin to avoid re-counting and auto-appending our message.
    public static boolean isSending() {
        return sending;
    }

    @Override
    public void onInitializeClient() {
        KeyMapping key = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.counter.send_assigned_phrase",
                InputConstants.Type.KEYBOARD,
                21, // R
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.getConnection() == null) {
                pending.clear();
                wasShiftDown = false;
                while (key.consumeClick()) {
                    // Discard presses outside a world.
                }
                return;
            }

            boolean shiftDown = client.hasShiftDown();

            // Never activate a gameplay keybind while a screen is open.
            if (client.gui.screen() == null) {
                while (key.consumeClick()) {
                    String entry = makeEntry();
                    if (entry == null) {
                        continue;
                    }

                    if (shiftDown) {
                        pending.add(entry);
                    } else {
                        send(client, entry);
                    }
                }
            } else {
                while (key.consumeClick()) {
                    // Discard GUI presses.
                }
            }

            // Releasing the final held Shift sends the batch.
            if (wasShiftDown && !shiftDown && !pending.isEmpty()) {
                send(client, joinEntries(pending));
                pending.clear();
            }

            wasShiftDown = shiftDown;
        });
    }

    private static String makeEntry() {
        CounterConfig config = CounterMod.configManager.getConfig();
        String assigned = config.keybindPhrase;

        if (assigned == null || assigned.isBlank()) {
            return null;
        }

        // Match tracked phrases case-insensitively, retaining their canonical spelling.
        String tracked = null;
        for (String phrase : config.phrases) {
            if (phrase.equalsIgnoreCase(assigned)) {
                tracked = phrase;
                break;
            }
        }

        if (tracked == null || !config.enable) {
            return assigned;
        }

        config.performCountersChecks(tracked, true);

        Map<String, Integer> counters = config.counters.get(UUIDHandler.getUUID());
        int count = counters.get(tracked) + 1;
        counters.put(tracked, count);

        CounterMod.saveConfig();
        return assigned + " X" + count;
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

    private static void send(Minecraft client, String message) {
        if (message.length() > MAX_CHAT_LENGTH) {
            client.player.sendSystemMessage(Component.literal(
                    "Keybind message exceeds 256 characters and was not sent. Counters were already updated."
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
    }
}
