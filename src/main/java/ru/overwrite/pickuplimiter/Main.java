package ru.overwrite.pickuplimiter;

import lombok.Getter;
import org.bukkit.command.PluginCommand;
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
        databaseManager = new DatabaseManager(this);
        try {
            databaseManager.connect(config.getConfigurationSection("database_settings"));
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

    public void onDisable() {
        try {
            databaseManager.disconnect();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        getServer().getScheduler().cancelTasks(this);
    }
}
