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
public abstract class ModifySuggestionsMixin {
    @Shadow @Nullable private CompletableFuture<?> pendingSuggestions;
    @Shadow private TextFieldWidget textField;

    @Inject(method = "refresh", at = @At("HEAD"), cancellable = true)
    private void refreshDotCommands(CallbackInfo ci) {
        // Get player text in the chat
        String text = this.textField.getText();
        int cursor = this.textField.getCursor();

        // Make sure this is a mod command
        if (text.startsWith(".")) {
            // Edge case: Typing an extra space where you shouldn't should stop all suggestions
            // TODO: This should display an error while also disabling tab completion. Perhaps
            //       consider figuring this out in the future when you have the time. Note that
            //       greedy arguments should take in double spaces. The logic here is still
            //       correct because typing anything, including double spaces, for the greedy
            //       argument (or any argument) should stop suggesting. The parser will consider
            //       the double spaces.
            if (text.startsWith(". ") || text.contains("  ")) {
                return;
            }

            // Only consider the part up to the cursor
            String typed = text.substring(0, cursor);

            // Start from the beginning of the command
            String[] parts = typed.split(" ");
            int argumentIndex = 0;
            String currentArgument = parts[argumentIndex];

            // Builder and suggestions
            Collection<String> suggestions = new ArrayList<>();

            // Loop through all the arguments
            ArrayList<ModCommand> nodes = CounterMod.CommandRegistry.COMMANDS;
            boolean cancelled = false;  // In the vanilla behavior, there are some edge cases where nothing is suggested
            while (true) {
                // If the list is empty, then there is nothing to suggest
                if (nodes.isEmpty()) {
                    cancelled = true;
                    break;
                }

                // Does our current argument match any of the names of the current commands or is a greedy argument?
                int index = ModCommandRegistry.findMatchIndex(nodes, currentArgument);
                if (index <= -1) {
                    // Suggest the current commands that match with the prefix so far
                    suggestions = ModCommandRegistry.createSuggestions(nodes, parts[argumentIndex]);

                    // Edge cases for cancelling the suggestions
                    if (suggestions.isEmpty()) {
                        // Don't suggest anything if the player mistyped a command (doesn't
                        // apply if the player partially typed a command correctly).
                        cancelled = true;
                    }
                    else if (nodes.get(0).type == ModCommand.ArgType.GREEDY && parts[argumentIndex].length() > 0) {
                        // If this is a greedy argument and the player typed something, don't suggest anything
                        cancelled = true;
                    }
                    else if (nodes.get(0).type == ModCommand.ArgType.LITERAL && typed.endsWith(" ")) {
                        // If this is a literal argument and the player typed a space afterward, don't suggest
                        // anything even if the argument was typed partially correctly
                        cancelled = true;
                    }

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
                    else if (!typed.endsWith(" ")) {
                        // According to vanilla's behavior, the suggestor's behavior changes if they correctly
                        // typed all parts of the command up to the end correctly. If the last argument is
                        // a literal and there's no space in the end, the vanilla code suggests nothing.
                        cancelled = true;
                        break;
                    }
                    else {
                        // There's a space at the end, so suggest the next set of commands
                        nodes = nodes.get(index).children;
                        suggestions = ModCommandRegistry.createSuggestions(nodes, "");  // Suggest all commands using no prefix
                        break;
                    }
                }
            }

            if (!cancelled) {
                SuggestionsBuilder builder = new SuggestionsBuilder(typed, typed.lastIndexOf(' ') + 1);
                this.pendingSuggestions = CommandSource.suggestMatching(suggestions, builder);

                // Trigger display like vanilla does
                this.pendingSuggestions.thenRun(() -> {
                    if (this.pendingSuggestions.isDone()) {
                        ((ChatInputSuggestor) (Object) this).show(false);
                    }
                });

                // Cancel the remaining vanilla logic
                ci.cancel();
            }
        }
    }
}
