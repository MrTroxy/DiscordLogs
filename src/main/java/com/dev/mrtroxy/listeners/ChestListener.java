package com.dev.mrtroxy.listeners;

import com.dev.mrtroxy.DiscordLogs;
import com.dev.mrtroxy.models.LogMessage;
import com.dev.mrtroxy.utils.DiscordWebhook;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Chest;
import org.bukkit.event.inventory.InventoryAction;

public class ChestListener implements Listener {

    private final DiscordLogs plugin;

    public ChestListener(DiscordLogs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!plugin.getConfigManager().isChestLoggingEnabled()) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.getInventory().getHolder() instanceof Chest) {
            Player player = (Player) event.getWhoClicked();
            Location chestLoc = ((Chest) event.getInventory().getHolder()).getLocation();

            // Gestion des différentes actions d'inventaire
            switch (event.getAction()) {
                case PLACE_ALL:
                case PLACE_ONE:
                case PLACE_SOME:
                    if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.CHEST) {
                        ItemStack item = event.getCursor().clone();
                        LogMessage message = new LogMessage(
                                player.getName(),
                                "CHEST_ITEM_ADDED",
                                chestLoc,
                                formatItemStack(item)
                        );
                        new DiscordWebhook(plugin).sendLog(message);
                    }
                    break;

                case PICKUP_ALL:
                case PICKUP_HALF:
                case PICKUP_ONE:
                case PICKUP_SOME:
                    if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.CHEST) {
                        ItemStack item = event.getCurrentItem().clone();
                        LogMessage message = new LogMessage(
                                player.getName(),
                                "CHEST_ITEM_REMOVED",
                                chestLoc,
                                formatItemStack(item)
                        );
                        new DiscordWebhook(plugin).sendLog(message);
                    }
                    break;

                case MOVE_TO_OTHER_INVENTORY:
                    if (event.getClickedInventory() != null) {
                        ItemStack item = event.getCurrentItem().clone();
                        String action = event.getClickedInventory().getType() == InventoryType.CHEST
                                ? "CHEST_ITEM_REMOVED"
                                : "CHEST_ITEM_ADDED";
                        LogMessage message = new LogMessage(
                                player.getName(),
                                action,
                                chestLoc,
                                formatItemStack(item)
                        );
                        new DiscordWebhook(plugin).sendLog(message);
                    }
                    break;
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!plugin.getConfigManager().isChestLoggingEnabled()) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.getInventory().getHolder() instanceof Chest) {
            Player player = (Player) event.getWhoClicked();
            Location chestLoc = ((Chest) event.getInventory().getHolder()).getLocation();

            // Vérifier si les items sont déplacés dans le coffre
            event.getRawSlots().forEach(slot -> {
                if (slot < event.getInventory().getSize()) {
                    ItemStack item = event.getNewItems().get(slot);
                    if (item != null) {
                        LogMessage message = new LogMessage(
                                player.getName(),
                                "CHEST_ITEM_ADDED",
                                chestLoc,
                                formatItemStack(item)
                        );
                        new DiscordWebhook(plugin).sendLog(message);
                    }
                }
            });
        }
    }

    private String formatItemStack(ItemStack item) {
        if (item == null) return "nothing";

        StringBuilder result = new StringBuilder();
        result.append(item.getAmount()).append("x ");
        result.append(item.getType().toString().replace("_", " ").toLowerCase());

        if (item.hasItemMeta()) {
            if (item.getItemMeta().hasDisplayName()) {
                result.append(" (").append(item.getItemMeta().getDisplayName()).append(")");
            }
        }

        return result.toString();
    }
}