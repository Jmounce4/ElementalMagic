package me.bhml.elementalmagictesting.spells.earth;

import me.bhml.elementalmagictesting.listeners.MobSpawningListener;
import me.bhml.elementalmagictesting.player.PlayerDataManager;
import me.bhml.elementalmagictesting.skills.SkillType;
import me.bhml.elementalmagictesting.spells.PlayerSpellTracker;
import me.bhml.elementalmagictesting.spells.Spell;
import me.bhml.elementalmagictesting.ElementalMagicTesting;
import me.bhml.elementalmagictesting.spells.Spell;
import me.bhml.elementalmagictesting.spells.SpellElement;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

import static me.bhml.elementalmagictesting.spells.SpellUtils.*;


public class Rumble implements Spell {

    @Override
    public String getName() {
        return "rumble";
    }

    @Override
    public SpellElement getElement() {
        return SpellElement.EARTH;
    }

    @Override
    public long getCooldown() {
        return 500; // 0.5 second
    }

    @Override
    public String getId() {
        return "rumble";
    }

    @Override
    public int calculateXpGain(Player player, List<Entity> hitEntities) {
        int xp = 0;
        double base = 2; // first few enemies
        double decay = 0.5; // -0.5 xp per additional

        for (int i = 0; i < hitEntities.size(); i++) {
            double bonus = Math.max(base - i * decay, 1);
            Entity target = hitEntities.get(i);

            //Bonus XP on kill
            if (target.isDead()) {
                bonus += 8;
            }

            if (target instanceof LivingEntity livingTarget) {
                if (MobSpawningListener.isSpawnerMob(livingTarget)) {
                    bonus *= 0.25; // Reduce XP by 75% if spawned from spawner
                }
            }

            xp += bonus;
        }
        return (int)xp;
    }


    @Override
    public void cast(Player player) {
        int durationTicks = 40; // 2 seconds total
        int interval = 10; // every 0.5 seconds
        double radius = 5.0;

        new BukkitRunnable() {
            int ticksRun = 0;

            @Override
            public void run() {
                if (!player.isOnline() || player.isDead()) {
                    cancel();
                    return;
                }

                Location center = player.getLocation();
                Set<UUID> hitEntities = new HashSet<>();
                // --- Damage + knock nearby enemies ---
                for (Entity entity : player.getWorld().getNearbyEntities(center, radius, 3, radius)) {
                    if (!(entity instanceof LivingEntity target)) continue;
                    if (target.equals(player)) continue;
                    if (target.hasMetadata("em_spell_damage")) continue;

                    if (!handleBlockedTargetFeedback(player, target)) continue;

                    hitEntities.add(target.getUniqueId());


                    // Light vertical knock
                    Vector velocity = target.getVelocity();
                    //velocity.setY(0.10);
                    //target.setVelocity(velocity);

                    // Apply spell damage with metadata guard
                    //target.setMetadata("em_spell_damage", new FixedMetadataValue(JavaPlugin.getPlugin(ElementalMagicTesting.class), true));

                    applySpellDamage(player, target, 2.5);

                    //Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(ElementalMagicTesting.class), () ->
                    //target.removeMetadata("em_spell_damage", JavaPlugin.getPlugin(ElementalMagicTesting.class)), 1L);

                    // Particle burst under enemies
                    target.getWorld().spawnParticle(Particle.BLOCK_CRACK, target.getLocation(), 10, 0.3, 0.1, 0.3, Material.DIRT.createBlockData());

                    // --- New: Play hit sound at enemy ---
                    target.getWorld().playSound(target.getLocation(), Sound.BLOCK_GRAVEL_BREAK, 1.0f, 1.2f);

                    // --- New: Camera shake effect for players hit ---
                    if (target instanceof Player targetPlayer) {
                        // Apply a brief slowness effect (level 1, 5 ticks) to simulate camera jitter
                        targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 2, 1, false, false, false));
                    }
                }

                // --- Play quake sound centered on player ---
                center.getWorld().playSound(center, Sound.BLOCK_STONE_BREAK, 1.2f, 0.7f);

                // --- Visual circle around player to mark quake zone ---
                int points = 20; // how many particles in the ring
                for (int i = 0; i < points; i++) {
                    double angle = 2 * Math.PI * i / points;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    Location particleLoc = center.clone().add(x, 0.1, z);
                    center.getWorld().spawnParticle(Particle.BLOCK_DUST, particleLoc, 10, 0.1, 0.1, 0.1, 0, Material.DIRT.createBlockData());
                }

                // --- Slight camera shake for caster ---
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 2, 1, false, false, false));



                List<Entity> hitList = hitEntities.stream()
                            .map(Bukkit::getEntity)
                            .filter(Objects::nonNull)
                            .filter(e -> e instanceof LivingEntity)
                            .toList();


                    //XP Gain for use
                int xp = calculateXpGain(player, hitList);
                PlayerDataManager.get(player).addXp(SkillType.EARTH, xp);
                PlayerDataManager.saveData(player.getUniqueId());
                Bukkit.getLogger().info(xp + " xp for earth");
                clearBlockedTargets(player);





                ticksRun += interval;
                if (ticksRun >= durationTicks) {
                    clearBlockedTargets(player);
                    cancel();
                }

            }
        }.runTaskTimer(JavaPlugin.getPlugin(ElementalMagicTesting.class), 0L, interval);
    }


}
