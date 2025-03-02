package ru.overwrite.pickuplimiter.configuration;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import ru.overwrite.pickuplimiter.configuration.data.MainSettings;
import ru.overwrite.pickuplimiter.configuration.data.Messages;
import ru.overwrite.pickuplimiter.utils.Utils;

@Getter
public class Config {

    private MainSettings mainSettings;
    private Messages messages;

    public void setMainSettings(FileConfiguration config) {
        ConfigurationSection mainSettingsSection = config.getConfigurationSection("main_settings");
        Utils.setupColorizer(mainSettingsSection);
        mainSettings = new MainSettings(
                mainSettingsSection.getBoolean("default_enabled", true),
                mainSettingsSection.getBoolean("whitelist", false),
                mainSettingsSection.getStringList("active_worlds"));
    }

    public void setupMessages(FileConfiguration config) {
        ConfigurationSection messagesSection = config.getConfigurationSection("messages");
        messages = new Messages(
                Utils.COLORIZER.colorize(messagesSection.getString("no_perm", "&cУ вас нет прав.")),
                Utils.COLORIZER.colorize(messagesSection.getString("enabled", "&fБлокировка подъёма предметов &aвключена!")),
                Utils.COLORIZER.colorize(messagesSection.getString("already_enabled", "&fБлокировка подъёма предметов &aуже включена!")),
                Utils.COLORIZER.colorize(messagesSection.getString("disabled", "&fБлокировка подъёма предметов &cвыключена!")),
                Utils.COLORIZER.colorize(messagesSection.getString("already_disabled", "&fБлокировка подъёма предметов &cуже выключена!")),
                Utils.COLORIZER.colorize(messagesSection.getString("block_success", "&fВы успешно заблокировали материал &6%material%")),
                Utils.COLORIZER.colorize(messagesSection.getString("unblock_success", "&fВы успешно разблокировали материал &6%material%")),
                Utils.COLORIZER.colorize(messagesSection.getString("already_blocked", "&cМатериал %material% уже заблокирован вами!")),
                Utils.COLORIZER.colorize(messagesSection.getString("not_blocked", "&cМатериал %material% не заблокирован вами!")),
                Utils.COLORIZER.colorize(messagesSection.getString("incorrect_material", "&cОшибка! Материал &6%material% &cне найден!")),
                Utils.COLORIZER.colorize(messagesSection.getString("list", "&fЗаблокированные материалы на данный момент: &6%list%")),
                Utils.COLORIZER.colorize(messagesSection.getString("usage", """
                        Использование.
                            &a/picklimit enable &f- включить блокировку подъёма предметов
                            &a/picklimit diable &f- отключить блокировку подъёма предметов
                            &a/picklimit add <ID> &f- добавить материал в список
                            &a/picklimit remove <ID> &f- убрать материал из списка
                            &a/picklimit list &f- посмотреть список всех заблокированных материалов"""))
        );
    }
}
