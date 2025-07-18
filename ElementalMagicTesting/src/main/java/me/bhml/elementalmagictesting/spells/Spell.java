package me.bhml.elementalmagictesting.spells;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

public interface Spell {

    String getName();
    SpellElement getElement();

    default String getDisplayName(){
        return getElement().getColor() + getName();
    }

    long getCooldown();

    void cast(Player player);

    String getId();

    default int calculateXpGain(Player player, List<Entity> affectedTargets) {
        return 0; // override per spell
    }

}


