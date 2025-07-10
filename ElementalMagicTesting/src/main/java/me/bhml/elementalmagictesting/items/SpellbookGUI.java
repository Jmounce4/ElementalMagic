package me.bhml.elementalmagictesting.items;

import me.bhml.elementalmagictesting.spells.PlayerSpellTracker;
import me.bhml.elementalmagictesting.spells.Spell;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class SpellbookGUI {
    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, ChatColor.DARK_PURPLE + "Elemental Spells");

        Spell current = PlayerSpellTracker.getCurrentSpell(player);
        if (current != null) {
            ItemStack spellItem = new ItemStack(Material.ENCHANTED_BOOK);
            ItemMeta meta = spellItem.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA + current.getName());
            meta.setLore(List.of(
                    ChatColor.GRAY + "Element: " + current.getElement().getColor() + current.getElement().name(),
                    ChatColor.GRAY + "Cooldown: " + current.getCooldown() + " ticks"
            ));
            spellItem.setItemMeta(meta);

            gui.setItem(4, spellItem); // center slot
        }

        player.openInventory(gui);
    }
}
