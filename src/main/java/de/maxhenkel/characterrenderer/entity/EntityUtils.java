package de.maxhenkel.characterrenderer.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public class EntityUtils {

    public static LivingEntity cloneEntity(LivingEntity entity) {
        if (entity instanceof Player player) {
            if (entity instanceof DummyPlayer) {
                return entity;
            } else {
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

    @Nullable
    public static LivingEntity create(ResourceLocation resourceLocation) {
        CompoundTag data = new CompoundTag();
        data.putString("id", resourceLocation.toString());
        Entity entity = EntityType.loadEntityRecursive(data, Minecraft.getInstance().level, (e) -> e);
        if (entity instanceof LivingEntity livingEntity) {
            return livingEntity;
        } else {
            return null;
        }
    }

}
