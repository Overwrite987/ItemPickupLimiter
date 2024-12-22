package ru.overwrite.pickuplimiter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import ru.overwrite.pickuplimiter.configuration.Config;
import ru.overwrite.pickuplimiter.utils.MaterialUtils;

import java.util.*;

public class CommandClass implements CommandExecutor, TabCompleter {

    private final Main plugin;
    private final Config pluginConfig;
    private final DatabaseManager databaseManager;

    public CommandClass(Main plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
        this.databaseManager = plugin.getDatabaseManager();
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player p)) {
            plugin.getLogger().info("Только для игроков");
            return true;
        }
        if (!p.hasPermission("pickuplimiter.use")) {
            p.sendMessage(pluginConfig.getMessages().noPerm());
            return true;
        }
        if (args.length < 1 || args.length > 3) {
            p.sendMessage(pluginConfig.getMessages().usage());
            return true;
        }
        String playerName = p.getName();
        switch (args[0].toLowerCase()) {
            case "enable": {
                if (databaseManager.getCachedPlayers().containsKey(playerName)) {
                    p.sendMessage(pluginConfig.getMessages().alreadyEnabled());
                    return true;
                }
                databaseManager.enableForPlayer(playerName);
                p.sendMessage(pluginConfig.getMessages().enabled());
                return true;
            }
            case "disable": {
                if (databaseManager.getCachedPlayers().containsKey(playerName)) {
                    databaseManager.disableForPlayer(playerName);
                    p.sendMessage(pluginConfig.getMessages().disabled());
                    return true;
                }
                p.sendMessage(pluginConfig.getMessages().alreadyDisabled());
                return true;
            }
            case "add": {
                if (args.length < 2) {
                    p.sendMessage(pluginConfig.getMessages().usage());
                    return true;
                }
                String material = args[1].toUpperCase();
                if (!MaterialUtils.MATERIAL_NAMES.contains(material)) {
                    p.sendMessage(pluginConfig.getMessages().incorrectMaterial());
                    return true;
                }
                if (!databaseManager.getCachedPlayers().containsKey(playerName)) {
                    databaseManager.addPlayer(playerName, pluginConfig.getMainSettings().defaultEnabled(), Set.of(new String[]{material}));
                    p.sendMessage(pluginConfig.getMessages().blockSuccess().replace("%material%", material));
                    return true;
                }
                Set<String> newSet = new HashSet<>(databaseManager.getCachedPlayers().get(playerName));
                if (newSet.contains(material)) {
                    p.sendMessage(pluginConfig.getMessages().alreadyBlocked());
                    return true;
                }
                newSet.add(material);
                databaseManager.updatePlayer(playerName, newSet);
                p.sendMessage(pluginConfig.getMessages().blockSuccess().replace("%material%", material));
                return true;
            }
            case "remove": {
                if (args.length < 2) {
                    p.sendMessage(pluginConfig.getMessages().usage());
                    return true;
                }
                String material = args[1].toUpperCase();
                if (!MaterialUtils.MATERIAL_NAMES.contains(material)) {
                    p.sendMessage(pluginConfig.getMessages().incorrectMaterial());
                    return true;
                }
                Set<String> newSet = new HashSet<>(databaseManager.getCachedPlayers().get(playerName));
                if (!newSet.contains(material)) {
                    p.sendMessage(pluginConfig.getMessages().notBlocked().replace("%material%", material));
                    return true;
                }
                newSet.remove(material);
                if (newSet.isEmpty()) {
                    databaseManager.removePlayer(playerName);
                    p.sendMessage(pluginConfig.getMessages().unblockSuccess().replace("%material%", material));
                    return true;
                }
                databaseManager.updatePlayer(playerName, newSet);
                p.sendMessage(pluginConfig.getMessages().unblockSuccess().replace("%material%", material));
                return true;
            }
            case "list": {
                Set<String> blocked = databaseManager.getCachedPlayers().get(p.getName());
                if (blocked == null) {
                    p.sendMessage(pluginConfig.getMessages().list().replace("%list%", "NaN"));
                    return true;
                }
                blocked = new TreeSet<>(databaseManager.getCachedPlayers().get(playerName));
                p.sendMessage(pluginConfig.getMessages().list().replace("%list%", blocked.toString()));
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        final List<String> completions = new ArrayList<>();
        if (!(sender instanceof Player p)) {
            return List.of();
        }
        if (!sender.hasPermission("pickuplimiter.use")) {
            return List.of();
        }
        if (args.length == 1) {
            completions.add("enable");
            completions.add("disable");
            completions.add("add");
            completions.add("remove");
            completions.add("list");
            return getResult(args, completions);
        }
        return switch (args[0]) {
            case "add" -> {
                completions.addAll(MaterialUtils.MATERIAL_NAMES);
                yield getResult(args, completions);
            }
            case "remove" -> {
                Set<String> blocked = databaseManager.getCachedPlayers().get(p.getName());
                if (blocked == null) {
                    yield List.of();
                }
                completions.addAll(databaseManager.getCachedPlayers().get(p.getName()));
                yield getResult(args, completions);
            }
            default -> List.of();
        };
    }

    private List<String> getResult(String[] args, List<String> completions) {
        final List<String> result = new ArrayList<>();
        for (String c : completions) {
            if (StringUtil.startsWithIgnoreCase(c, args[args.length - 1])) {
                result.add(c);
            }
        }
        return result;
    }
}
