package de.maxhenkel.characterrenderer.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.characterrenderer.render.RenderManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderSystem.class, remap = false)
public class RenderSystemFlipFrameMixin {

    @Inject(method = "flipFrame", at = @At("HEAD"), remap = false)
    private static void renderCharacters(long l, CallbackInfo ci) {
        RenderManager.doAllRenders();
    }

}
