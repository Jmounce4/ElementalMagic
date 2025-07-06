package me.bhml.elementalmagictesting.spells;
import org.bukkit.entity.Player;
public interface Spell {

    String getName();
    SpellElement getElement();

    default String getDisplayName(){
        return getElement().getColor() + getName();
    }

    long getCooldown();

    void cast(Player player);

}
