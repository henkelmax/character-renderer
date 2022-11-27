package de.maxhenkel.characterrenderer.mixin;

import de.maxhenkel.characterrenderer.commands.EntitySuggestionProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    private boolean initialized;

    @Inject(method = "setLevel", at = @At("RETURN"))
    private void renderCharacters(ClientLevel level, CallbackInfo ci) {
        if (initialized) {
            return;
        }
        if (level != null) {
            EntitySuggestionProvider.initEntityTypes(level);
            initialized = true;
        }
    }

}
