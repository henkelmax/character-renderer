package de.maxhenkel.characterrenderer.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.maxhenkel.characterrenderer.entity.DummyPlayer;
import de.maxhenkel.characterrenderer.entity.EntityUtils;
import de.maxhenkel.characterrenderer.gui.CharacterRendererScreen;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class CharacterRendererCommands {

    private static final Minecraft mc = Minecraft.getInstance();

    public static void init(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        LiteralArgumentBuilder<FabricClientCommandSource> characterrenderer = ClientCommandManager.literal("characterrenderer");

        RequiredArgumentBuilder<FabricClientCommandSource, String> playerArg = ClientCommandManager.argument("player", NearbyPlayerArgumentType.id()).suggests(NearbyPlayerSuggestionProvider.id());
        playerArg.executes(context -> {
            PlayerInfo player = NearbyPlayerArgumentType.player("player", context);
            return player(context, player, player);
        });
        playerArg.then(ClientCommandManager.argument("skin", NearbyPlayerArgumentType.id()).suggests(NearbyPlayerSuggestionProvider.id()).executes(context -> {
            return player(context, NearbyPlayerArgumentType.player("player", context), NearbyPlayerArgumentType.player("skin", context));
        }));
        characterrenderer.then(ClientCommandManager.literal("player").then(playerArg));

        characterrenderer.then(ClientCommandManager.literal("entity").then(ClientCommandManager.argument("id", EntityArgumentType.id()).suggests(EntitySuggestionProvider.id()).executes(context -> {
            ResourceLocation entityId = EntityArgumentType.getEntity(context, "id");

            LivingEntity livingEntity = EntityUtils.create(entityId);

            if (livingEntity == null) {
                context.getSource().sendError(Component.translatable("message.characterrenderer.cant_create_entity", entityId));
                return 1;
            }

            showGuiDelayed(livingEntity);
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

    private static int player(CommandContext<FabricClientCommandSource> context, PlayerInfo player, PlayerInfo skin) throws CommandSyntaxException {
        Player playerByUUID = mc.level.getPlayerByUUID(player.getProfile().getId());

        Player dummyPlayer;

        if (playerByUUID == null) {
            dummyPlayer = new DummyPlayer(skin.getProfile());
            context.getSource().sendFeedback(Component.translatable("message.characterrenderer.player_too_far_away", player.getProfile().getName()));
        } else {
            dummyPlayer = new DummyPlayer(skin.getProfile(), playerByUUID);
        }

        showGuiDelayed(dummyPlayer);

        return 1;
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
