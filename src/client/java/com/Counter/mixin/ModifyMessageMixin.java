package com.Counter.mixin;

import com.Counter.CounterMod;
import com.Counter.command.CommandContext;
import com.Counter.command.CommandParser;
import com.Counter.command.ModCommand;
import com.Counter.command.ModCommandRegistry;
import com.Counter.utils.PhraseUtils;
import com.Counter.utils.Tuple;
import com.Counter.utils.UUIDHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.util.Crypt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Instant;
import java.util.*;

@Mixin(ClientPacketListener.class)
public class ModifyMessageMixin {
    // Shadow private variables, so I can use them
    @Shadow
    private LastSeenMessagesTracker lastSeenMessages;
    @Shadow
    private SignedMessageChain.Encoder signedMessageEncoder;

    // Maximum length per chat packet
    final int MAX_CHAT_LENGTH = 256;

    @Inject(
            method = "sendChat",
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
                    // If we still have unparsed arguments, then this command should fail.
                    if (parser.hasNext()) {
                        invalidCommand();
                        break;
                    }
                }

                // Do the arguments stop here?
                if (!parser.hasNext()) {
                    // Does this selected node have an executable?
                    if (selected.action == null) {
                        // Invalid command if it doesn't have one
                        invalidCommand();
                        break;
                    }

                    // We can execute the command
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
            // This is not a command. Only count phrases if enabled.
            if(!CounterMod.configManager.getConfig().enable) {
                return;
            }

            // modify the phrases and counters based on the player's mod settings.
            String uuid = UUIDHandler.getUUID(); // Server or single-player UUID

            // Replace all shortcuts with their corresponding phrases
            for (Tuple<String, String> tuple : CounterMod.configManager.getConfig().shortcuts) {
                String phrase = tuple.first();
                String shortcut = tuple.second();
                content = PhraseUtils.replaceShortcut(content, phrase, shortcut);
            }

            boolean cancelAppend = false; // Whether to skip the append phrase
            Map<String, Integer> serverCounters = CounterMod.configManager.getConfig().counters.get(uuid);

            for (String phrase : CounterMod.configManager.getConfig().phrases) {
                // Find all exact occurrences of the phrase
                List<Integer> phraseIndices = PhraseUtils.findPhraseIndices(content, phrase);

                // Cancel append if the last phrase ends at the end of the content
                if (!phraseIndices.isEmpty()) {
                    int endIndex = phraseIndices.get(phraseIndices.size() - 1) + phrase.length();
                    String endContent = content.substring(endIndex);
                    if (PhraseUtils.isAllPunctuation(endContent)) {
                        cancelAppend = true;
                    }
                }

                // Ensure server counters exist
                CounterMod.configManager.getConfig().performCountersChecks(phrase, true);

                // Update counter for this phrase on this server
                int counter = serverCounters.get(phrase) + phraseIndices.size();
                serverCounters.put(phrase, counter);

                // Insert counters after each phrase (backwards to avoid index shift)
                StringBuilder builder = new StringBuilder(content);
                for (int i = phraseIndices.size() - 1; i >= 0; i--) {
                    int index = phraseIndices.get(i) + phrase.length();
                    builder.insert(index, " X" + counter);
                    counter--;
                }
                content = builder.toString();
            }

            // Skip append if sentence fully enclosed or all punctuation
            if ((content.startsWith("(") && content.endsWith(")")) ||
                    (content.startsWith("[") && content.endsWith("]")) ||
                    (content.startsWith("{") && content.endsWith("}")) ||
                    PhraseUtils.isAllPunctuation(content)) {
                cancelAppend = true;
            }

            // Skip append if sentence starts with a tracked phrase
            for (String phrase : CounterMod.configManager.getConfig().phrases) {
                if (content.toLowerCase().startsWith(phrase.toLowerCase())) {
                    cancelAppend = true;
                    break;
                }
            }

            // Append the player's append phrase if allowed
            if (!cancelAppend) {
                String append = CounterMod.configManager.getConfig().appendPhrase;
                if (append != null) {
                    CounterMod.configManager.getConfig().performCountersChecks(append, true);
                    int counter = serverCounters.get(append) + 1;
                    serverCounters.put(append, counter);

                    String appendText = ", " + append + " X" + counter;
                    int insertIndex = content.length();

                    // Walk backwards over punctuation that should be preserved after append
                    while (insertIndex > 0) {
                        char c = content.charAt(insertIndex - 1);

                        // Only push insertion BEFORE . ! ?
                        if (c == '.' || c == '!' || c == '?') {
                            insertIndex--;
                        }
                        // For everything else (like ), ], }, commas), stop here
                        else if (!Character.isLetterOrDigit(c) && !Character.isWhitespace(c)) {
                            break;
                        } else {
                            break;
                        }
                    }

                    content = content.substring(0, insertIndex) + appendText + content.substring(insertIndex);
                }
            }

            // Save updated config
            CounterMod.saveConfig();

            // Split content into chunks
            for (int start = 0; start < content.length(); start += MAX_CHAT_LENGTH) {
                int end = Math.min(start + MAX_CHAT_LENGTH, content.length());
                String chunk = content.substring(start, end);

                Instant instantChunk = Instant.now();
                long lChunk = Crypt.SaltSupplier.getLong();
                LastSeenMessagesTracker.Update updateChunk = this.lastSeenMessages.generateAndApplyUpdate();
                MessageSignature messageSignatureChunk = this.signedMessageEncoder.pack(
                        new SignedMessageBody(chunk, instantChunk, lChunk, updateChunk.lastSeen())
                );

                ClientPacketListener clientPacketListener = (ClientPacketListener) (Object) this;
                clientPacketListener.send(
                        new ServerboundChatPacket(chunk, instantChunk, lChunk, messageSignatureChunk, updateChunk.update())
                );
            }

            // Cancel the original message send
            ci.cancel();
        }
    }

    // Handles invalid commands
    private void invalidCommand() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.sendSystemMessage(Component.literal("Invalid command. Type .help for a list of commands.")
                            .withStyle(ChatFormatting.RED));
        }
    }
}
