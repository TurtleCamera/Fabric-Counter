package com.Counter.mixin;

import com.Counter.CounterMod;
import com.Counter.command.CommandContext;
import com.Counter.command.CommandParser;
import com.Counter.command.ModCommand;
import com.Counter.command.ModCommandRegistry;
import com.Counter.config.ConfigManager;
import com.Counter.config.CounterConfig;
import com.Counter.utils.LeviathanDistance;
import com.ibm.icu.impl.ICUCurrencyMetaInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.message.LastSeenMessagesCollector;
import net.minecraft.network.message.MessageBody;
import net.minecraft.network.message.MessageChain;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;

import java.time.Instant;
import java.util.*;

@Mixin(ClientPlayNetworkHandler.class)
public class ModifyMessageMixin {
    // Shadow private variables, so I can use them
    @Shadow
    private LastSeenMessagesCollector lastSeenMessagesCollector;
    @Shadow
    private MessageChain.Packer messagePacker;

    @Inject(
            method = "sendChatMessage",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onSendChatMessage(String content, CallbackInfo ci) {
        // Create a command parser
        CommandParser parser = new CommandParser(content);

        // If this is a command, parse it and don't send anything into the chat
        if (parser.isCommand()) {

            // Get the ModCommand tree
            ArrayList<ModCommand> current = CounterMod.CommandRegistry.COMMANDS;

            // If this is empty, just return the invalid command message.
            if (current == null || current.isEmpty()) {
                invalidCommand();
                ci.cancel();
                return;
            }

            // Keep looping until we reach a leaf node. The leaf nodes are assumed
            // to not have children because of the validation checker.
            while (true) {
                // We expect another argument
                if (!parser.hasNext()) {
                    // Invalid command because we didn't get another argument
                    invalidCommand();
                    break;
                }

                // Check if this list of nodes is a list of literals or a single non-literal node
                // Note: If a node is non-literal, it must be the only node in the list. If a node
                //       in the list is literal, then all the nodes must be literal.
                int nextNodeIndex;
                if (current.get(0).type == ModCommand.ArgType.LITERAL) {
                    // This is (assumed) a list of LITERAL ModCommand nodes.
                    // Get the names of the literals.
                    String[] names = ModCommandRegistry.generateLiteralList(current);

                    // Set the list of literal names before using the parser
                    parser.setLiterals(names);

                    // Now, we can try parsing the next argument. We don't need to put
                    // anything for the argName because the list of literal names will
                    // be used instead.
                    if (!parser.processNextArg(ModCommand.ArgType.LITERAL, "")) {
                        // If this failed, return the invalid command message
                        invalidCommand();
                        break;
                    }

                    // The matched index should be stored by the parser at this point
                    nextNodeIndex = parser.literalIndex;
                } else {
                    // Non-literal types can be parsed immediately.
                    ModCommand.ArgType type = current.get(0).type;
                    String argName = current.get(0).name;
                    if (!parser.processNextArg(type, argName)) {
                        // If this failed, return the invalid command message
                        invalidCommand();
                        break;
                    }

                    // There's only one node we can match arguments for
                    nextNodeIndex = 0;
                }

                // Is the selected node a leaf?
                ModCommand selected = current.get(nextNodeIndex);
                if (selected.isLeaf()) {
                    // If we still have unparsed arguments, this command should fail.
                    if (parser.hasNext()) {
                        invalidCommand();
                        break;
                    }

                    // Otherwise, we can execute the command
                    Map<String, Object> parsedArgs = parser.parsedArgs;
                    CommandContext context = new CommandContext(parsedArgs);
                    selected.action.execute(context);
                    break;
                }

                // If it's an internal node, then get the children and continue
                current = selected.children;
            }

            ci.cancel();
        }
        else {
            // This is not a command, so modify the phrases and counters based on the player's mod settings. I will
            // assume that the player (only me) is tracking reasonable phrases because I'll just be calling the
            // fixMisspellings function on the updated string every time. This is a personal mod after all.

            // For each tracked phrase, autocorrect and find the starting indices of each instance of the phrases.
            for (String phrase: CounterMod.configManager.getConfig().phrases) {
                List<Integer> phraseIndices;

                // Only autocorrect if the player enabled it
                if(CounterMod.configManager.getConfig().enableAutocorrect) {
                    // Autocorrect misspellings in the message
                    Map<String, Object> result = LeviathanDistance.fixMisspellings(content, phrase, 2);

                    // Update the content with the fixed phrases
                    content = (String) result.get("fixedText");

                    // Get the indices of the fixed phrases
                    phraseIndices = (List<Integer>) result.get("fixedIndices");
                }
                else {
                    phraseIndices = LeviathanDistance.findPhraseIndices(content, phrase);
                }

                // Add the counters for the phrase
                // Get a unique identifier for the server the player is on
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

                // This should not happen, but check if there even is a hashmap of servers
                if (CounterMod.configManager.getConfig().counters == null) {
                    CounterMod.configManager.getConfig().counters = new HashMap<>();
                }

                // Are we already tracking this server (or single player)?
                if (!CounterMod.configManager.getConfig().counters.containsKey(uuid)) {
                    // If not, create a new hashmap to track counters
                    CounterMod.configManager.getConfig().counters.put(uuid, new HashMap<>());
                }

                // Are we already tracking counters for this phrase on this server?
                if (!CounterMod.configManager.getConfig().counters.get(uuid).containsKey(phrase)) {
                    CounterMod.configManager.getConfig().counters.get(uuid).put(phrase, 0);
                }

                // Should not happen, but if the counter is negative, set it to 0
                if(CounterMod.configManager.getConfig().counters.get(uuid).get(phrase) < 0) {
                    CounterMod.configManager.getConfig().counters.get(uuid).put(phrase, 0);
                }

                // Update the counter for this phrase on this server
                int counter = CounterMod.configManager.getConfig().counters.get(uuid).get(phrase) + phraseIndices.size();
                CounterMod.configManager.getConfig().counters.get(uuid).put(phrase, counter);

                // Append the counter after each phrase backwards (the indices should already be sorted in ascending order)
                StringBuilder builder = new StringBuilder(content);
                for (int i = phraseIndices.size() - 1; i >= 0; i--) {
                    int index = phraseIndices.get(i) + phrase.length();
                    builder.insert(index, " X" + counter);
                    counter --;
                }
                content = builder.toString();

                // Save to config
                CounterMod.saveConfig();
            }

            // Send the modified content
            Instant instant = Instant.now();
            long l = NetworkEncryptionUtils.SecureRandomUtil.nextLong();
            LastSeenMessagesCollector.LastSeenMessages lastSeenMessages = this.lastSeenMessagesCollector.collect();
            MessageSignatureData messageSignatureData = this.messagePacker.pack(new MessageBody(content, instant, l, lastSeenMessages.lastSeen()));

            MinecraftClient client = MinecraftClient.getInstance();
            if(client.getNetworkHandler() != null) {
                client.getNetworkHandler().sendPacket(
                        new ChatMessageC2SPacket(content, instant, l, messageSignatureData, lastSeenMessages.update())
                );
            }

            // Prevent sending two messages in chat
            ci.cancel();
        }
    }

    // Handles invalid commands
    private void invalidCommand() {
        // Message the player telling them of the invalid command
        ClientPlayerEntity player =  MinecraftClient.getInstance().player;
        MutableText message = Text.literal("Invalid command. Type .help for a list of commands.").styled(style -> style.withColor(Formatting.RED));
        player.sendMessage(message, false);
    }
}
