package com.Counter.mixin;

import com.Counter.CounterMod;
import com.Counter.command.CommandParser;
import com.Counter.command.ModCommand;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

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

            // Don't send this message into the chat
            ci.cancel();
        }
    }
}
