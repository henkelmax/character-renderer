package de.maxhenkel.characterrenderer.config;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.configbuilder.ConfigEntry;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientConfig {

    public final ConfigEntry<String> saveFolder;

    public ClientConfig(ConfigBuilder builder) {
        saveFolder = builder.stringEntry("save_folder", "");
    }

    public Path getSaveFolder() {
        String saveFolderStr = saveFolder.get();
        if (!saveFolderStr.isEmpty()) {
            return Paths.get(saveFolderStr);
        }
        return FabricLoader.getInstance().getGameDir().resolve("character_renderer");
    }

}
