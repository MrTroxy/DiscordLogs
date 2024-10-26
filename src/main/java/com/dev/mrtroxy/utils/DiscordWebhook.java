package com.dev.mrtroxy.utils;

import com.dev.mrtroxy.DiscordLogs;
import com.dev.mrtroxy.models.LogMessage;
import org.bukkit.Location;

import javax.net.ssl.HttpsURLConnection;
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
            if (webhookUrl.equals("YOUR_WEBHOOK_URL")) return;

            URL url = new URL(webhookUrl);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            Location loc = message.getLocation();
            String locationStr = String.format("x: %d, y: %d, z: %d",
                    loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

            // Dans la méthode sendLog de DiscordWebhook.java, modifiez la partie JSON :
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
                                        "            }%s\n" +
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
                                                                     "}", message.getItemDetails()) : ""
            );

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = json.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            connection.getResponseCode();
            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}