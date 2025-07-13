package me.bhml.elementalmagictesting.items;

import me.bhml.elementalmagictesting.player.PlayerData;
import me.bhml.elementalmagictesting.player.PlayerDataManager;
import me.bhml.elementalmagictesting.skills.SkillProgress;
import me.bhml.elementalmagictesting.skills.SkillType;
import me.bhml.elementalmagictesting.spells.PlayerSpellTracker;
import me.bhml.elementalmagictesting.spells.Spell;
import me.bhml.elementalmagictesting.spells.SpellElement;
import me.bhml.elementalmagictesting.spells.SpellRegistry;
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
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + "Spellbook");
        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return;

        // Magic Mastery
        SkillProgress mastery = data.getSkillProgress(SkillType.MAGIC_MASTERY);
        gui.setItem(4, buildSkillItem("Magic Mastery", Material.BOOK, mastery.getLevel(), mastery.getXp()));

        // Elements
        gui.setItem(11, buildSkillItem("Air", Material.LIGHT_BLUE_CONCRETE, data.getSkillProgress(SkillType.AIR).getLevel(), data.getSkillProgress(SkillType.AIR).getXp()));
        gui.setItem(12, buildSkillItem("Fire", Material.RED_CONCRETE, data.getSkillProgress(SkillType.FIRE).getLevel(), data.getSkillProgress(SkillType.FIRE).getXp()));
        gui.setItem(13, buildSkillItem("Water", Material.BLUE_CONCRETE, data.getSkillProgress(SkillType.WATER).getLevel(), data.getSkillProgress(SkillType.WATER).getXp()));
        gui.setItem(14, buildSkillItem("Earth", Material.GREEN_CONCRETE, data.getSkillProgress(SkillType.EARTH).getLevel(), data.getSkillProgress(SkillType.EARTH).getXp()));
        gui.setItem(15, buildSkillItem("Lightning", Material.YELLOW_CONCRETE, data.getSkillProgress(SkillType.LIGHTNING).getLevel(), data.getSkillProgress(SkillType.LIGHTNING).getXp()));

        // Physical Skills
        gui.setItem(21, buildSkillItem("Melee", Material.IRON_SWORD, data.getSkillProgress(SkillType.MELEE).getLevel(), data.getSkillProgress(SkillType.MELEE).getXp()));
        gui.setItem(22, buildSkillItem("Archery", Material.BOW, data.getSkillProgress(SkillType.ARCHERY).getLevel(), data.getSkillProgress(SkillType.ARCHERY).getXp()));
        gui.setItem(23, buildSkillItem("Mining", Material.IRON_PICKAXE, data.getSkillProgress(SkillType.MINING).getLevel(), data.getSkillProgress(SkillType.MINING).getXp()));


        // Loadout Spells (slots 39-43: center bottom row)
        List<String> loadout = data.getLoadoutSpells();
        for (int i = 0; i < 5; i++) {
            int slot = 38 + i;
            if (i < loadout.size()) {
                String spellId = loadout.get(i);
                Spell spell = SpellRegistry.get(spellId);
                if (spell != null) {
                    Material elementMat = getConcreteForElement(spell.getElement());
                    ItemStack item = new ItemStack(elementMat);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(ChatColor.AQUA + spell.getName());
                    meta.setLore(List.of(
                            ChatColor.GRAY + "Element: " + spell.getElement().getColor() + spell.getElement().name(),
                            ChatColor.GRAY + "Cooldown: " + spell.getCooldown() + " ticks"
                    ));
                    item.setItemMeta(meta);
                    gui.setItem(slot, item);
                }
            } else {
                // Empty spell slot
                ItemStack empty = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                ItemMeta meta = empty.getItemMeta();
                meta.setDisplayName(ChatColor.DARK_GRAY + "Empty Spell Slot");
                empty.setItemMeta(meta);
                gui.setItem(slot, empty);
            }
        }

        player.openInventory(gui);
    }

    private static ItemStack buildSkillItem(String name, Material icon, int level, double xp) {
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + name + ChatColor.GRAY + " (Lvl " + level + ")");
        meta.setLore(List.of(ChatColor.GRAY + "XP: " + String.format("%.1f", xp)));
        item.setItemMeta(meta);
        return item;
    }

    private static Material getConcreteForElement(SpellElement element) {
        return switch (element) {
            case AIR -> Material.LIGHT_BLUE_CONCRETE;
            case FIRE -> Material.RED_CONCRETE;
            case WATER -> Material.BLUE_CONCRETE;
            case EARTH -> Material.GREEN_CONCRETE;
            case LIGHTNING -> Material.YELLOW_CONCRETE;
        };
    }
}
