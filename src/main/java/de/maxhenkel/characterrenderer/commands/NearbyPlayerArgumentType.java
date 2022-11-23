package de.maxhenkel.characterrenderer.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

public class NearbyPlayerArgumentType implements ArgumentType<String> {

    public static final DynamicCommandExceptionType ERROR_UNKNOWN_ENTITY = new DynamicCommandExceptionType((object) -> Component.translatable("message.characterrenderer.unknown_player", object));

    private static final Minecraft mc = Minecraft.getInstance();

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        if (mc.player == null) {
            return reader.readString();
        }
        mc.player.connection.getOnlinePlayers().stream().filter(player -> player.getProfile().getName().equals(reader.getRemaining())).findFirst().orElseThrow(() -> ERROR_UNKNOWN_ENTITY.create(reader.getRemaining()));
        return reader.readString();
    }

    public static PlayerInfo player(String name, CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        String playerName = context.getArgument(name, String.class);
        return mc.player.connection.getOnlinePlayers().stream().filter(player -> player.getProfile().getName().equals(playerName)).findFirst().orElseThrow(() -> ERROR_UNKNOWN_ENTITY.create(playerName));
    }

    public static NearbyPlayerArgumentType id() {
        return new NearbyPlayerArgumentType();
    }

}
