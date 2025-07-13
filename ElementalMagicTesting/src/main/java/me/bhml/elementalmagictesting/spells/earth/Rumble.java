package me.bhml.elementalmagictesting.spells.earth;

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
        return "Rumble";
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

                // --- Damage + knock nearby enemies ---
                for (Entity entity : player.getWorld().getNearbyEntities(center, radius, 3, radius)) {
                    if (!(entity instanceof LivingEntity target)) continue;
                    if (target.equals(player)) continue;
                    if (target.hasMetadata("em_spell_damage")) continue;

                    if(!handleBlockedTargetFeedback(player, target)) continue;




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

                ticksRun += interval;
                if (ticksRun >= durationTicks) {
                    clearBlockedTargets(player);
                    cancel();
                }
            }
        }.runTaskTimer(JavaPlugin.getPlugin(ElementalMagicTesting.class), 0L, interval);
    }


}
