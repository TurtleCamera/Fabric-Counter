package com.Counter.command;

import com.Counter.CounterMod;
import com.Counter.utils.Tuple;
import com.Counter.utils.UUIDHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.ChatFormatting;

import java.util.ArrayList;

public class ModCommandRegistry {
    public ArrayList<ModCommand> COMMANDS;

    public ModCommandRegistry() {
        // Initialize commands
        COMMANDS = new ArrayList<>();

        // .track
        ModCommand track = new ModCommand(".track", ModCommand.ArgType.LITERAL)
                .then(new ModCommand("<phrase>", ModCommand.ArgType.GREEDY)
                        .executes(context -> {
                            // Get the phrase
                            String phrase = context.getString("<phrase>");

                            // An instance of the player, so we can send messages to them
                            LocalPlayer player =  Minecraft.getInstance().player;

                            // Try adding it
                            boolean isAdded = addIgnoreCase(CounterMod.configManager.getConfig().phrases, phrase);

                            // Different messages depending on whether we're already tracking this phrase
                            if (isAdded) {
                                // Phrase was added
                                MutableComponent message = Component.literal("Now tracking the phrase \"").withStyle(style -> style.withColor(ChatFormatting.GREEN))
                                        .append(Component.literal(phrase).withStyle(style -> style.withColor(ChatFormatting.AQUA)))
                                        .append(Component.literal("\".").withStyle(style -> style.withColor(ChatFormatting.GREEN)));
                                player.sendSystemMessage(message);

                                // Save to configuration
                                CounterMod.saveConfig();
                            }
                            else {
                                // Phrase already tracked
                                MutableComponent message = Component.literal("Already tracking the phrase \"").withStyle(style -> style.withColor(ChatFormatting.GREEN))
                                        .append(Component.literal(phrase).withStyle(style -> style.withColor(ChatFormatting.AQUA)))
                                        .append(Component.literal("\".").withStyle(style -> style.withColor(ChatFormatting.GREEN)));
                                player.sendSystemMessage(message);
                            }
                        }));

        // .untrack
        ModCommand untrack = new ModCommand(".untrack", ModCommand.ArgType.LITERAL)
                .then(new ModCommand("<phrase>", ModCommand.ArgType.GREEDY)
                        .executes(context -> {
                            // Get the phrase
                            String phrase = context.getString("<phrase>");

                            // An instance of the player, so we can send messages to them
                            LocalPlayer player =  Minecraft.getInstance().player;

                            // Try removing the phrase
                            int removedIndex = removeIgnoreCase(CounterMod.configManager.getConfig().phrases, phrase);

                            // Different messages depending on whether we're already tracking this phrase
                            if (removedIndex != -1) {
                                // Phrase was removed
                                MutableComponent message = Component.literal("No longer tracking the phrase \"").withStyle(style -> style.withColor(ChatFormatting.GREEN))
                                        .append(Component.literal(phrase).withStyle(style -> style.withColor(ChatFormatting.AQUA)))
                                        .append(Component.literal("\".").withStyle(style -> style.withColor(ChatFormatting.GREEN)));
                                player.sendSystemMessage(message);

                                // Save to configuration
                                CounterMod.saveConfig();
                            }
                            else {
                                // Phrase didn't exist
                                MutableComponent message = Component.literal("The phrase \"").withStyle(style -> style.withColor(ChatFormatting.RED))
                                        .append(Component.literal(phrase).withStyle(style -> style.withColor(ChatFormatting.AQUA)))
                                        .append(Component.literal("\" was not being tracked.").withStyle(style -> style.withColor(ChatFormatting.RED)));
                                player.sendSystemMessage(message);
                            }
                        }));

        // .list
        ModCommand list = new ModCommand(".list", ModCommand.ArgType.LITERAL)
                .then(new ModCommand("shortcut", ModCommand.ArgType.LITERAL)
                    .executes(context -> {
                        // An instance of the player, so we can send messages to them
                        LocalPlayer player =  Minecraft.getInstance().player;

                        // If there are no tracked phrases, print a different message
                        if (CounterMod.configManager.getConfig().shortcuts.isEmpty()) {
                            MutableComponent message = Component.literal("There are no tracked shortcuts.").withStyle(style -> style.withColor(ChatFormatting.GREEN));
                            player.sendSystemMessage(message);
                        }
                        else {
                            // List all tracked phrases
                            player.sendSystemMessage(Component.literal("Listing all tracked shortcuts: ").withStyle(style -> style.withColor(ChatFormatting.GREEN)));
                            for (Tuple<String, String> tuple : CounterMod.configManager.getConfig().shortcuts) {
                                MutableComponent message = Component.literal("- ").withStyle(style -> style.withColor(ChatFormatting.GREEN))
                                        .append(Component.literal(tuple.second()).withStyle(style -> style.withColor(ChatFormatting.AQUA))
                                                .append(Component.literal(" -> ").withStyle(style -> style.withColor(ChatFormatting.GREEN))
                                                        .append(Component.literal(tuple.first()).withStyle(style -> style.withColor(ChatFormatting.AQUA)))));
                                player.sendSystemMessage(message);
                            }
                        }
                    }))
                .then(new ModCommand("phrase", ModCommand.ArgType.LITERAL)
                        .executes(context -> {
                            // An instance of the player, so we can send messages to them
                            LocalPlayer player =  Minecraft.getInstance().player;

                            // If there are no tracked phrases, print a different message
                            if (CounterMod.configManager.getConfig().phrases.isEmpty()) {
                                MutableComponent message = Component.literal("There are no tracked phrases.").withStyle(style -> style.withColor(ChatFormatting.GREEN));
                                player.sendSystemMessage(message);
                            }
                            else {
                                // List all tracked phrases
                                player.sendSystemMessage(Component.literal("Listing all tracked phrases: ").withStyle(style -> style.withColor(ChatFormatting.GREEN)));
                                for (String phrase : CounterMod.configManager.getConfig().phrases) {
                                    MutableComponent message = Component.literal("- ").withStyle(style -> style.withColor(ChatFormatting.GREEN))
                                            .append(Component.literal(phrase).withStyle(style -> style.withColor(ChatFormatting.AQUA)));
                                    player.sendSystemMessage(message);
                                }
                            }
                        }));

        // .enable
        ModCommand enable = new ModCommand(".enable", ModCommand.ArgType.LITERAL)
                .executes(context -> {
                    // An instance of the player, so we can send messages to them
                    LocalPlayer player = Minecraft.getInstance().player;

                    // Running this command without specifying an argument should tell the player
                    // if autocorrect is enabled or disabled.
                    if (CounterMod.configManager.getConfig().enable) {
                        // An instance of the player, so we can send messages to them
                        MutableComponent message = Component.literal("Automatic counting is currently enabled.").withStyle(style -> style.withColor(ChatFormatting.GREEN));
                        player.sendSystemMessage(message);
                    }
                    else {
                        MutableComponent message = Component.literal("Automatic counting is currently disabled.").withStyle(style -> style.withColor(ChatFormatting.RED));
                        player.sendSystemMessage(message);
                    }
                })
                .then(new ModCommand("true", ModCommand.ArgType.LITERAL)
                        .executes(context -> {
                            // An instance of the player, so we can send messages to them
                            LocalPlayer player = Minecraft.getInstance().player;

                            if (!CounterMod.configManager.getConfig().enable) {
                                // Enable autocorrect
                                CounterMod.configManager.getConfig().enable = true;

                                // Tell the player that autocorrect was enabled
                                MutableComponent message = Component.literal("Automatic counting is now enabled.").withStyle(style -> style.withColor(ChatFormatting.GREEN));
                                player.sendSystemMessage(message);

                                // Save to config
                                CounterMod.saveConfig();
                            }
                            else {
                                MutableComponent message = Component.literal("Automatic counting is already enabled.").withStyle(style -> style.withColor(ChatFormatting.GREEN));
                                player.sendSystemMessage(message);
                            }
                        }))
                .then(new ModCommand("false", ModCommand.ArgType.LITERAL)
                        .executes(context -> {
                            // An instance of the player, so we can send messages to them
                            LocalPlayer player = Minecraft.getInstance().player;

                            if (CounterMod.configManager.getConfig().enable) {
                                // Disable autocorrect
                                CounterMod.configManager.getConfig().enable = false;

                                // Tell the player that autocorrect was disabled
                                MutableComponent message = Component.literal("Automatic counting is now disabled.").withStyle(style -> style.withColor(ChatFormatting.RED));
                                player.sendSystemMessage(message);

                                // Save to config
                                CounterMod.saveConfig();
                            }
                            else {
                                MutableComponent message = Component.literal("Automatic counting is already disabled.").withStyle(style -> style.withColor(ChatFormatting.RED));
                                player.sendSystemMessage(message);
                            }
                        }));

        // .set
        ModCommand set = new ModCommand(".set", ModCommand.ArgType.LITERAL)
                .then(new ModCommand("<phrase>", ModCommand.ArgType.STRING)
                        .then(new ModCommand("<count>", ModCommand.ArgType.INTEGER)
                            .executes(context -> {
                                // An instance of the player, so we can send messages to them
                                LocalPlayer player = Minecraft.getInstance().player;

                                // Get the phrase and count
                                String phrase = context.getString("<phrase>");
                                int count = context.getInteger("<count>");

                                // Phrases must be at least 3 words long.
                                if (phrase.length() < 3) {
                                    // Invalid phrase
                                    MutableComponent message = Component.literal("Phrases must be at least 3 letters long.").withStyle(style -> style.withColor(ChatFormatting.RED));
                                    player.sendSystemMessage(message);

                                    return;
                                }

                                // Count must be at least 0
                                if (count < 0) {
                                    // Invalid count
                                    MutableComponent message = Component.literal("The count must be at least 0.").withStyle(style -> style.withColor(ChatFormatting.RED));
                                    player.sendSystemMessage(message);

                                    return;
                                }

                                // Get the UUID of the current server (or single player)
                                String uuid = UUIDHandler.getUUID();

                                // Is this phrase being tracked?
                                if (!CounterMod.configManager.getConfig().phrases.contains(phrase)) {
                                    // Tell them that we aren't tracking this phrase, so there's nothing to reset.
                                    MutableComponent message = Component.literal("The phrase \"").withStyle(style -> style.withColor(ChatFormatting.RED))
                                            .append(Component.literal(phrase).withStyle(style -> style.withColor(ChatFormatting.AQUA))
                                            .append(Component.literal("\" was not being tracked.").withStyle(style -> style.withColor(ChatFormatting.RED))));
                                    player.sendSystemMessage(message);
                                }
                                else {
                                    // Perform updates and error checks on the config's counters
                                    CounterMod.configManager.getConfig().performCountersChecks(phrase, false);

                                    // This phrase is being tracked, so set the counter to what the player specified
                                    CounterMod.configManager.getConfig().counters.get(uuid).put(phrase, count);

                                    // Tell the player what happened
                                    MutableComponent message = Component.literal("Set the counter for the phrase \"").withStyle(style -> style.withColor(ChatFormatting.GREEN))
                                            .append(Component.literal(phrase).withStyle(style -> style.withColor(ChatFormatting.AQUA))
                                                    .append(Component.literal("\" to ").withStyle(style -> style.withColor(ChatFormatting.GREEN))
                                                            .append(Component.literal("" + count).withStyle(style -> style.withColor(ChatFormatting.AQUA))
                                                                    .append(Component.literal(".").withStyle(style -> style.withColor(ChatFormatting.GREEN))))));
                                    player.sendSystemMessage(message);

                                    // Save the config
                                    CounterMod.saveConfig();
                                }
                            })));

        // .append
        ModCommand append = new ModCommand(".append", ModCommand.ArgType.LITERAL)
                .executes(context -> {
                    // An instance of the player, so we can send messages to them
                    LocalPlayer player = Minecraft.getInstance().player;

                    // Get the current append phrase
                    String phrase = CounterMod.configManager.getConfig().appendPhrase;

                    // Stop appending the phrase if it exists
                    if (phrase != null) {
                        // Tell the player that they removed the append phrase
                        MutableComponent message = Component.literal("No longer appending the phrase \"").withStyle(style -> style.withColor(ChatFormatting.GREEN))
                                .append(Component.literal(phrase).withStyle(style -> style.withColor(ChatFormatting.AQUA))
                                        .append(Component.literal("\" to the end of each sentence.").withStyle(style -> style.withColor(ChatFormatting.GREEN))));
                        player.sendSystemMessage(message);

                        // Remove the append phrase
                        CounterMod.configManager.getConfig().appendPhrase = null;

                        // Save the config
                        CounterMod.saveConfig();
                    }
                    else {
                        // Tell the player that nothing was being appended
                        MutableComponent message = Component.literal("No phrases are being appended to the end of each sentence.").withStyle(style -> style.withColor(ChatFormatting.RED));
                        player.sendSystemMessage(message);
                    }
                })
                .then(new ModCommand("<phrase>", ModCommand.ArgType.STRING)
                        .executes(context -> {
                            // An instance of the player, so we can send messages to them
                            LocalPlayer player = Minecraft.getInstance().player;

                            // Get the phrase
                            String phrase = context.getString("<phrase>");

                            // A phrase can be appended whether or not it is tracked.
                            if (CounterMod.configManager.getConfig().appendPhrase != null && CounterMod.configManager.getConfig().appendPhrase.equals(phrase)) {
                                // If this phrase is already being appended, do nothing and tell the player
                                MutableComponent message = Component.literal("Already appending the phrase \"").withStyle(style -> style.withColor(ChatFormatting.GREEN))
                                        .append(Component.literal(phrase).withStyle(style -> style.withColor(ChatFormatting.AQUA))
                                                .append(Component.literal("\".").withStyle(style -> style.withColor(ChatFormatting.GREEN))));
                                player.sendSystemMessage(message);
                            }
                            else {
                                // Store this phrase for appending
                                CounterMod.configManager.getConfig().appendPhrase = phrase;

                                // Tell the player what happened
                                MutableComponent message = Component.literal("The phrase \"").withStyle(style -> style.withColor(ChatFormatting.GREEN))
                                        .append(Component.literal(phrase).withStyle(style -> style.withColor(ChatFormatting.AQUA))
                                                .append(Component.literal("\" will now be appended at the end of each sentence if possible.").withStyle(style -> style.withColor(ChatFormatting.GREEN))));
                                player.sendSystemMessage(message);

                                // Save the config
                                CounterMod.saveConfig();
                            }
                        }));

        // .shortcut
        ModCommand shortcut = new ModCommand(".shortcut", ModCommand.ArgType.LITERAL)
                .then(new ModCommand("remove", ModCommand.ArgType.LITERAL)
                        .then(new ModCommand("<shortcut>", ModCommand.ArgType.STRING)
                                .executes(context -> {
                                    // An instance of the player, so we can send messages to them
                                    LocalPlayer player = Minecraft.getInstance().player;

                                    // Get the shortcut
                                    String phraseShortcut = context.getString("<shortcut>");

                                    // Shortcuts can only contain letters and numbers
                                    if (!phraseShortcut.matches("[a-zA-Z0-9]+")) {
                                        // Regular message without the "it was previously..."
                                        MutableComponent message = Component.literal("Shortcuts can only contain letters and numbers.").withStyle(style -> style.withColor(ChatFormatting.RED));
                                        player.sendSystemMessage(message);

                                        return;
                                    }

                                    // Remove the shortcut and phrase if it exists.
                                    String removedPhrase = null;
                                    for (int i = 0; i < CounterMod.configManager.getConfig().shortcuts.size(); i++) {
                                        // Get the current tuple
                                        Tuple<String, String> tuple = CounterMod.configManager.getConfig().shortcuts.get(i);

                                        // Did we find the shortcut?
                                        if (tuple.second().equals(phraseShortcut)) {
                                            // Remove the shortcut
                                            removedPhrase = CounterMod.configManager.getConfig().shortcuts.get(i).first();
                                            CounterMod.configManager.getConfig().shortcuts.remove(i);

                                            break;
                                        }
                                    }

                                    // Different messages to the player depending on whether we found the shortcut
                                    if (removedPhrase != null) {
                                        // Regular message without the "it was previously..."
                                        MutableComponent message = Component.literal("The shortcut \"").withStyle(style -> style.withColor(ChatFormatting.GREEN))
                                                .append(Component.literal(phraseShortcut).withStyle(style -> style.withColor(ChatFormatting.AQUA))
                                                        .append(Component.literal("\" will no longer be replaced with the phrase \"").withStyle(style -> style.withColor(ChatFormatting.GREEN))
                                                                .append(Component.literal(removedPhrase).withStyle(style -> style.withColor(ChatFormatting.AQUA))
                                                                        .append(Component.literal(".\"").withStyle(style -> style.withColor(ChatFormatting.GREEN))))));
                                        player.sendSystemMessage(message);

                                        // Save the config
                                        CounterMod.saveConfig();
                                    }
                                    else {
                                        MutableComponent message = Component.literal("The shortcut \"").withStyle(style -> style.withColor(ChatFormatting.RED))
                                                .append(Component.literal(phraseShortcut).withStyle(style -> style.withColor(ChatFormatting.AQUA))
                                                        .append(Component.literal("\" was not being replaced by a phrase.").withStyle(style -> style.withColor(ChatFormatting.RED))));
                                        player.sendSystemMessage(message);
                                    }
                                })))
                .then(new ModCommand("add", ModCommand.ArgType.LITERAL)
                        .then(new ModCommand("<phrase>", ModCommand.ArgType.STRING)
                                .then(new ModCommand("<shortcut>", ModCommand.ArgType.STRING)
                                        .executes(context -> {
                                            // An instance of the player, so we can send messages to them
                                            LocalPlayer player = Minecraft.getInstance().player;

                                            // Get the phrase and shortcut
                                            String phrase = context.getString("<phrase>");
                                            String phraseShortcut = context.getString("<shortcut>");

                                            // Shortcuts can only contain letters and numbers
                                            if (!phraseShortcut.matches("[a-zA-Z0-9]+")) {
                                                // Regular message without the "it was previously..."
                                                MutableComponent message = Component.literal("Shortcuts can only contain letters and numbers.").withStyle(style -> style.withColor(ChatFormatting.RED));
                                                player.sendSystemMessage(message);

                                                return;
                                            }

                                            // Are we already tracking this shortcut? If so, replace it.
                                            String previousPhrase = null;
                                            boolean samePhrase =  false;
                                            for (int i = 0; i < CounterMod.configManager.getConfig().shortcuts.size(); i++) {
                                                // Get the current tuple
                                                Tuple<String, String> tuple = CounterMod.configManager.getConfig().shortcuts.get(i);

                                                // If this shortcut already exists, we need to replace it
                                                if (tuple.second().equals(phraseShortcut)) {
                                                    // Get the index and previous phrase
                                                    previousPhrase = tuple.first();

                                                    // Was the phrase also the same?
                                                    if (tuple.first().equals(phrase)) {
                                                        samePhrase = true;
                                                    }

                                                    // Replace the phrase with the new one
                                                    Tuple<String, String> newTuple = new Tuple<>(phrase, phraseShortcut);
                                                    CounterMod.configManager.getConfig().shortcuts.set(i, newTuple);

                                                    break;
                                                }
                                            }

                                            if (previousPhrase != null) {
                                                // Different messages depending on whether the phrase was also the same
                                                if (samePhrase) {
                                                    // Tell the player that they already replace the shortcut with the phrase
                                                    MutableComponent message = Component.literal("The shortcut \"").withStyle(style -> style.withColor(ChatFormatting.GREEN))
                                                            .append(Component.literal(phraseShortcut).withStyle(style -> style.withColor(ChatFormatting.AQUA))
                                                                    .append(Component.literal("\" was already being replaced with the phrase \"").withStyle(style -> style.withColor(ChatFormatting.GREEN))
                                                                            .append(Component.literal(phrase).withStyle(style -> style.withColor(ChatFormatting.AQUA))
                                                                                    .append(Component.literal("\".").withStyle(style -> style.withColor(ChatFormatting.GREEN))))));
                                                    player.sendSystemMessage(message);
                                                }
                                                else {
                                                    // Tell the player that we're replacing the phrase tied to the shortcut
                                                    MutableComponent message = Component.literal("The shortcut \"").withStyle(style -> style.withColor(ChatFormatting.GREEN))
                                                            .append(Component.literal(phraseShortcut).withStyle(style -> style.withColor(ChatFormatting.AQUA))
                                                                    .append(Component.literal("\" will now be replaced with the phrase \"").withStyle(style -> style.withColor(ChatFormatting.GREEN))
                                                                            .append(Component.literal(phrase).withStyle(style -> style.withColor(ChatFormatting.AQUA))
                                                                                    .append(Component.literal("\". It was previously replaced with the phrase \"").withStyle(style -> style.withColor(ChatFormatting.GREEN))
                                                                                            .append(Component.literal(previousPhrase).withStyle(style -> style.withColor(ChatFormatting.AQUA))
                                                                                                    .append(Component.literal(".\"").withStyle(style -> style.withColor(ChatFormatting.GREEN))))))));
                                                    player.sendSystemMessage(message);
                                                }
                                            }
                                            else {
                                                // This shortcut wasn't already being stored, so we need to add it
                                                Tuple<String, String> newTuple = new Tuple<>(phrase, phraseShortcut);
                                                CounterMod.configManager.getConfig().shortcuts.add(newTuple);

                                                // Let the player know that it was added
                                                MutableComponent message = Component.literal("The shortcut \"").withStyle(style -> style.withColor(ChatFormatting.GREEN))
                                                        .append(Component.literal(phraseShortcut).withStyle(style -> style.withColor(ChatFormatting.AQUA))
                                                                .append(Component.literal("\" will now be replaced with the phrase \"").withStyle(style -> style.withColor(ChatFormatting.GREEN))
                                                                        .append(Component.literal(phrase).withStyle(style -> style.withColor(ChatFormatting.AQUA))
                                                                                .append(Component.literal("\".").withStyle(style -> style.withColor(ChatFormatting.GREEN))))));
                                                player.sendSystemMessage(message);
                                            }

                                            // Save the config
                                            CounterMod.saveConfig();
                                        }))));

        // .bindphrase: assign any phrase to the configurable keybind.
        ModCommand bindphrase = new ModCommand(".bindphrase", ModCommand.ArgType.LITERAL)
                .executes(context -> {
                    LocalPlayer player = Minecraft.getInstance().player;
                    String phrase = CounterMod.configManager.getConfig().keybindPhrase;
                    player.sendSystemMessage(Component.literal(phrase == null
                            ? "No keybind phrase assigned. Use .bindphrase <phrase>."
                            : "Current keybind phrase: " + phrase).withStyle(ChatFormatting.GREEN));
                })
                .then(new ModCommand("<phrase>", ModCommand.ArgType.GREEDY)
                        .executes(context -> {
                            String phrase = context.getString("<phrase>").trim();
                            LocalPlayer player = Minecraft.getInstance().player;
                            if (phrase.equalsIgnoreCase("clear")) {
                                CounterMod.configManager.getConfig().keybindPhrase = null;
                                player.sendSystemMessage(Component.literal("Keybind phrase cleared.").withStyle(ChatFormatting.GREEN));
                            } else if (phrase.isEmpty()) {
                                player.sendSystemMessage(Component.literal("Please specify a phrase.").withStyle(ChatFormatting.RED));
                                return;
                            } else {
                                CounterMod.configManager.getConfig().keybindPhrase = phrase;
                                player.sendSystemMessage(Component.literal("Keybind phrase set to: " + phrase).withStyle(ChatFormatting.GREEN));
                            }
                            CounterMod.saveConfig();
                        }));

        // .help
        ModCommand help = new ModCommand(".help", ModCommand.ArgType.LITERAL)
                .executes(context -> {
                    LocalPlayer player = Minecraft.getInstance().player;

                    // Header
                    player.sendSystemMessage(Component.literal("§a=========== Counter Mod Help ==========="));

                    // Condensed commands
                    player.sendSystemMessage(Component.literal("§b.track <phrase> §7- Starts counting the number of times the phrase is used for each server. The phrase must be at least 3 letters long."));
                    player.sendSystemMessage(Component.literal("§b.untrack <phrase> §7- Stops counting for the specified phrase. The phrase must be at least 3 letters long."));
                    player.sendSystemMessage(Component.literal("§b.list [phrase/shortcut] §7- List the tracked phrases or shortcuts."));
                    player.sendSystemMessage(Component.literal("§b.enable [true/false] §7- Enables or disables autocorrect for phrases."));
                    player.sendSystemMessage(Component.literal("§b.set <phrase> <count> §7- Sets the counter of a phrase to the specified value. The phrase must be at least 3 letters long and the count must be ≥ 0."));
                    player.sendSystemMessage(Component.literal("§b.append <phrase> §7- Appends a phrase to the end of each sentence (with its counter if tracked). Typing the command without any arguments removes the append phrase. This action is cancelled if a phrase is already at the start or end of the sentence, the sentence is only punctuation, or the sentence is inside (), [], or {}."));
                    player.sendSystemMessage(Component.literal("§b.shortcut add <phrase> <shortcut> §7- Add a shortcut for a phrase. Shortcuts will be replaced with the corresponding phrase."));
                    player.sendSystemMessage(Component.literal("§b.bindphrase [phrase/clear] §7- Set, view, or clear the phrase sent by the configurable keybind. Hold Shift to batch presses."));
                    player.sendSystemMessage(Component.literal("§b.shortcut remove <shortcut> §7- Remove a shortcut."));

                    // Footer
                    player.sendSystemMessage(Component.literal("§a======================================"));
                });

        // Register all commands
        register(track);
        register(untrack);
        register(list);
        register(enable);
        register(set);
        register(append);
        register(shortcut);
        register(bindphrase);
        register(help);
    }

    // Helper function to add a phrase. It returns true if it successfully added the phrase
    // and false if a copy of this phrase (ignore case) already exists.
    private boolean addIgnoreCase(ArrayList<String> phrases, String phrase) {
        // Don't add null phrases
        if (phrase == null) {
            return false;
        }

        // Does this phrase (ignore case) already exist in the list?
        for (String current : phrases) {
            if (current != null && current.equalsIgnoreCase(phrase)) {
                return false;
            }
        }

        // Can add the phrase to the list
        phrases.add(phrase);
        return true;
    }

    // Helper function to remove a phrase (ignore case)
    private int removeIgnoreCase(ArrayList<String> phrases, String phrase) {
        // Can't match null phrases
        if (phrase == null) {
            return -1;
        }

        // Find if the phrase (ignore cases) exists already
        for (int i = 0; i < phrases.size(); i++) {
            String current = phrases.get(i);
            if (current != null && current.equalsIgnoreCase(phrase)) {
                phrases.remove(i);
                return i;
            }
        }

        // Not found
        return -1;
    }

    // Registers the command
    private void register(ModCommand command) {
        // Make sure everything is valid before registering the command
        validateRoot(command);
        validateLeaves(command);
        COMMANDS.add(command);
    }

    // Checks if the root of the mod command is valid
    private void validateRoot(ModCommand root) {
        // Error checks for the root
        if (root.type != ModCommand.ArgType.LITERAL) {
            throw new IllegalArgumentException("Root commands must be LITERAL: " +
                    root.name + ".");
        }

        if (!root.name.startsWith(".")) {
            throw new IllegalArgumentException("Root commands must start with '.': " +
                    root.name + ".");
        }
    }

    // Checks if all leaf nodes have actions
    private void validateLeaves(ModCommand node) {
        // If this is a leaf node, check if it has an action
        if (node.children.isEmpty()) {
            if (node.action == null) {
                throw new IllegalArgumentException("Leaf nodes must have an action: " +
                        node.name + ".");
            }

            return;
        }

        // Loop through all the children
        for (ModCommand child : node.children) {
            validateLeaves(child);
        }
    }

    // Given an argument, return an index in modCommands if it matches any of the command names
    public static int findMatchIndex(ArrayList<ModCommand> modCommands, String argument) {
        // Check all commands to see if the argument matches any of their conditions
        for (int i  = 0; i < modCommands.size(); i++) {
            ModCommand currentCommand = modCommands.get(i);
            if (currentCommand.type == ModCommand.ArgType.STRING || currentCommand.type == ModCommand.ArgType.INTEGER || currentCommand.type == ModCommand.ArgType.DOUBLE) {
                // Always match (if it's one of these types, it should be the only child of the parent node)
                // TODO: Perhaps handle the logic of checking the correct type if needed
                return i;
            }

            if (currentCommand.type == ModCommand.ArgType.GREEDY) {
                // Greedy arguments consume the rest of the input, so stop here
                return -1;
            }

            if (currentCommand.type == ModCommand.ArgType.LITERAL && currentCommand.name.equalsIgnoreCase(argument)) {
                // Matches the literal
                return i;
            }
        }

        // Not found or the list was empty
        return -1;
    }

    // Given a list of mod commands, generate an ArrayList of command names as suggestions
    public static ArrayList<String> createSuggestions(ArrayList<ModCommand> modCommands, String prefix) {
        ArrayList<String> suggestions = new ArrayList<>();

        // Handle null modCommands
        if (modCommands == null) {
            return suggestions;
        }

        // Normalize prefix (treat null as "")
        if (prefix == null) {
            prefix = "";
        }

        for (ModCommand node : modCommands) {
            if (node != null && node.name != null) {
                if (node.name.startsWith(prefix)) {
                    suggestions.add(node.name);
                }
            }
        }

        return suggestions;
    }

    // This function assumes that the list passed in contains only literal ModCommand nodes.
    // It will return an array of strings consisting of the literal names
    public static String[] generateLiteralList(ArrayList<ModCommand> modCommands) {
        // Array containing the literal names
        String[] names =  new String[modCommands.size()];

        // Loop through all the ModCommand nodes
        for (int i = 0; i < modCommands.size(); i++) {
            // Get the current node
            ModCommand node = modCommands.get(i);

            // This shouldn't happen, but print an error message to the player if we,
            // for some reason, passed in a list of ModCommands that aren't literals.
            if (!node.type.equals(ModCommand.ArgType.LITERAL)) {
                LocalPlayer player =  Minecraft.getInstance().player;
                MutableComponent message = Component.literal("An error occurred in the searchLiterals() " +
                        "function because a list of ModCommands with non-literal argument " +
                        "types was passed in. Please contact TurtleCamera about this issue.")
                        .withStyle(style -> style.withColor(ChatFormatting.RED));
                player.sendSystemMessage(message);

                return null;
            }

            names[i] = node.name;
        }

        return names;
    }
}
