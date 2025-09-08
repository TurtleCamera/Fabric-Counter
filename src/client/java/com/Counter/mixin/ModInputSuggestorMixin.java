package com.Counter.mixin;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Mixin(ChatInputSuggestor.class)
public abstract class ModInputSuggestorMixin {
    @Shadow
    @Nullable
    private CompletableFuture<Suggestions> pendingSuggestions;

    @Shadow
    private TextFieldWidget textField;

    @Inject(method = "refresh", at = @At("HEAD"), cancellable = true)
    private void refreshDotCommands(CallbackInfo ci) {
        String text = this.textField.getText();
        int cursor = this.textField.getCursor();

        if (text.startsWith(".")) {
            // Strip the dot
            String typed = text.substring(1, cursor);

            // Split into "command" and "args"
            String[] parts = typed.split(" ", 2);
            String command = parts[0];
            String args = parts.length > 1 ? parts[1] : "";

            SuggestionsBuilder builder = new SuggestionsBuilder(typed, typed.lastIndexOf(' ') + 1);

            Collection<String> suggestions;

            switch (command) {
                case "warp" -> {
                    // If there's no arg yet, suggest destinations
                    suggestions = Arrays.asList("base", "nether", "end");
                }
                case "home" -> {
                    suggestions = Arrays.asList("set", "delete", "list");
                }
                default -> {
                    // Top-level commands
                    suggestions = Arrays.asList("warp", "home", "tpa", "heal", "fly");
                }
            }

            this.pendingSuggestions = CommandSource.suggestMatching(suggestions, builder);

            // Trigger display like vanilla does
            this.pendingSuggestions.thenRun(() -> {
                if (this.pendingSuggestions.isDone()) {
                    ((ChatInputSuggestor)(Object)this).show(false);
                }
            });

            ci.cancel();
        }
    }
}
