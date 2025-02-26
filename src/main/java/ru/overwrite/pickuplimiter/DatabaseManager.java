package ru.overwrite.pickuplimiter;

import lombok.Getter;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.*;
import java.util.*;

public class DatabaseManager {

    public record DatabaseSettings(
            boolean useMysql,
            boolean useMariadb,
            String hostname,
            String user,
            String password,
            String databaseName,
            String connectionParams
    ) {
    }

    @Getter
    private final Map<String, Set<String>> cachedPlayers = new HashMap<>();

    private final Main plugin;
    private final DatabaseSettings settings;
    private Connection connection;

    public DatabaseManager(Main plugin, DatabaseSettings settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    public void connect() throws SQLException {
        if (settings.useMysql()) {
            String urlPrefix = settings.useMariadb() ? "jdbc:mariadb://" : "jdbc:mysql://"; // yeah. the greatest fucking way to do that. #weed
            String url = urlPrefix + settings.hostname() + "/" + settings.databaseName() + settings.connectionParams();
            connection = DriverManager.getConnection(url, settings.user(), settings.password());
        } else {
            String sqlitePath = new File(plugin.getDataFolder(), settings.databaseName() + ".db").getAbsolutePath();
            String url = "jdbc:sqlite:" + sqlitePath;
            connection = DriverManager.getConnection(url);
        }
        createTable();
        cachedPlayers.putAll(getAllEnabledPlayers());
        plugin.getLogger().info("Cached " + cachedPlayers.size() + " players from database.");
    }

    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    private void createTable() throws SQLException {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS Players (" +
                "player VARCHAR(255) PRIMARY KEY, " +
                "enabled BOOLEAN NOT NULL, " +
                "list TEXT NOT NULL" +
                ");";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
        }
    }

    public void addPlayer(String playerName, boolean enabled, Set<String> materials) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String insertSQL = "INSERT INTO Players (player, enabled, list) VALUES (?, ?, ?) " +
                        "ON CONFLICT(player) DO UPDATE SET enabled = excluded.enabled, list = excluded.list;";
                try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
                    pstmt.setString(1, playerName);
                    pstmt.setBoolean(2, enabled);
                    pstmt.setString(3, String.join(",", materials));
                    pstmt.executeUpdate();
                }
                if (enabled) {
                    cachedPlayers.put(playerName, materials);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void removePlayer(String playerName) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String deleteSQL = "DELETE FROM Players WHERE player = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(deleteSQL)) {
                    pstmt.setString(1, playerName);
                    pstmt.executeUpdate();
                }
                cachedPlayers.remove(playerName);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void updatePlayer(String playerName, Set<String> materials) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String updateSQL = "UPDATE Players SET list = ? WHERE player = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(updateSQL)) {
                    pstmt.setString(1, String.join(",", materials));
                    pstmt.setString(2, playerName);
                    pstmt.executeUpdate();
                }
                cachedPlayers.computeIfPresent(playerName, (key, value) -> materials);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void enableForPlayer(String playerName) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                setEnabledStatusForPlayer(playerName, true);
                cachedPlayers.put(playerName, getMaterialsForPlayer(playerName));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void disableForPlayer(String playerName) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                setEnabledStatusForPlayer(playerName, false);
                cachedPlayers.remove(playerName);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void setEnabledStatusForPlayer(String playerName, boolean status) throws SQLException {
        String updateSQL = "UPDATE Players SET enabled = ? WHERE player = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(updateSQL)) {
            pstmt.setBoolean(1, status);
            pstmt.setString(2, playerName);
            pstmt.executeUpdate();
        }
    }

    public Map<String, Set<String>> getAllEnabledPlayers() throws SQLException {
        String selectSQL = "SELECT player, list FROM Players WHERE enabled = true";
        Map<String, Set<String>> players = new HashMap<>();
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(selectSQL)) {
            while (rs.next()) {
                String player = rs.getString("player");
                String list = rs.getString("list");
                Set<String> materials = new HashSet<>(List.of(list.split(",")));
                players.put(player, materials);
            }
        }
        return players;
    }

    public Set<String> getMaterialsForPlayer(String playerName) throws SQLException {
        String selectSQL = "SELECT list, enabled FROM Players WHERE player = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(selectSQL)) {
            pstmt.setString(1, playerName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                boolean enabled = rs.getBoolean("enabled");
                if (enabled) {
                    String list = rs.getString("list");
                    return new HashSet<>(List.of(list.split(",")));
                }
            }
        }
        return null;
    }
}
