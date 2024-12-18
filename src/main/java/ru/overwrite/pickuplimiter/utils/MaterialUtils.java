package ru.overwrite.pickuplimiter.utils;

import org.bukkit.Material;

import java.util.HashSet;
import java.util.Set;

public final class MaterialUtils {

    public static final Set<String> MATERIAL_NAMES = new HashSet<>();

    static {
        for (Material material : Material.values()) {
            MATERIAL_NAMES.add(material.name());
        }
    }
}
