package de.maxhenkel.characterrenderer.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class EntityUtils {

    public static LivingEntity cloneEntity(LivingEntity entity) {
        if (entity instanceof Player player) {
            if (!(entity instanceof DummyPlayer)) {
                return new DummyPlayer(player.getGameProfile(), player);
            }
        }
        return cloneLivingEntity(entity);
    }

    private static LivingEntity cloneLivingEntity(LivingEntity entity) {
        CompoundTag data = new CompoundTag();
        entity.saveWithoutId(data);

        LivingEntity clone = (LivingEntity) entity.getType().create(entity.level);

        clone.load(data);

        return clone;
    }

}
