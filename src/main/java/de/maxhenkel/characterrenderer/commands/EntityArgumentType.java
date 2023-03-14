package de.maxhenkel.characterrenderer.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

public class EntityArgumentType implements ArgumentType<ResourceLocation> {

    public static final DynamicCommandExceptionType ERROR_UNKNOWN_ENTITY = new DynamicCommandExceptionType((object) -> {
        return Component.translatable("entity.notFound", object);
    });

    public static EntityArgumentType id() {
        return new EntityArgumentType();
    }

    public static ResourceLocation getEntity(CommandContext<FabricClientCommandSource> commandContext, String string) throws CommandSyntaxException {
        return verifyCanSummon(commandContext.getArgument(string, ResourceLocation.class));
    }

    private static ResourceLocation verifyCanSummon(ResourceLocation resourceLocation) throws CommandSyntaxException {
        BuiltInRegistries.ENTITY_TYPE.getOptional(resourceLocation).filter(EntityType::canSummon).orElseThrow(() -> {
            return ERROR_UNKNOWN_ENTITY.create(resourceLocation);
        });
        return resourceLocation;
    }

    @Override
    public ResourceLocation parse(StringReader stringReader) throws CommandSyntaxException {
        return verifyCanSummon(ResourceLocation.read(stringReader));
    }
}