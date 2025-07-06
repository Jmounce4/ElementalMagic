package me.bhml.elementalmagictesting.items;


import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class SpellbookItem {


    /**
     * Builds and returns the Spellbook item.
     */
    public static ItemStack create(JavaPlugin plugin) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(ChatColor.AQUA + "Elemental Spellbook");
        meta.setLore(List.of(
                ChatColor.GRAY + "Right-click to manage your spell loadout",
                ChatColor.GRAY + "Contains your magical knowledge"
        ));

        item.setItemMeta(meta);

        ItemUtils.setKey(item, "spellbook", "true");
        ItemUtils.addGlow(item);

        return item;
    }
}
