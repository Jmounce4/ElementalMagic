package me.bhml.elementalmagictesting.spells.air;

import me.bhml.elementalmagictesting.ElementalMagicTesting;
import me.bhml.elementalmagictesting.listeners.MobSpawningListener;
import me.bhml.elementalmagictesting.player.PlayerDataManager;
import me.bhml.elementalmagictesting.skills.SkillType;
import me.bhml.elementalmagictesting.spells.PlayerSpellTracker;
import me.bhml.elementalmagictesting.spells.Spell;
import me.bhml.elementalmagictesting.spells.SpellElement;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

import static me.bhml.elementalmagictesting.spells.SpellUtils.*;

public class AirGustSpell implements Spell {

    public String getName(){
        return "Gust";
    }

    public SpellElement getElement() {
        return SpellElement.AIR;
    }


    @Override
    public long getCooldown() {
        return 500; // .5 second
    }

    @Override
    public String getId() {
        return "gust";
    }

    @Override
    public int calculateXpGain(Player player, List<Entity> hitEntities) {
        int xp = 0;
        int base = 4; // first few enemies
        int decay = 1; // -1 xp per additional

        for (int i = 0; i < hitEntities.size(); i++) {
            int bonus = Math.max(base - i * decay, 1);
            Entity target = hitEntities.get(i);

            //Bonus XP on kill
            if (target.isDead()) {
                bonus += 2;
            }

            if (target instanceof LivingEntity livingTarget) {
                if (MobSpawningListener.isSpawnerMob(livingTarget)) {
                    bonus *= 0.25; // Reduce XP by 75% if spawned from spawner
                }
            }

            xp += bonus;
        }
        return xp;
    }



    @Override
    public void cast(Player player) {
        Location origin = player.getEyeLocation();
        Vector direction = origin.getDirection().normalize();
        World world = player.getWorld();

        double maxDistance = 15.0;
        int steps = 6; //Essentially speed, Higher = Slower
        double initialRadius = 1.5;
        double finalRadius = 8.0;

        Set<UUID> hitEntities = new HashSet<>();

        new BukkitRunnable() {
            int currentStep = 0;

            @Override
            public void run() {
                if (currentStep > steps) {
                    // Clear blocked targets now that the spell cast is done
                    clearBlockedTargets(player);
                    cancel();
                    return;
                }

                double progress = currentStep / (double) steps;
                double forwardDistance = maxDistance * progress;
                double currentRadius = initialRadius + (finalRadius - initialRadius) * progress;

                Location stepCenter = origin.clone().add(direction.clone().multiply(forwardDistance));

                Vector perp = new Vector(0, 1, 0).crossProduct(direction).normalize();

                int particleCount = (int) (currentRadius * 1);

                for (int i = -particleCount; i <= particleCount; i++) {
                    double fraction = i / (double) particleCount;
                    Vector offset = perp.clone().multiply(fraction * currentRadius);
                    Location particleLocation = stepCenter.clone().add(offset).add(0, 1, 0);
                    world.spawnParticle(Particle.SWEEP_ATTACK, particleLocation, 1, 0, 0, 0, 0);
                }

                // Hit detection
                for (Entity entity : world.getNearbyEntities(stepCenter, currentRadius, 1, currentRadius)) {
                    if (!(entity instanceof LivingEntity target)) continue;
                    if (target.equals(player)) continue;
                    if (hitEntities.contains(target.getUniqueId())) continue;

                    if(!handleBlockedTargetFeedback(player, target)) continue;

                    //My hit detection
                    if (!hasClearShot(player, target)) continue;

                    Vector toTarget = target.getLocation().toVector().subtract(origin.toVector());
                    double angle = Math.toDegrees(direction.angle(toTarget));
                    if (angle > 60.0) continue; // Only entities roughly in front
                    if (toTarget.length() > maxDistance) continue; // Past max range

                    hitEntities.add(target.getUniqueId());

                    // Knockback away from player
                    Vector knockback = toTarget.normalize().multiply(2.5);
                    knockback.setY(1.05);
                    target.setVelocity(knockback);

                    // Apply damage (tweak damage as needed)
                    applySpellDamage(player, target, 4.0);

                    // Play hit sound
                    world.playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 1.6f);
                }


                //Attempt to debug XP gain calced many times.
                if (currentStep == steps-1) {
                    List<Entity> hitList = hitEntities.stream()
                            .map(Bukkit::getEntity)
                            .filter(Objects::nonNull)
                            .filter(e -> e instanceof LivingEntity)
                            .toList();


                    //XP Gain for use
                    int xp = calculateXpGain(player, hitList);
                    PlayerDataManager.get(player).addXp(SkillType.AIR, xp);
                    PlayerDataManager.saveData(player.getUniqueId());
                    Bukkit.getLogger().info(xp + " xp for air");
                    clearBlockedTargets(player);
                }

                currentStep++;
            }







        }.runTaskTimer(JavaPlugin.getPlugin(ElementalMagicTesting.class), 0L, 1L);

        // Initial cast effect
        world.spawnParticle(Particle.CLOUD, origin, 20, 0.5, 0.5, 0.5, 0.05);
        world.playSound(origin, Sound.ENTITY_PHANTOM_FLAP, 1.2f, 2.0f);
    }





    /* Original Air Slashes
    @Override
    public void cast(Player player) {
        Location origin = player.getEyeLocation();
        Vector direction = origin.getDirection().normalize();

        for (Entity entity : player.getNearbyEntities(10, 10, 10)) {
            if (!(entity instanceof LivingEntity target)) continue;
            if (entity.equals(player)) continue;

            Vector toTarget = entity.getLocation().toVector().subtract(origin.toVector());
            double angle = direction.angle(toTarget);
            if (Math.toDegrees(angle) > 45) continue;

            double distance = entity.getLocation().distance(origin);
            if (distance > 10) continue;

            // Scale: 1.0 = point-blank, 0.0 = 10 blocks away
            double strength = 1.0 - (distance / 10.0);
            strength = Math.max(0, Math.min(strength, 1)); // clamp

            // ---- 1. Initial Knockback ----
            Vector knockback = direction.clone().multiply(3.0 * strength); // more distance
            knockback.setY(0.2); // just enough to nudge off ground
            entity.setVelocity(knockback);

            // ---- 2. Delayed Vertical Launch ----
            double verticalBoost = 0.1 + (0.2 * strength); // 1.0 to 2.0 lift
            Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(ElementalMagicTesting.class), () -> {
                Vector boostedVel = entity.getVelocity().clone();
                boostedVel.setY(boostedVel.getY() + verticalBoost); // Add upward force without resetting X/Z
                entity.setVelocity(boostedVel);

                // Floaty particle burst
                entity.getWorld().spawnParticle(Particle.CLOUD, entity.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.05);
            }, 4L);


            //Debugging - Target
            LivingEntity finalTarget = target;

            // ---- 3. Air Slash Hits ----
            int slashes = (int) (2 + (3 * strength)); // 2 to 5 hits based on closeness
            double damagePerHit = 1.5 + (0.5 * strength); // light damage, slightly stronger close up

            for (int i = 0; i < slashes; i++) {
                int delay = i * 2;
                if (!hasClearShot(player,finalTarget)) continue;
                Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(ElementalMagicTesting.class), () -> {
                    if (!finalTarget.isDead() && finalTarget.getWorld().equals(player.getWorld())) {

                        // Mark as spell damage
                        //finalTarget.setMetadata("em_spell_damage", new FixedMetadataValue(JavaPlugin.getPlugin(ElementalMagicTesting.class), true));


                        applySpellDamage(player, target, damagePerHit);

                        // Remove metadata 1 tick later
                        //Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(ElementalMagicTesting.class), () -> {
                            //finalTarget.removeMetadata("em_spell_damage", JavaPlugin.getPlugin(ElementalMagicTesting.class));
                        //}, 1L);

                        // Effects
                        finalTarget.getWorld().spawnParticle(Particle.SWEEP_ATTACK, finalTarget.getLocation().add(0, 1, 0), 1);
                        finalTarget.getWorld().playSound(finalTarget.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 1.8f);
                    }
                }, delay);
            }

            // Gust sound per entity hit
            player.getWorld().playSound(entity.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 0.8f, 1.6f);
        }

        // ---- 4. Casting Feedback at Player ----
        player.getWorld().spawnParticle(Particle.CLOUD, origin, 10, 0.8, 0.8, 0.8, 0.08);
        //For minimal particle
        Location particleLocation = origin.clone().add(direction.multiply(1)); // 1 block ahead
        player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, particleLocation, 2, 0.2, 0.2, 0.2, 0.02);
        player.getWorld().playSound(origin, Sound.ENTITY_PHANTOM_FLAP, 1f, 2f);
    }*/
}


