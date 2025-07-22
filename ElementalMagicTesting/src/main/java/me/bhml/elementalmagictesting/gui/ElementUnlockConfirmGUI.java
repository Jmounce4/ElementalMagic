package me.bhml.elementalmagictesting.gui;




import me.bhml.elementalmagictesting.player.PlayerData;
import me.bhml.elementalmagictesting.spells.SpellElement;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ElementUnlockConfirmGUI {

    public static void open(Player player, PlayerData data, SpellElement elementToUnlock) {
        Inventory gui = Bukkit.createInventory(null, InventoryType.HOPPER, ChatColor.DARK_GREEN + "Unlock " + elementToUnlock.name() + "?");

        // Yes button
        ItemStack yes = new ItemStack(Material.LIME_WOOL);
        ItemMeta yesMeta = yes.getItemMeta();
        yesMeta.setDisplayName(ChatColor.GREEN + "Unlock " + elementToUnlock.name());
        yes.setItemMeta(yesMeta);

        // Cancel button
        ItemStack no = new ItemStack(Material.RED_WOOL);
        ItemMeta noMeta = no.getItemMeta();
        noMeta.setDisplayName(ChatColor.RED + "Cancel");
        no.setItemMeta(noMeta);

        // Fill GUI
        gui.setItem(1, yes);
        gui.setItem(3, no);

        // Temporarily store the element being confirmed (use metadata or a map)
        data.setTempElementToUnlock(elementToUnlock); // Add this method to PlayerData

        player.openInventory(gui);
    }

}
