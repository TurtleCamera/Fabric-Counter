package com.Counter.mixin;

import com.Counter.CounterMod;
import com.Counter.command.CommandContext;
import com.Counter.command.CommandParser;
import com.Counter.command.ModCommand;
import com.Counter.command.ModCommandRegistry;
import com.Counter.utils.Autocorrect;
import com.Counter.utils.Tuple;
import com.Counter.utils.UUIDHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.message.LastSeenMessagesCollector;
import net.minecraft.network.message.MessageBody;
import net.minecraft.network.message.MessageChain;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
            // This is not a command, so modify the phrases and counters based on the player's mod settings. I will
            // assume that the player (only me) is tracking reasonable phrases because I'll just be calling the
            // fixMisspellings function on the updated string every time. This is a personal mod after all.

            // Replace all shortcuts with their corresponding phrases
            for (Tuple<String, String> tuple : CounterMod.configManager.getConfig().shortcuts) {
                // Get the phrase and its shortcut
                String phrase = tuple.first();
                String shortcut = tuple.second();

                // Replace all candidate instances of the shortcut with the phrase
                content = Autocorrect.replaceShortcut(content, phrase, shortcut);
            }

            // For each tracked phrase, autocorrect and find the starting indices of each instance of the phrases.
            boolean hasPhraseAtEnd = false; // Check if one of the tracked phrases appears at the end of the String content.
            String uuid = UUIDHandler.getUUID();    // Get a unique identifier for the server the player is on
            for (String phrase : CounterMod.configManager.getConfig().phrases) {
                List<Integer> phraseIndices;

                // Only autocorrect if the player enabled it
                if(CounterMod.configManager.getConfig().enableAutocorrect) {
                    // Autocorrect misspellings in the message
                    Map<String, Object> result = Autocorrect.fixMisspellings(content, phrase);

                    // Update the content with the fixed phrases
                    content = (String) result.get("fixedText");

                    // Get the indices of the fixed phrases
                    phraseIndices = (List<Integer>) result.get("fixedIndices");
                }
                else {
                    phraseIndices = Autocorrect.findPhraseIndices(content, phrase);
                }

                // Check if the last instance of this phrase appears at the end of the content.
                // This check must happen before we append counters.
                if (!phraseIndices.isEmpty()) {
                    int endIndex = phraseIndices.get(phraseIndices.size() - 1) + phrase.length();
                    String endContent = content.substring(endIndex);
                    if (Autocorrect.isAllPunctuation(endContent)) {
                        hasPhraseAtEnd = true;
                    }
                }

                // Add the counters for the phrase
                // Perform updates and error checks on the config's counters
                CounterMod.configManager.getConfig().performCountersChecks(phrase, true);

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
            }

            // Is no phrase at the end of the content
            if (!hasPhraseAtEnd) {
                // Does the player want to append a phrase?
                String append = CounterMod.configManager.getConfig().appendPhrase;
                if (append != null) {
                    // Append this phrase to the end of the content
                    int trailingPunctuationStart = Autocorrect.trailingPunctuationStart(content);

                    // Perform updates and error checks on the config's counters. This phrase should be in
                    // the config, but just in case.
                    CounterMod.configManager.getConfig().performCountersChecks(append, true);

                    // Increment the counter
                    int counter = CounterMod.configManager.getConfig().counters.get(uuid).get(append) + 1;
                    CounterMod.configManager.getConfig().counters.get(uuid).put(append, counter);

                    // If there is no punctuation at the end, just append the phrase to the end
                    append = ", " + append + " X" + counter;
                    if (trailingPunctuationStart == -1) {
                        content = content + append;
                    }
                    else {
                        // If there is trailing punctuation, insert before the start of the trailing punctuation
                        content = content.substring(0, trailingPunctuationStart) + append + content.substring(trailingPunctuationStart);
                    }
                }
            }

            // Save to config
            CounterMod.saveConfig();

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
