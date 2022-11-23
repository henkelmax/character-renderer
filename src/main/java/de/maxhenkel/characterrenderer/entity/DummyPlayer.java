package de.maxhenkel.characterrenderer.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;

public class DummyPlayer extends RemotePlayer {

    private byte model;

    public DummyPlayer(GameProfile gameProfile, Player toCopy) {
        super(Minecraft.getInstance().level, gameProfile, null);
        model = toCopy.getEntityData().get(DATA_PLAYER_MODE_CUSTOMISATION);
    }

    public void showPart(PlayerModelPart part, boolean show) {
        if (show) {
            model |= part.getMask();
        } else {
            model &= ~part.getMask();
        }
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean shouldShowName() {
        return false;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public boolean isModelPartShown(PlayerModelPart part) {
        return (model & part.getMask()) == part.getMask();
    }

}
