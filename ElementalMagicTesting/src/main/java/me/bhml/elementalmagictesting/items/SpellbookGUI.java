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
        addElementItem(gui, player, data, SpellElement.AIR, 11);
        addElementItem(gui, player, data, SpellElement.FIRE, 12);
        addElementItem(gui, player, data, SpellElement.WATER, 13);
        addElementItem(gui, player, data, SpellElement.EARTH, 14);
        addElementItem(gui, player, data, SpellElement.LIGHTNING, 15);

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
                if (spellId != null) {
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

    private static void addElementItem(Inventory gui, Player player, PlayerData data, SpellElement element, int slot) {
        boolean unlocked = data.getUnlockedElements().contains(element);
        Material material = getConcreteForElement(element);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (unlocked) {
            meta.setDisplayName(ChatColor.GOLD + element.name() + " (Unlocked)");
            meta.setLore(List.of(ChatColor.GRAY + "Click to view progress."));
        } else {
            meta.setDisplayName(ChatColor.RED + element.name() + " (Locked)");
            if (data.getPendingElementUnlocks() > 0) {
                meta.setLore(List.of(
                        ChatColor.YELLOW + "Click to unlock this element!",
                        ChatColor.GRAY + "You have " + data.getPendingElementUnlocks() + " unlock(s) available."
                ));
            } else {
                meta.setLore(List.of(ChatColor.GRAY + "Unlocks at higher levels."));
            }
        }

        item.setItemMeta(meta);
        gui.setItem(slot, item);
    }
}
