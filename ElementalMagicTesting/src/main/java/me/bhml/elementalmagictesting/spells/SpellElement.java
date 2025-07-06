package me.bhml.elementalmagictesting.spells;
import org.bukkit.ChatColor;
public enum SpellElement {
    AIR(ChatColor.DARK_AQUA),
    FIRE(ChatColor.RED),
    LIGHTNING(ChatColor.YELLOW),
    WATER(ChatColor.BLUE),
    EARTH(ChatColor.DARK_GREEN);

    private final ChatColor color;

    SpellElement(ChatColor color) {
        this.color = color;
    }

    public ChatColor getColor() {
        return color;
    }

}
