package de.maxhenkel.characterrenderer.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.Util;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class SkinUtils {

    private static final Map<String, GameProfile> GAME_PROFILES = new ConcurrentHashMap<>();

    public static void getGameProfile(String name, Consumer<GameProfile> callback) {
        if (GAME_PROFILES.containsKey(name)) {
            callback.accept(GAME_PROFILES.get(name));
        } else {
            GameProfile profile = new GameProfile(Util.NIL_UUID, name);
            SkullBlockEntity.updateGameprofile(profile, (gameProfile) -> {
                GAME_PROFILES.put(name, gameProfile);
                callback.accept(gameProfile);
            });
        }
    }

}
