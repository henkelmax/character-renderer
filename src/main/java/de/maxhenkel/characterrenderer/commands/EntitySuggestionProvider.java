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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class EntitySuggestionProvider implements SuggestionProvider<FabricClientCommandSource> {

    private static final Map<EntityType<?>, Class<?>> ENTITY_CLASSES = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return SharedSuggestionProvider.suggestResource(ENTITY_CLASSES.entrySet().stream().filter(e -> e.getKey().canSummon()).filter(e -> LivingEntity.class.isAssignableFrom(e.getValue())).map(Map.Entry::getKey), builder, EntityType::getKey, (entityType) -> {
            return Component.translatable(Util.makeDescriptionId("entity", EntityType.getKey(entityType)));
        });
    }

    public static EntitySuggestionProvider id() {
        return new EntitySuggestionProvider();
    }

    public static void initEntityTypes(Level level) {
        ENTITY_CLASSES.clear();
        Registry.ENTITY_TYPE.stream().forEach((entityType) -> {
            Entity e = entityType.create(level);
            if (e == null) {
                return;
            }
            ENTITY_CLASSES.put(entityType, e.getClass());
        });
    }

}
