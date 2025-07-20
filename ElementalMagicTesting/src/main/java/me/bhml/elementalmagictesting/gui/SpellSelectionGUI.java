package me.bhml.elementalmagictesting.gui;

import me.bhml.elementalmagictesting.items.SpellbookGUI;
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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;


public class SpellSelectionGUI {
    // Maps each GUI slot (playerUUID:index) to the corresponding spell ID
    private static final Map<String, String> guiSlotToSpellId = new HashMap<>();
    // Tracks which loadout slot the player is currently editing
    private static final Map<UUID, Integer> playerSelectionSlot = new HashMap<>();

    /**
     * Opens the Spell Selection GUI for the given player and loadout slot index.
     * @param player The player opening the GUI
     * @param loadoutSlot The index (0-4) in the player's loadout to assign
     */
    public static void open(Player player, int loadoutSlot) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.BLUE + "Select a Spell");
        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return;

        // Track which slot we're editing and clear old mappings
        playerSelectionSlot.put(player.getUniqueId(), loadoutSlot);
        guiSlotToSpellId.keySet().removeIf(k -> k.startsWith(player.getUniqueId().toString()));

        // Avoid duplicates: get currently equipped IDs
        Set<String> equipped = new HashSet<>(data.getLoadoutSpells());

        // Show only unlocked but not equipped spells
        List<Spell> available = SpellRegistry.getAll().stream()
                .filter(spell -> data.getUnlockedSpells().contains(spell.getId()))
                .filter(spell -> !equipped.contains(spell.getId()))
                .toList();

        // Populate items and record slot→spellID mapping
        for (int i = 0; i < available.size(); i++) {
            Spell spell = available.get(i);
            ItemStack item = new ItemStack(getConcreteForElement(spell.getElement()));
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA + spell.getName());
            meta.setLore(List.of(
                    ChatColor.GRAY + "Element: " + spell.getElement().getColor() + spell.getElement().name(),
                    ChatColor.GRAY + "Cooldown: " + spell.getCooldown() + " ticks",
                    ChatColor.YELLOW + "Click to assign to Loadout Slot " + (loadoutSlot + 1)
            ));
            item.setItemMeta(meta);

            // map this GUI index to the spell ID for click handler
            guiSlotToSpellId.put(player.getUniqueId() + ":" + i, spell.getId());
            gui.setItem(i, item);
        }

        player.openInventory(gui);
    }

    /**
     * Handle a click in the "Select a Spell" GUI: equip the chosen spell.
     */
    public static void handleClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!e.getView().getTitle().equals(ChatColor.BLUE + "Select a Spell")) return;
        e.setCancelled(true);

        int slot = e.getRawSlot();
        String key = player.getUniqueId() + ":" + slot;
        if (!guiSlotToSpellId.containsKey(key)) return;

        String spellId = guiSlotToSpellId.get(key);
        Integer loadoutSlot = playerSelectionSlot.get(player.getUniqueId());
        if (spellId == null || loadoutSlot == null) return;

        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return;

        // Build a mutable copy of the loadout
        List<String> loadout = new ArrayList<>(data.getLoadoutSpells());
        while (loadout.size() <= loadoutSlot) loadout.add(null);
        loadout.set(loadoutSlot, spellId);
        data.setLoadoutSpells(loadout.stream().filter(Objects::nonNull).collect(Collectors.toList()));

        // Persist and update in‐memory tracker
        PlayerDataManager.saveData(player.getUniqueId());
        PlayerSpellTracker.get(player).refreshAvailableSpells(); // <<— here

        player.sendMessage(ChatColor.GREEN + "Equipped "
                + SpellRegistry.get(spellId).getName()
                + " in slot " + (loadoutSlot + 1));

        // Clean up and reopen main GUI
        guiSlotToSpellId.keySet().removeIf(k -> k.startsWith(player.getUniqueId().toString()));
        playerSelectionSlot.remove(player.getUniqueId());
        SpellbookGUI.open(player);
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
