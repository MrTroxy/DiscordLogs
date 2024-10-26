package com.dev.mrtroxy.utils;

import com.dev.mrtroxy.DiscordLogs;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LangManager {

    private final DiscordLogs plugin;
    private final Map<String, YamlConfiguration> languages;
    private YamlConfiguration currentLanguage;

    public LangManager(DiscordLogs plugin) {
        this.plugin = plugin;
        this.languages = new HashMap<>();
    }

    public void loadLanguages() {
        File langFolder = new File(plugin.getDataFolder(), "langs");
        if (!langFolder.exists()) {
            langFolder.mkdir();
            saveDefaultLanguages();
        }

        // Load all language files
        File[] langFiles = langFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (langFiles != null) {
            for (File file : langFiles) {
                String langName = file.getName().replace(".yml", "");
                languages.put(langName, YamlConfiguration.loadConfiguration(file));
            }
        }

        // Set current language
        String configLang = plugin.getConfigManager().getLanguage();
        currentLanguage = languages.getOrDefault(configLang, languages.get("english"));
    }

    private void saveDefaultLanguages() {
        saveResource("english.yml");
        saveResource("french.yml");
        saveResource("spanish.yml");
    }

    private void saveResource(String fileName) {
        if (!new File(plugin.getDataFolder() + "/langs", fileName).exists()) {
            plugin.saveResource("langs/" + fileName, false);
        }
    }

    public String getMessage(String key) {
        return currentLanguage.getString(key, key);
    }
}