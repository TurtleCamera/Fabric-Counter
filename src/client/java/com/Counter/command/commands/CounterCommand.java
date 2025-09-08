package com.Counter.command.commands;

import com.Counter.CounterMod;
import com.Counter.command.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class CounterCommand extends Command {
    // Register the /counter command
    public void register() {
        // Note: This doesn't work because commands can only be registered server-side. I need to use the mixin instead.
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, commandSource) -> dispatcher.register(CommandManager.literal("counter")
                .then(CommandManager.argument("track", StringArgumentType.string())
                        .then(CommandManager.argument("emote", StringArgumentType.string()))
                        .executes(context -> {
                            // Get the name of the emote the player wants to track and store it in the config
                            String emoteName =  StringArgumentType.getString(context, "emote");
                            CounterMod.configManager.getConfig().emotes.add(emoteName);

                            // Emote did exist
                            MutableText message = Text.literal("Now tracking\"").styled(style -> style.withColor(Formatting.GREEN))
                                    .append(Text.literal(emoteName).styled(style -> style.withColor(Formatting.AQUA)))
                                    .append(Text.literal(".\"").styled(style -> style.withColor(Formatting.GREEN)));
                            context.getSource().sendFeedback(() -> message, false);

                            // Return 1 to indicate success
                            CounterMod.saveConfig();
                            return 1;
                        }))
                .then(CommandManager.argument("untrack", StringArgumentType.string())
                        .then(CommandManager.argument("emote", StringArgumentType.string()))
                        .executes(context -> {
                            // Get the name of the emote the player wants to track and store it in the config
                            String emoteName =  StringArgumentType.getString(context, "emote");

                            // Different cases depending on whether the emote is already being tracked.
                            if(CounterMod.configManager.getConfig().emotes.remove(emoteName)) {
                                // Emote did exist
                                MutableText message = Text.literal("No longer tracking\"").styled(style -> style.withColor(Formatting.GREEN))
                                        .append(Text.literal(emoteName).styled(style -> style.withColor(Formatting.AQUA)))
                                        .append(Text.literal(".\"").styled(style -> style.withColor(Formatting.GREEN)));
                                context.getSource().sendFeedback(() -> message, false);
                            }
                            else {
                                // Emote didn't exist
                                MutableText message = Text.literal("\"").styled(style -> style.withColor(Formatting.RED))
                                        .append(Text.literal(emoteName).styled(style -> style.withColor(Formatting.AQUA)))
                                        .append(Text.literal("\" wasn't being tracked.").styled(style -> style.withColor(Formatting.RED)));
                                context.getSource().sendFeedback(() -> message, false);
                            }

                            // Return 1 to indicate success
                            CounterMod.saveConfig();
                            return 1;
                        }))
        ));
    }
}
