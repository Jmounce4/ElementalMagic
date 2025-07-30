package me.bhml.elementalmagictesting.spells;

import me.bhml.elementalmagictesting.spells.air.AirGustSpell;
import me.bhml.elementalmagictesting.spells.earth.Rumble;
import me.bhml.elementalmagictesting.spells.earth.TerraPath;
import me.bhml.elementalmagictesting.spells.fire.FireballSpell;
import me.bhml.elementalmagictesting.spells.lightning.Flash;
import me.bhml.elementalmagictesting.spells.lightning.LightningSpell;
import me.bhml.elementalmagictesting.spells.water.HealingAura;
import me.bhml.elementalmagictesting.spells.water.LiquidLance;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SpellRegistry {
    private static final Map<String, Spell> spells = new HashMap<>();


    public static Collection<Spell> getAll() {
        return spells.values();
    }

    // Call this once during plugin startup to register all spells
    public static void registerAll() {
        spells.put("fireball", new FireballSpell());
        spells.put("liquidlance", new LiquidLance());
        spells.put("gust", new AirGustSpell());
        spells.put("rumble", new Rumble());
        spells.put("zap", new LightningSpell());
        spells.put("healingaura", new HealingAura());
        spells.put("flash", new Flash());
        spells.put("terrapath", new TerraPath());
        // Add other spells here as you create them
    }

    public static Spell get(String id) {
        return spells.get(id);
    }
}
