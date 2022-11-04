package de.maxhenkel.characterrenderer.config;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.configbuilder.ConfigEntry;

public class ClientConfig {

    public final ConfigEntry<String> saveFolder;

    public ClientConfig(ConfigBuilder builder) {
        saveFolder = builder.stringEntry("save_folder", "");
    }

}
