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
    public boolean isDebugEnabled() {
        return config.getBoolean("debug", false);
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

        // MySQL defaults
        config.addDefault("mysql.enabled", false);
        config.addDefault("mysql.host", "localhost");
        config.addDefault("mysql.port", 3306);
        config.addDefault("mysql.database", "minecraft_logs");
        config.addDefault("mysql.username", "root");
        config.addDefault("mysql.password", "password");
        config.addDefault("mysql.table", "discord_logs");
        config.addDefault("debug", false);

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

    // MySQL getters
    public boolean isMySQLEnabled() {
        return config.getBoolean("mysql.enabled");
    }

    public String getMySQLHost() {
        return config.getString("mysql.host");
    }

    public int getMySQLPort() {
        return config.getInt("mysql.port");
    }

    public String getMySQLDatabase() {
        return config.getString("mysql.database");
    }

    public String getMySQLUsername() {
        return config.getString("mysql.username");
    }

    public String getMySQLPassword() {
        return config.getString("mysql.password");
    }

    public String getMySQLTable() {
        return config.getString("mysql.table");
    }
}