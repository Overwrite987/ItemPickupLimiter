package ru.overwrite.pickuplimiter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import ru.overwrite.pickuplimiter.configuration.Config;
import ru.overwrite.pickuplimiter.configuration.data.Messages;
import ru.overwrite.pickuplimiter.utils.MaterialUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
        Messages messages = pluginConfig.getMessages();
        if (!p.hasPermission("pickuplimiter.use")) {
            p.sendMessage(messages.noPerm());
            return true;
        }
        if (args.length < 1 || args.length > 3) {
            p.sendMessage(messages.usage());
            return true;
        }
        String playerName = p.getName();
        switch (args[0].toLowerCase()) {
            case "enable": {
                if (databaseManager.getCachedPlayers().containsKey(playerName)) {
                    p.sendMessage(messages.alreadyEnabled());
                    return true;
                }
                databaseManager.enableForPlayer(playerName);
                p.sendMessage(messages.enabled());
                return true;
            }
            case "disable": {
                if (databaseManager.getCachedPlayers().containsKey(playerName)) {
                    databaseManager.disableForPlayer(playerName);
                    p.sendMessage(messages.disabled());
                    return true;
                }
                p.sendMessage(messages.alreadyDisabled());
                return true;
            }
            case "add": {
                if (args.length < 2) {
                    p.sendMessage(messages.usage());
                    return true;
                }
                String material = args[1].toUpperCase();
                if (!MaterialUtils.MATERIAL_NAMES.contains(material)) {
                    p.sendMessage(messages.incorrectMaterial());
                    return true;
                }
                if (!databaseManager.getCachedPlayers().containsKey(playerName)) {
                    databaseManager.addPlayer(playerName, pluginConfig.getMainSettings().defaultEnabled(), Set.of(new String[]{material}));
                } else {
                    Set<String> materialSet = databaseManager.getCachedPlayers().get(playerName);
                    if (materialSet.contains(material)) {
                        p.sendMessage(messages.alreadyBlocked());
                        return true;
                    }
                    materialSet.add(material);
                    databaseManager.updatePlayer(playerName, materialSet);
                }
                p.sendMessage(messages.blockSuccess().replace("%material%", material));
                return true;
            }
            case "remove": {
                if (args.length < 2) {
                    p.sendMessage(messages.usage());
                    return true;
                }
                String material = args[1].toUpperCase();
                if (!MaterialUtils.MATERIAL_NAMES.contains(material)) {
                    p.sendMessage(messages.incorrectMaterial());
                    return true;
                }
                Set<String> materialSet = databaseManager.getCachedPlayers().get(playerName);
                if (!materialSet.contains(material)) {
                    p.sendMessage(messages.notBlocked().replace("%material%", material));
                    return true;
                }
                materialSet.remove(material);
                if (materialSet.isEmpty()) {
                    databaseManager.removePlayer(playerName);
                } else {
                    databaseManager.updatePlayer(playerName, materialSet);
                }
                p.sendMessage(messages.unblockSuccess().replace("%material%", material));
                return true;
            }
            case "list": {
                Set<String> blocked = databaseManager.getCachedPlayers().get(playerName);
                String list = blocked == null
                        ? "NaN"
                        : new TreeSet<>(blocked).toString();
                p.sendMessage(messages.list().replace("%list%", list));
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (!(sender instanceof Player p)) {
            return List.of();
        }
        if (!sender.hasPermission("pickuplimiter.use")) {
            return List.of();
        }
        final List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("enable");
            completions.add("disable");
            completions.add("add");
            completions.add("remove");
            completions.add("list");
            return getResult(args, completions);
        }
        return switch (args[0].toLowerCase()) {
            case "add" -> {
                completions.addAll(MaterialUtils.MATERIAL_NAMES);
                yield getResult(args, completions);
            }
            case "remove" -> {
                Set<String> blocked = databaseManager.getCachedPlayers().get(p.getName());
                if (blocked == null) {
                    yield List.of();
                }
                completions.addAll(blocked);
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
