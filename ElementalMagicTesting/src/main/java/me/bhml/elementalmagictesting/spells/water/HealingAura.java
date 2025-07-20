package me.bhml.elementalmagictesting.spells.water;

import me.bhml.elementalmagictesting.spells.Spell;

import me.bhml.elementalmagictesting.player.PlayerData;
import me.bhml.elementalmagictesting.player.PlayerDataManager;
import me.bhml.elementalmagictesting.skills.SkillType;
import me.bhml.elementalmagictesting.spells.Spell;
import me.bhml.elementalmagictesting.spells.SpellElement;
import me.bhml.elementalmagictesting.player.TargetingUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

import static me.bhml.elementalmagictesting.spells.SpellUtils.clearBlockedTargets;

public class HealingAura implements Spell {

    private static final double RADIUS = 8.0;
    private static final double HEAL_AMOUNT = 4.0; // 2 hearts
    private static final int REGEN_DURATION = 200; // 10 seconds
    private static final int REGEN_AMPLIFIER = 0;

    @Override
    public String getName() {
        return "Healing Aura";
    }

    @Override
    public SpellElement getElement() {
        return SpellElement.WATER;
    }

    @Override
    public long getCooldown() {
        return 500; // 0.5 second
    }

    @Override
    public String getId() {
        return "healingaura";
    }

    @Override
    public int calculateXpGain(Player player, List<Entity> hitEntities) {
        int totalXp = 0;

        for (Entity entity : hitEntities) {
            if (!(entity instanceof LivingEntity)) continue;
            LivingEntity living = (LivingEntity) entity;

            double missingHealth = living.getMaxHealth() - living.getHealth();
            double healed = Math.min(HEAL_AMOUNT, missingHealth);

            // XP formula: 3 XP per HP restored
            totalXp += (int) healed*3;
        }

        return totalXp;
    }


    @Override
    public void cast(Player caster) {
        Location center = caster.getLocation();
        World world = caster.getWorld();

        Collection<LivingEntity> nearby = world.getNearbyLivingEntities(center, RADIUS);
        Set<LivingEntity> healedEntities = new HashSet<>();

        for (LivingEntity entity : nearby) {
            if (!TargetingUtils.canHeal(caster, entity)) continue;

            double before = entity.getHealth();
            double after = Math.min(entity.getHealth() + HEAL_AMOUNT, entity.getMaxHealth());

            if (after > before) {
                entity.setHealth(after);
                healedEntities.add(entity);

                entity.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, entity.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.1);
            }

            entity.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, REGEN_DURATION, REGEN_AMPLIFIER));
        }

        // Show aura visual
        world.spawnParticle(Particle.DRIP_WATER, center, 50, RADIUS / 2, 1, RADIUS / 2, 0.2);

        // Calculate and apply XP
        int xp = calculateXpGain(caster, new ArrayList<>(healedEntities));
        PlayerDataManager.get(caster).addXp(SkillType.WATER, xp);
        PlayerDataManager.saveData(caster.getUniqueId());
        Bukkit.getLogger().info(xp + " xp for water");
        clearBlockedTargets(caster);
    }




}
