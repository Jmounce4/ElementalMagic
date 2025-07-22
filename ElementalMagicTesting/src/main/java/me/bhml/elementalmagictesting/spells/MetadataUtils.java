package me.bhml.elementalmagictesting.spells;

import me.bhml.elementalmagictesting.ElementalMagicTesting;

import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

public class MetadataUtils {

    private static final Plugin plugin = ElementalMagicTesting.getInstance();

    public static void set(Entity entity, String key, Object value) {
        entity.setMetadata(key, new FixedMetadataValue(plugin, value));
    }

    public static void remove(Entity entity, String key) {
        entity.removeMetadata(key, plugin);
    }

}
