package com.dev.mrtroxy.models;

import org.bukkit.Location;

public class LogMessage {
    private final String playerName;
    private final String action;
    private final Location location;
    private final String itemDetails;

    public LogMessage(String playerName, String action, Location location) {
        this(playerName, action, location, null);
    }

    public LogMessage(String playerName, String action, Location location, String itemDetails) {
        this.playerName = playerName;
        this.action = action;
        this.location = location;
        this.itemDetails = itemDetails;
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
}