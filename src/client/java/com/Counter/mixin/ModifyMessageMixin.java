package com.Counter.mixin;

import com.Counter.command.CommandParser;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ModifyMessageMixin {
    @Inject(
            method = "sendChatMessage",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onSendChatMessage(String content, CallbackInfo ci) {
        // Is this a command?
        if (CommandParser.isCommand(content)) {
            // Parse the command
            CommandParser.parseCommand(content);

            // Don't send this message into the chat
            ci.cancel();
        }
    }
}
