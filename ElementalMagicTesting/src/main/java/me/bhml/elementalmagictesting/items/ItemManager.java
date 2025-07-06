package me.bhml.elementalmagictesting.items;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemManager {

    private static JavaPlugin plugin;

    private static ItemStack spellbookItem;
    private static ItemStack elementalCoreItem;

    public static void init(JavaPlugin pluginInstance) {
        plugin = pluginInstance;

        // Build the custom item
        spellbookItem = createSpellbook();
        elementalCoreItem = createElementalCore();
    }

    private static ItemStack createSpellbook() {
        return SpellbookItem.create(plugin);
    }

    public static ItemStack getSpellbook() {
        return spellbookItem.clone();
    }

    public static boolean isSpellBook(ItemStack item) {
        return ItemUtils.hasKey(item, "spellbook");
    }

    private static ItemStack createElementalCore() {
        return ElementalCore.create(plugin);
    }

    public static ItemStack getElementalCore() {
        return elementalCoreItem.clone();
    }

    public static boolean isElementalCore(ItemStack item) {
        return ItemUtils.hasKey(item, "elemental_core");
    }

}


