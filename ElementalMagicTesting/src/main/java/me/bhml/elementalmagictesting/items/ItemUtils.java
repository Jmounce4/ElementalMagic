package me.bhml.elementalmagictesting.items;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.enchantments.Enchantment;

import java.util.Map;

public class ItemUtils {
    private static JavaPlugin plugin;

    // Initialize the plugin reference for NamespacedKey usage
    public static void init(JavaPlugin pluginInstance) {
        plugin = pluginInstance;
    }

    /**
     * Sets a custom string key on an item using PersistentDataContainer.
     */
    public static void setKey(ItemStack item, String key, String value) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, key),
                PersistentDataType.STRING,
                value
        );
        item.setItemMeta(meta);
    }

    /**
     * Checks if an item has a specific custom key.
     */
    public static boolean hasKey(ItemStack item, String key) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        return meta.getPersistentDataContainer().has(
                new NamespacedKey(plugin, key),
                PersistentDataType.STRING
        );
    }

    /**
     * Retrieves a custom string key from the item.
     */
    public static String getKey(ItemStack item, String key) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        return meta.getPersistentDataContainer().get(
                new NamespacedKey(plugin, key),
                PersistentDataType.STRING
        );
    }

    /**
     * Adds a glowing enchant effect without any functional enchant.
     */
    public static void addGlow(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        // Add a dummy enchantment
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.setUnbreakable(true); // optional: make it unbreakable for effect

        item.setItemMeta(meta);
    }

    /**
     * Clears all enchantments from an item (used if you want to remove glow).
     */
    public static void clearEnchants(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
            meta.removeEnchant(entry.getKey());
        }

        item.setItemMeta(meta);
    }


}
