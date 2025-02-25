package ru.overwrite.pickuplimiter;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import ru.overwrite.pickuplimiter.configuration.Config;

import java.util.Set;

public class PickupLimiter implements Listener {

    private final Config pluginConfig;
    private final DatabaseManager databaseManager;

    public PickupLimiter(Main plugin) {
        this.pluginConfig = plugin.getPluginConfig();
        this.databaseManager = plugin.getDatabaseManager();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player p)) {
            return;
        }
        String playerName = p.getName();
        Set<String> blockedMaterials = databaseManager.getCachedPlayers().get(playerName);
        if (blockedMaterials == null) {
            return;
        }
        if (!pluginConfig.getMainSettings().activeWorlds().contains(p.getWorld().getName())) {
            return;
        }
        if (pluginConfig.getMainSettings().whitelist() != blockedMaterials.contains(e.getItem().getItemStack().getType().toString())) {
            e.setCancelled(true);
        }
    }
}
