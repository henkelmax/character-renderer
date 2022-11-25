package de.maxhenkel.characterrenderer.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.Util;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;

import java.util.concurrent.CompletableFuture;

public class EntitySuggestionProvider implements SuggestionProvider<FabricClientCommandSource> {

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return SharedSuggestionProvider.suggestResource(Registry.ENTITY_TYPE.stream().filter(EntityType::canSummon), builder, EntityType::getKey, (entityType) -> {
            return Component.translatable(Util.makeDescriptionId("entity", EntityType.getKey(entityType)));
        });
    }

    public static EntitySuggestionProvider id() {
        return new EntitySuggestionProvider();
    }

}
