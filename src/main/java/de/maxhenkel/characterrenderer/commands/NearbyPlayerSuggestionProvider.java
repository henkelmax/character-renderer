package de.maxhenkel.characterrenderer.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class NearbyPlayerSuggestionProvider implements SuggestionProvider<FabricClientCommandSource> {

    private static final Minecraft mc = Minecraft.getInstance();

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return suggest(mc.player.connection.getOnlinePlayers().stream().map(playerInfo -> playerInfo.getProfile().getName()), builder);
    }

    private static CompletableFuture<Suggestions> suggest(Stream<String> stream, SuggestionsBuilder suggestionsBuilder) {
        String string = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
        stream.filter((name) -> matchesSubStr(string, name.toLowerCase(Locale.ROOT))).forEach(suggestionsBuilder::suggest);
        return suggestionsBuilder.buildFuture();
    }

    private static boolean matchesSubStr(String string, String string2) {
        for (int i = 0; !string2.startsWith(string, i); ++i) {
            i = string2.indexOf(95, i);
            if (i < 0) {
                return false;
            }
        }

        return true;
    }

    public static NearbyPlayerSuggestionProvider id() {
        return new NearbyPlayerSuggestionProvider();
    }

}
