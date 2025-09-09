package com.Counter.mixin;

import com.Counter.CounterMod;
import com.Counter.command.ModCommand;
import com.Counter.command.ModCommandRegistry;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Mixin(ChatInputSuggestor.class)
public abstract class ModInputSuggestorMixin {
    @Shadow @Nullable private CompletableFuture<?> pendingSuggestions;
    @Shadow private TextFieldWidget textField;

    @Inject(method = "refresh", at = @At("HEAD"), cancellable = true)
    private void refreshDotCommands(CallbackInfo ci) {
        // Get player text in the chat
        String text = this.textField.getText();
        int cursor = this.textField.getCursor();

        // Make sure this is a mod command
        if (text.startsWith(".")) {
            // Strip the dot
            String typed = text.substring(1, cursor);

            // Start from the beginning of the command
            String[] parts = typed.split(" ");
            int argumentIndex = 0;
            String currentArgument = parts[argumentIndex];

            // Builder and suggestions
            SuggestionsBuilder builder = new SuggestionsBuilder(typed, typed.lastIndexOf(' ') + 1);
            Collection<String> suggestions;

            // Loop through all the arguments
            ArrayList<ModCommand> nodes = CounterMod.modCommandRegistry.COMMANDS;
            while (true) {
                // Does our current argument match any of the names of the current commands?
                int index = ModCommandRegistry.findMatchIndex(nodes, currentArgument);
//                for (String part: parts) {
//                    System.out.println(part);
//                }
//                System.out.println();
                if (index <= -1) {
                    // Suggest the current commands
                    suggestions = ModCommandRegistry.createSuggestions(nodes);
                    break;
                }
                else {
                    // We found a match
                    argumentIndex ++;

                    // Different cases for going down the tree
                    if (argumentIndex < parts.length) {
                        // Go down the command tree if the player typed another argument
                        nodes = nodes.get(index).children;
                        currentArgument = parts[argumentIndex];
                    }
                    else if (typed.endsWith(" ")) {
                        // If the player typed a space at the end of the text, then suggest the next set of commands
                        nodes = nodes.get(index).children;
                        suggestions = ModCommandRegistry.createSuggestions(nodes);
                        break;
                    }
                    else {
                        // Player didn't type an additional argument, so keep suggesting the current command
                        suggestions = ModCommandRegistry.createSuggestions(nodes);
                        break;
                    }
                }
            }

            this.pendingSuggestions = CommandSource.suggestMatching(suggestions, builder);

            // Trigger display like vanilla does
            this.pendingSuggestions.thenRun(() -> {
                if (this.pendingSuggestions.isDone()) {
                    ((ChatInputSuggestor)(Object)this).show(false);
                }
            });

            // Cancel the remaining vanilla logic
            ci.cancel();
        }
    }
}
