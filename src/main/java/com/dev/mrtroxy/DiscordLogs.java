package com.dev.mrtroxy;

import com.dev.mrtroxy.config.ConfigManager;
import com.dev.mrtroxy.listeners.ChestListener;
import com.dev.mrtroxy.utils.LangManager;
import org.bukkit.plugin.java.JavaPlugin;

public class DiscordLogs extends JavaPlugin {

    private static DiscordLogs instance;
    private ConfigManager configManager;
    private LangManager langManager;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.langManager = new LangManager(this);

        // Register listeners
        getServer().getPluginManager().registerEvents(new ChestListener(this), this);

        // Load configurations
        configManager.loadConfig();
        langManager.loadLanguages();

        getLogger().info("DiscordLogs has been enabled!");
    }

    @Override
    public void onDisable() {
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
}