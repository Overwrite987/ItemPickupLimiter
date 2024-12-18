package ru.overwrite.pickuplimiter.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import ru.overwrite.pickuplimiter.utils.color.*;

public final class Utils {

    public static Colorizer COLORIZER;

    public static void setupColorizer(ConfigurationSection mainSettings) {
        COLORIZER = switch (mainSettings.getString("serializer", "LEGACY").toUpperCase()) {
            case "MINIMESSAGE" -> new MiniMessageColorizer();
            case "LEGACY" -> new LegacyColorizer();
            case "LEGACY_ADVANCED" -> new LegacyAdvancedColorizer();
            default -> new VanillaColorizer();
        };
    }

    public static final char COLOR_CHAR = '§';

    public static String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
        final char[] b = textToTranslate.toCharArray();

        for (int i = 0, length = b.length - 1; i < length; ++i) {
            if (b[i] == altColorChar && isValidColorCharacter(b[i + 1])) {
                b[i++] = COLOR_CHAR;
                b[i] |= 0x20;
            }
        }

        return new String(b);
    }

    private static boolean isValidColorCharacter(char c) {
        return (c >= '0' && c <= '9') ||
                (c >= 'a' && c <= 'f') ||
                c == 'r' ||
                (c >= 'k' && c <= 'o') ||
                c == 'x' ||
                (c >= 'A' && c <= 'F') ||
                c == 'R' ||
                (c >= 'K' && c <= 'O') ||
                c == 'X';
    }

    public static boolean startsWithIgnoreCase(@NotNull String str, String prefix) {
        if (prefix == null) {
            return false;
        }
        return str.regionMatches(true, 0, prefix, 0, prefix.length());
    }
}
