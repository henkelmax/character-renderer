package de.maxhenkel.characterrenderer.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.maxhenkel.characterrenderer.entity.DummyPlayer;
import de.maxhenkel.characterrenderer.entity.EntityUtils;
import de.maxhenkel.characterrenderer.gui.CharacterRendererScreen;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class CharacterRendererCommands {

    private static final Minecraft mc = Minecraft.getInstance();

    public static void init(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        LiteralArgumentBuilder<FabricClientCommandSource> characterrenderer = ClientCommandManager.literal("characterrenderer");

        characterrenderer.then(ClientCommandManager.literal("player").then(ClientCommandManager.argument("player", NearbyPlayerArgumentType.id()).suggests(NearbyPlayerSuggestionProvider.id()).executes(context -> {
            PlayerInfo player = NearbyPlayerArgumentType.player("player", context);

            Player playerByUUID = mc.level.getPlayerByUUID(player.getProfile().getId());

            Player dummyPlayer;

            if (playerByUUID == null) {
                dummyPlayer = new DummyPlayer(player.getProfile());
                context.getSource().sendFeedback(Component.translatable("message.characterrenderer.player_too_far_away", player.getProfile().getName()));
            } else {
                dummyPlayer = playerByUUID;
            }

            showGuiDelayed(dummyPlayer);

            return 1;
        })));

        characterrenderer.then(ClientCommandManager.literal("view").executes(context -> {
            if (!(mc.crosshairPickEntity instanceof LivingEntity lookingAt)) {
                context.getSource().sendError(Component.translatable("message.characterrenderer.not_looking_at_entity"));
                return 1;
            }

            showGuiDelayed(lookingAt);
            return 1;
        }));

        dispatcher.register(characterrenderer);
    }

    private static void showGuiDelayed(LivingEntity entity) {
        new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return;
            }
            LivingEntity renderEntity = EntityUtils.cloneEntity(entity);
            mc.execute(() -> mc.setScreen(new CharacterRendererScreen(renderEntity)));
        }).start();
    }

}
