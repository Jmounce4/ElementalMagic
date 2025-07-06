package me.bhml.elementalmagictesting.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;


public class ElementalCore {
    /**
     * Creates the Elemental Core item used for casting magic.
     */
    public static ItemStack create(JavaPlugin plugin) {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Elemental Core");
        meta.setLore(List.of(
                ChatColor.GRAY + "Right-click: Cycle through spells",
                ChatColor.GRAY + "Left-click: Cast current spell"
        ));

        item.setItemMeta(meta);

        // Tag the item with a custom identifier
        ItemUtils.setKey(item, "elemental_core", "true");

        // Add a glowing enchantment purely for visual flair
        ItemUtils.addGlow(item);

        return item;
    }


}
