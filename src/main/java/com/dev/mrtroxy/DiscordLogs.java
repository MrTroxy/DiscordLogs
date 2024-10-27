package com.dev.mrtroxy;

import com.dev.mrtroxy.config.ConfigManager;
import com.dev.mrtroxy.database.DatabaseManager;
import com.dev.mrtroxy.listeners.ChestListener;
import com.dev.mrtroxy.utils.LangManager;
import org.bukkit.plugin.java.JavaPlugin;

public class DiscordLogs extends JavaPlugin {

    private static DiscordLogs instance;
    private ConfigManager configManager;
    private LangManager langManager;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.langManager = new LangManager(this);

        // Load configurations
        configManager.loadConfig();
        langManager.loadLanguages();

        // Initialize database if enabled
        if (configManager.isMySQLEnabled()) {
            this.databaseManager = new DatabaseManager(this);
            if (databaseManager.connect()) {
                getLogger().info("Successfully connected to MySQL database!");
            } else {
                getLogger().warning("Failed to connect to MySQL database!");
            }
        }

        // Register listeners
        getServer().getPluginManager().registerEvents(new ChestListener(this), this);

        getLogger().info("DiscordLogs has been enabled!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
        getLogger().info("DiscordLogs has been disabled!");
    }

    public static DiscordLogs getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LangManager getLangManager() {
        return langManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}