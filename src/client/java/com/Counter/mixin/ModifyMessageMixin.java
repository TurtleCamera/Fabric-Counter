package com.Counter.mixin;

import com.Counter.utils.CommandHandler;
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
        if (CommandHandler.isCommand(content)) {
            // Parse the command
            CommandHandler.parseCommand(content);

            // Don't send this message into the chat
            ci.cancel();
        }
    }
}
