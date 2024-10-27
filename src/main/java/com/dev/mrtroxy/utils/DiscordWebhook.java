package com.dev.mrtroxy.utils;

import com.dev.mrtroxy.DiscordLogs;
import com.dev.mrtroxy.models.LogMessage;
import org.bukkit.Location;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DiscordWebhook {

    private final DiscordLogs plugin;

    public DiscordWebhook(DiscordLogs plugin) {
        this.plugin = plugin;
    }

    public void sendLog(LogMessage message) {
        try {
            String webhookUrl = plugin.getConfigManager().getWebhookUrl();
            plugin.getLogger().info("Using webhook URL: " + webhookUrl);

            if (webhookUrl.equals("YOUR_WEBHOOK_URL")) {
                plugin.getLogger().warning("Webhook URL not configured!");
                return;
            }

            URL url = new URL(webhookUrl);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "Java-DiscordWebhook-BY-MrTroxy");
            connection.setDoOutput(true);

            Location loc = message.getLocation();
            String locationStr = String.format("x: %d, y: %d, z: %d",
                    loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

            String json = String.format("{\n" +
                                        "    \"embeds\": [{\n" +
                                        "        \"title\": \"Player Action Log\",\n" +
                                        "        \"color\": 3447003,\n" +
                                        "        \"fields\": [\n" +
                                        "            {\n" +
                                        "                \"name\": \"Player\",\n" +
                                        "                \"value\": \"%s\",\n" +
                                        "                \"inline\": true\n" +
                                        "            },\n" +
                                        "            {\n" +
                                        "                \"name\": \"Action\",\n" +
                                        "                \"value\": \"%s\",\n" +
                                        "                \"inline\": true\n" +
                                        "            },\n" +
                                        "            {\n" +
                                        "                \"name\": \"Location\",\n" +
                                        "                \"value\": \"%s\",\n" +
                                        "                \"inline\": true\n" +
                                        "            }%s%s\n" +
                                        "        ]\n" +
                                        "    }]\n" +
                                        "}",
                    message.getPlayerName(),
                    plugin.getLangManager().getMessage(message.getAction()),
                    locationStr,
                    message.getItemDetails() != null ? String.format(",{\n" +
                                                                     "    \"name\": \"Items\",\n" +
                                                                     "    \"value\": \"%s\",\n" +
                                                                     "    \"inline\": false\n" +
                                                                     "}", message.getItemDetails()) : "",
                    message.getEnchantments() != null ? String.format(",{\n" +
                                                                      "    \"name\": \"Enchantments\",\n" +
                                                                      "    \"value\": \"%s\",\n" +
                                                                      "    \"inline\": false\n" +
                                                                      "}", message.getEnchantments()) : ""
            );

            plugin.getLogger().info("Sending JSON: " + json);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = json.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            plugin.getLogger().info("Response code: " + responseCode);

            // Read the response only if it's not a 204 No Content response
            if (responseCode != 204 && connection.getErrorStream() != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    plugin.getLogger().warning("Error response: " + response.toString());
                }
            }

            connection.disconnect();

        } catch (Exception e) {
            plugin.getLogger().severe("Error sending webhook: " + e.getMessage());
            e.printStackTrace();
        }
    }
}