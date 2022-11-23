package de.maxhenkel.characterrenderer.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;

public class DummyPlayer extends RemotePlayer {

    private byte model;

    public DummyPlayer(GameProfile gameProfile, Player toCopy) {
        this(gameProfile);
        model = toCopy.getEntityData().get(DATA_PLAYER_MODE_CUSTOMISATION);

        Inventory toCopyInventory = toCopy.getInventory();
        Inventory inventory = getInventory();

        for (int i = 0; i < toCopyInventory.items.size(); i++) {
            inventory.items.set(i, toCopyInventory.items.get(i).copy());
        }
        for (int i = 0; i < toCopyInventory.offhand.size(); i++) {
            inventory.offhand.set(i, toCopyInventory.offhand.get(i).copy());
        }
        for (int i = 0; i < toCopyInventory.armor.size(); i++) {
            inventory.armor.set(i, toCopyInventory.armor.get(i).copy());
        }
        inventory.selected = toCopyInventory.selected;
    }

    public DummyPlayer(GameProfile gameProfile) {
        super(Minecraft.getInstance().level, gameProfile, null);
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
