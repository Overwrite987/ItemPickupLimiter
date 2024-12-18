package ru.overwrite.pickuplimiter.configuration.data;

import java.util.List;

public record MainSettings(
        boolean defaultEnabled,
        boolean whitelist,
        List<String> activeWorlds) {
}
