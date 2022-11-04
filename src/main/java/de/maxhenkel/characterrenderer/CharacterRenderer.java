package de.maxhenkel.characterrenderer;

import de.maxhenkel.characterrenderer.config.ClientConfig;
import de.maxhenkel.characterrenderer.gui.CharacterRendererScreen;
import de.maxhenkel.configbuilder.ConfigBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class CharacterRenderer implements ClientModInitializer {

    public static final String MODID = "characterrenderer";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static ClientConfig CLIENT_CONFIG;

    public static KeyMapping OPEN_GUI;

    @Override
    public void onInitializeClient() {
        OPEN_GUI = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.characterrenderer.open_gui", GLFW.GLFW_KEY_C, "key.categories.misc"));

        Minecraft mc = Minecraft.getInstance();
        CLIENT_CONFIG = ConfigBuilder.build(FabricLoader.getInstance().getConfigDir().resolve(MODID).resolve("characterrenderer.properties"), ClientConfig::new);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (mc.player == null) {
                return;
            }
            if (OPEN_GUI.consumeClick()) {
                mc.setScreen(new CharacterRendererScreen());
            }
        });
    }
}
