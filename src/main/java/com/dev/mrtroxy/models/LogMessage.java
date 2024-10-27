package com.dev.mrtroxy.models;

import org.bukkit.Location;

public class LogMessage {
    private final String playerName;
    private final String action;
    private final Location location;
    private final String itemDetails;
    private final String enchantments;

    public LogMessage(String playerName, String action, Location location) {
        this(playerName, action, location, null, null);
    }

    public LogMessage(String playerName, String action, Location location, String itemDetails) {
        this(playerName, action, location, itemDetails, null);
    }

    public LogMessage(String playerName, String action, Location location, String itemDetails, String enchantments) {
        this.playerName = playerName;
        this.action = action;
        this.location = location;
        this.itemDetails = itemDetails;
        this.enchantments = enchantments;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getAction() {
        return action;
    }

    public Location getLocation() {
        return location;
    }

    public String getItemDetails() {
        return itemDetails;
    }

    public String getEnchantments() {
        return enchantments;
    }
}