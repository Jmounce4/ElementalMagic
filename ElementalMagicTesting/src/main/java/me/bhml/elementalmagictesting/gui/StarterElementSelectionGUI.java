package me.bhml.elementalmagictesting.gui;

import me.bhml.elementalmagictesting.player.PlayerData;
import me.bhml.elementalmagictesting.player.PlayerDataManager;
import me.bhml.elementalmagictesting.spells.PlayerSpellTracker;
import me.bhml.elementalmagictesting.spells.Spell;
import me.bhml.elementalmagictesting.spells.SpellElement;
import me.bhml.elementalmagictesting.spells.SpellRegistry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.List;

public class StarterElementSelectionGUI implements Listener {
    private static final String GUI_TITLE = ChatColor.DARK_PURPLE + "Choose Your Element";

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, GUI_TITLE);

        gui.setItem(1, createElementItem(Material.BLAZE_POWDER, "Fire"));
        gui.setItem(2, createElementItem(Material.WATER_BUCKET, "Water"));
        gui.setItem(3, createElementItem(Material.FEATHER, "Air"));
        gui.setItem(4, createElementItem(Material.DIRT, "Earth"));
        gui.setItem(5, createElementItem(Material.LIGHTNING_ROD, "Lightning"));

        player.openInventory(gui);
    }

    private static ItemStack createElementItem(Material material, String elementName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + elementName + " Element");
        meta.setLore(Collections.singletonList(ChatColor.GRAY + "Click to choose this as your starting element"));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getView().getTitle() == null || !event.getView().getTitle().equals(GUI_TITLE)) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        SpellElement chosen = switch (clicked.getType()) {
            case BLAZE_POWDER -> SpellElement.FIRE;
            case WATER_BUCKET -> SpellElement.WATER;
            case FEATHER -> SpellElement.AIR;
            case DIRT -> SpellElement.EARTH;
            case LIGHTNING_ROD -> SpellElement.LIGHTNING;
            default -> null;
        };

        if (chosen == null) return;

        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return;

        if (data.getUnlockedSpells().size() > 0) {
            player.sendMessage(ChatColor.RED + "You've already chosen a starter element.");
            return;
        }

        // Map to actual spell IDs
        String spellId = switch (chosen) {
            case FIRE -> "fireball";
            case WATER -> "liquidlance";
            case AIR -> "gust";
            case EARTH -> "rumble";
            case LIGHTNING -> "zap";
        };

        Spell starterSpell = SpellRegistry.get(spellId);
        if (starterSpell == null) {
            player.sendMessage(ChatColor.RED + "Could not find starter spell.");
            return;
        }

        data.unlockSpell(spellId);
        data.setLoadoutSpells(List.of(spellId));
        data.setStarterChosen(true);
        PlayerDataManager.saveData(player.getUniqueId());
        PlayerSpellTracker.get(player).refreshAvailableSpells();

        player.sendMessage(ChatColor.GREEN + "You have chosen the " + chosen.name() + " element!");
        player.closeInventory();
    }
}

