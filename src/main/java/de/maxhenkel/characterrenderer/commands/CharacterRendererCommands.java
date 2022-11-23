package de.maxhenkel.characterrenderer.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.maxhenkel.characterrenderer.entity.DummyPlayer;
import de.maxhenkel.characterrenderer.gui.CharacterRendererScreen;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class CharacterRendererCommands {

    public static void init(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        Minecraft mc = Minecraft.getInstance();
        LiteralArgumentBuilder<FabricClientCommandSource> builder = ClientCommandManager.literal("characterrenderer").then(ClientCommandManager.argument("player", NearbyPlayerArgumentType.id()).suggests(NearbyPlayerSuggestionProvider.id()).executes(context -> {
            PlayerInfo player = NearbyPlayerArgumentType.player("player", context);

            Player playerByUUID = mc.level.getPlayerByUUID(player.getProfile().getId());

            DummyPlayer dummyPlayer;

            if (playerByUUID == null) {
                dummyPlayer = new DummyPlayer(player.getProfile());
                context.getSource().sendFeedback(Component.translatable("message.characterrenderer.player_too_far_away", player.getProfile().getName()));
            } else {
                dummyPlayer = new DummyPlayer(player.getProfile(), playerByUUID);
            }

            new Thread(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    return;
                }
                mc.execute(() -> mc.setScreen(new CharacterRendererScreen(dummyPlayer)));
            }).start();
            return 1;
        }));
        dispatcher.register(builder);
    }

}
