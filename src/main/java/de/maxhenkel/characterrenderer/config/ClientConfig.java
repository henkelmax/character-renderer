package de.maxhenkel.characterrenderer.config;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.configbuilder.ConfigEntry;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientConfig {

    public final ConfigEntry<String> saveFolder;
    public final ConfigEntry<Integer> renderWidth;
    public final ConfigEntry<Integer> renderHeight;

    public ClientConfig(ConfigBuilder builder) {
        saveFolder = builder.stringEntry("save_folder", "");
        renderWidth = builder.integerEntry("render_width", 2000, 16, 16000);
        renderHeight = builder.integerEntry("render_height", 2000, 16, 16000);
    }

    public Path getSaveFolder() {
        String saveFolderStr = saveFolder.get();
        if (!saveFolderStr.isEmpty()) {
            return Paths.get(saveFolderStr);
        }
        return FabricLoader.getInstance().getGameDir().resolve("character_renderer");
    }

}
