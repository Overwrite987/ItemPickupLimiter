package ru.overwrite.pickuplimiter;

import lombok.Getter;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import ru.overwrite.pickuplimiter.configuration.Config;

import java.sql.SQLException;

@Getter
public final class Main extends JavaPlugin {

    private final Config pluginConfig = new Config();
    private DatabaseManager databaseManager;

    public void onEnable() {
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        databaseManager = new DatabaseManager(this, getDatabaseSettings(config));
        try {
            databaseManager.connect();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        setupPluginConfig(config);
        PluginCommand pickupLimiterCommand = getCommand("pickuplimiter");
        CommandClass pickupLimiterCommandClass = new CommandClass(this);
        pickupLimiterCommand.setExecutor(pickupLimiterCommandClass);
        pickupLimiterCommand.setTabCompleter(pickupLimiterCommandClass);
        getServer().getPluginManager().registerEvents(new PickupLimiter(this), this);
    }

    public void setupPluginConfig(FileConfiguration config) {
        pluginConfig.setMainSettings(config);
        pluginConfig.setupMessages(config);
    }

    private DatabaseManager.DatabaseSettings getDatabaseSettings(FileConfiguration config) {
        ConfigurationSection databaseSettings = config.getConfigurationSection("database_settings");
        if (databaseSettings == null) {
            throw new IllegalArgumentException("Database settings section is missing in configuration.");
        }

        return new DatabaseManager.DatabaseSettings(
                databaseSettings.getBoolean("mysql", false),
                databaseSettings.getBoolean("mariadb", false),
                databaseSettings.getString("hostname", "127.0.0.1:3306"),
                databaseSettings.getString("user", "user"),
                databaseSettings.getString("password", "password"),
                databaseSettings.getString("databasename", "playerdata"),
                databaseSettings.getString("connection_parameters", "?autoReconnect=true&initialTimeout=1&useSSL=false")
        );
    }

    public void onDisable() {
        try {
            databaseManager.disconnect();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        getServer().getScheduler().cancelTasks(this);
    }
}
