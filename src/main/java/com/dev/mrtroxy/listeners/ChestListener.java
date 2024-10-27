package com.dev.mrtroxy.listeners;

import com.dev.mrtroxy.DiscordLogs;
import com.dev.mrtroxy.models.LogMessage;
import com.dev.mrtroxy.utils.DiscordWebhook;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Listener class handling chest inventory tracking and logging
 */
public class ChestListener implements Listener {

    private final DiscordLogs plugin;
    // Map to store initial chest contents: Key = Player UUID + Chest Location, Value = Chest Contents
    private final Map<String, Map<String, Integer>> initialContents = new HashMap<>();

    public ChestListener(DiscordLogs plugin) {
        this.plugin = plugin;
    }

    /**
     * Creates a unique key for the chest interaction
     * @param player The player interacting with the chest
     * @param location The chest's location
     * @return A unique string key
     */
    private String createKey(Player player, Location location) {
        return player.getUniqueId().toString() + "_" +
               location.getWorld().getName() + "_" +
               location.getBlockX() + "_" +
               location.getBlockY() + "_" +
               location.getBlockZ();
    }

    /**
     * Handles chest opening events and stores initial inventory state
     */
    @EventHandler
    public void onChestOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        if (!(event.getInventory().getHolder() instanceof Chest)) return;

        Player player = (Player) event.getPlayer();
        Chest chest = (Chest) event.getInventory().getHolder();
        Location chestLoc = chest.getLocation();

        // Store initial chest contents
        Map<String, Integer> contents = getInventoryContents(event.getInventory());
        initialContents.put(createKey(player, chestLoc), contents);

        // Log chest opening
        LogMessage message = new LogMessage(
                player.getName(),
                "CHEST_OPEN",
                chestLoc,
                null,
                null
        );
        sendLogs(message);
    }

    /**
     * Handles chest closing events and compares inventory changes
     */
    @EventHandler
    public void onChestClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        if (!(event.getInventory().getHolder() instanceof Chest)) return;

        Player player = (Player) event.getPlayer();
        Chest chest = (Chest) event.getInventory().getHolder();
        Location chestLoc = chest.getLocation();
        String key = createKey(player, chestLoc);

        // Get initial contents
        Map<String, Integer> initial = initialContents.remove(key);
        if (initial == null) return; // Safety check

        // Get final contents
        Map<String, Integer> current = getInventoryContents(event.getInventory());

        // Compare and log differences
        logInventoryDifferences(player, chestLoc, initial, current);
    }

    /**
     * Compares initial and final inventory states and logs changes
     */
    private void logInventoryDifferences(Player player, Location chestLoc,
                                         Map<String, Integer> initial,
                                         Map<String, Integer> current) {
        // Find items that were added or had quantity increased
        Map<String, Integer> added = new HashMap<>();
        Map<String, Integer> removed = new HashMap<>();

        // Check for added or increased items
        for (Map.Entry<String, Integer> entry : current.entrySet()) {
            String itemKey = entry.getKey();
            int currentAmount = entry.getValue();
            int initialAmount = initial.getOrDefault(itemKey, 0);

            if (currentAmount > initialAmount) {
                added.put(itemKey, currentAmount - initialAmount);
            }
        }

        // Check for removed or decreased items
        for (Map.Entry<String, Integer> entry : initial.entrySet()) {
            String itemKey = entry.getKey();
            int initialAmount = entry.getValue();
            int currentAmount = current.getOrDefault(itemKey, 0);

            if (currentAmount < initialAmount) {
                removed.put(itemKey, initialAmount - currentAmount);
            }
        }

        // Log changes if any occurred
        if (!added.isEmpty()) {
            LogMessage addMessage = new LogMessage(
                    player.getName(),
                    "CHEST_ITEMS_ADDED",
                    chestLoc,
                    formatItemChanges(added),
                    null // Enchantments are included in the item key
            );
            sendLogs(addMessage);
        }

        if (!removed.isEmpty()) {
            LogMessage removeMessage = new LogMessage(
                    player.getName(),
                    "CHEST_ITEMS_REMOVED",
                    chestLoc,
                    formatItemChanges(removed),
                    null // Enchantments are included in the item key
            );
            sendLogs(removeMessage);
        }
    }

    /**
     * Creates a map of item contents with their quantities
     * Key format: "itemType:enchant1:level1,enchant2:level2"
     */
    private Map<String, Integer> getInventoryContents(Inventory inventory) {
        Map<String, Integer> contents = new HashMap<>();

        for (ItemStack item : inventory.getContents()) {
            if (item == null) continue;

            String itemKey = createItemKey(item);
            contents.merge(itemKey, item.getAmount(), Integer::sum);
        }

        return contents;
    }

    /**
     * Creates a unique key for an item including its enchantments
     */
    private String createItemKey(ItemStack item) {
        StringBuilder key = new StringBuilder(item.getType().toString());

        if (item.hasItemMeta()) {
            if (item.getItemMeta().hasDisplayName()) {
                key.append(":name=").append(item.getItemMeta().getDisplayName());
            }

            if (!item.getEnchantments().isEmpty()) {
                key.append(":enchants=");
                key.append(item.getEnchantments().entrySet().stream()
                        .map(e -> e.getKey().getName() + ":" + e.getValue())
                        .collect(Collectors.joining(",")));
            }
        }

        return key.toString();
    }

    /**
     * Formats item changes into a readable string
     */
    private String formatItemChanges(Map<String, Integer> changes) {
        return changes.entrySet().stream()
                .map(entry -> {
                    String[] parts = entry.getKey().split(":");
                    StringBuilder result = new StringBuilder();

                    // Add quantity and item type
                    result.append(entry.getValue()).append("x ");
                    result.append(parts[0].toLowerCase().replace("_", " "));

                    // Add custom name if exists
                    Arrays.stream(parts)
                            .filter(part -> part.startsWith("name="))
                            .findFirst()
                            .ifPresent(name -> result.append(" (").append(name.substring(5)).append(")"));

                    // Add enchantments if exist
                    Arrays.stream(parts)
                            .filter(part -> part.startsWith("enchants="))
                            .findFirst()
                            .ifPresent(enchants -> {
                                result.append(" with enchantments: ");
                                result.append(enchants.substring(9).replace(",", ", "));
                            });

                    return result.toString();
                })
                .collect(Collectors.joining(", "));
    }

    /**
     * Sends logs to both Discord and MySQL if enabled
     */
    private void sendLogs(LogMessage message) {
        // Send to Discord
        new DiscordWebhook(plugin).sendLog(message);

        // Log to MySQL if enabled
        if (plugin.getConfigManager().isMySQLEnabled() && plugin.getDatabaseManager() != null) {
            plugin.getDatabaseManager().logAction(message);
        }
    }
}