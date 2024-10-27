package com.dev.mrtroxy.database;

import com.dev.mrtroxy.DiscordLogs;
import com.dev.mrtroxy.models.LogMessage;
import org.bukkit.Location;

import java.sql.*;

/**
 * Manages all database operations for the plugin
 */
public class DatabaseManager {
    private final DiscordLogs plugin;
    private Connection connection;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final String table;

    /**
     * Initializes the database manager with configuration values
     * @param plugin The main plugin instance
     */
    public DatabaseManager(DiscordLogs plugin) {
        this.plugin = plugin;
        this.host = plugin.getConfigManager().getMySQLHost();
        this.port = plugin.getConfigManager().getMySQLPort();
        this.database = plugin.getConfigManager().getMySQLDatabase();
        this.username = plugin.getConfigManager().getMySQLUsername();
        this.password = plugin.getConfigManager().getMySQLPassword();
        this.table = plugin.getConfigManager().getMySQLTable();
    }

    /**
     * Establishes a connection to the MySQL database
     * @return true if connection is successful, false otherwise
     */
    public boolean connect() {
        try {
            if (connection != null && !connection.isClosed()) {
                return true;
            }

            synchronized (this) {
                if (connection != null && !connection.isClosed()) {
                    return true;
                }
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection(
                        "jdbc:mysql://" + host + ":" + port + "/" + database +
                        "?useSSL=false&autoReconnect=true&useUnicode=true&characterEncoding=utf-8",
                        username,
                        password
                );

                plugin.getLogger().info("Successfully connected to MySQL database!");
                createTableIfNotExists();
                return true;
            }
        } catch (SQLException | ClassNotFoundException e) {
            plugin.getLogger().severe("Failed to connect to MySQL database: " + e.getMessage());
            return false;
        }
    }

    /**
     * Closes the database connection
     */
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("Database connection closed successfully!");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error closing MySQL connection: " + e.getMessage());
        }
    }

    /**
     * Creates the necessary database table if it doesn't exist
     */
    private void createTableIfNotExists() {
        try (Statement statement = connection.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS " + table + " ("
                         + "id INT AUTO_INCREMENT PRIMARY KEY,"
                         + "player_name VARCHAR(36) NOT NULL,"
                         + "action VARCHAR(50) NOT NULL,"
                         + "world VARCHAR(50) NOT NULL,"
                         + "x INT NOT NULL,"
                         + "y INT NOT NULL,"
                         + "z INT NOT NULL,"
                         + "items TEXT,"
                         + "enchantments TEXT,"
                         + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                         + "INDEX idx_player (player_name),"
                         + "INDEX idx_action (action),"
                         + "INDEX idx_timestamp (timestamp)"
                         + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            statement.executeUpdate(sql);
            plugin.getLogger().info("Database table checked/created successfully!");
        } catch (SQLException e) {
            plugin.getLogger().severe("Error creating table: " + e.getMessage());
        }
    }

    /**
     * Logs an action to the database
     * @param message The LogMessage containing all action details
     */
    public void logAction(LogMessage message) {
        if (!connect()) {
            plugin.getLogger().severe("Failed to log action: Database connection failed!");
            return;
        }

        String sql = "INSERT INTO " + table + " (player_name, action, world, x, y, z, items, enchantments) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            Location loc = message.getLocation();

            pstmt.setString(1, message.getPlayerName());
            pstmt.setString(2, message.getAction());
            pstmt.setString(3, loc.getWorld().getName());
            pstmt.setInt(4, loc.getBlockX());
            pstmt.setInt(5, loc.getBlockY());
            pstmt.setInt(6, loc.getBlockZ());
            pstmt.setString(7, message.getItemDetails());
            pstmt.setString(8, message.getEnchantments());

            pstmt.executeUpdate();

            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().info("Successfully logged action to database for player: " + message.getPlayerName());
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error logging to database: " + e.getMessage());
            if (e.getMessage().contains("Connection")) {
                connection = null; // Force reconnection on next attempt
            }
        }
    }

    /**
     * Checks if the database connection is valid
     * @return true if connection is valid, false otherwise
     */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(1);
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Attempts to reconnect to the database
     * @return true if reconnection is successful, false otherwise
     */
    public boolean reconnect() {
        disconnect();
        return connect();
    }

    /**
     * Cleans up old logs based on retention period
     * @param days Number of days to keep logs for
     */
    public void cleanupOldLogs(int days) {
        if (!connect()) return;

        String sql = "DELETE FROM " + table + " WHERE timestamp < DATE_SUB(NOW(), INTERVAL ? DAY)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, days);
            int deletedRows = pstmt.executeUpdate();

            plugin.getLogger().info("Cleaned up " + deletedRows + " old log entries.");
        } catch (SQLException e) {
            plugin.getLogger().severe("Error cleaning up old logs: " + e.getMessage());
        }
    }
}