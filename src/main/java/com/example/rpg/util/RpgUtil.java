package com.example.rpg.util;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class RpgUtil {

    public static int getConfigInt(FileConfiguration config, String path, int def) {
        return config.isInt(path) ? config.getInt(path) : def;
    }

    public static int getIntOrDefault(ConfigurationSection section, String path, int def) {
        int value;
        if (section.isInt(path)) {
            value = section.getInt(path);
        } else {
            value = def;
        }

        return value;
    }
}
