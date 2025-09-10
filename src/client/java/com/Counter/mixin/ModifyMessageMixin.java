package com.Counter.mixin;

import com.Counter.CounterMod;
import com.Counter.command.CommandContext;
import com.Counter.command.CommandParser;
import com.Counter.command.ModCommand;
import com.Counter.command.ModCommandRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Mixin(ClientPlayNetworkHandler.class)
public class ModifyMessageMixin {
    @Inject(
            method = "sendChatMessage",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onSendChatMessage(String content, CallbackInfo ci) {
        // Create a command parser
        CommandParser parser = new CommandParser(content);

        // Is this a command?
        if (parser.isCommand()) {
            // Get the ModCommand tree
            ArrayList<ModCommand> current = CounterMod.CommandRegistry.COMMANDS;

            // If this is empty, just return the invalid command message.
            if (current == null || current.isEmpty()) {
                invalidCommand();
                ci.cancel();
            }

            // Keep looping until we reach a leaf node. The leaf nodes are assumed
            // to not have children because of the validation checker.
            while (true) {
                // We expect another argument
                if (!parser.hasNext()) {
                    // Invalid command because we didn't get another argument
                    invalidCommand();
                    ci.cancel();
                }

                // Check if this list of nodes is a list of literals or a single non-literal node
                // Note: If a node is non-literal, it must be the only node in the list. If a node
                //       in the list is literal, then all the nodes must be literal.
                int nextNodeIndex = -1;
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
                        ci.cancel();
                    }

                    // There's only one node we can match arguments for
                    nextNodeIndex = 0;
                } else {
                    // Non-literal types can be parsed immediately.
                    ModCommand.ArgType type = current.get(0).type;
                    String argName = current.get(0).name;
                    if (!parser.processNextArg(type, argName)) {
                        // If this failed, return the invalid command message
                        invalidCommand();
                        ci.cancel();
                    }

                    // The matched index should be stored by the parser at this point
                    nextNodeIndex = parser.literalIndex;
                }

                // Is the selected node a leaf?
                ModCommand selected = current.get(nextNodeIndex);
                if (selected.isLeaf()) {
                    // If we still have unparsed arguments, this command should fail.
                    if (parser.hasNext()) {
                        invalidCommand();
                        ci.cancel();
                    }

                    // Otherwise, we can execute the command
                    Map<String, Object> parsedArgs = parser.parsedArgs;
                    CommandContext context = new CommandContext(parsedArgs);
                    selected.action.execute(context);
                    ci.cancel();
                }

                // If it's an internal node, then get the children and continue
                current = selected.children;
            }
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
