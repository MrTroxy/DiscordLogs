package com.dev.mrtroxy.config;

import com.dev.mrtroxy.DiscordLogs;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigManager {

    private final DiscordLogs plugin;
    private FileConfiguration config;
    private File configFile;

    public ConfigManager(DiscordLogs plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        setDefaults();
    }

    private void setDefaults() {
        config.addDefault("webhook-url", "YOUR_WEBHOOK_URL");
        config.addDefault("language", "english");
        config.addDefault("logging.chest-interactions", true);
        config.options().copyDefaults(true);
        saveConfig();
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getWebhookUrl() {
        return config.getString("webhook-url");
    }

    public String getLanguage() {
        return config.getString("language");
    }

    public boolean isChestLoggingEnabled() {
        return config.getBoolean("logging.chest-interactions");
    }
}