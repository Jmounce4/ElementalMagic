package me.bhml.elementalmagictesting.spells.fire;

import me.bhml.elementalmagictesting.ElementalMagicTesting;
import me.bhml.elementalmagictesting.player.TargetingUtils;
import me.bhml.elementalmagictesting.spells.PlayerSpellTracker;
import me.bhml.elementalmagictesting.spells.Spell;
import me.bhml.elementalmagictesting.spells.SpellElement;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static me.bhml.elementalmagictesting.spells.SpellUtils.*;

public class FireballSpell implements Spell {

    public String getName(){
        return "Fireball";
    }

    public SpellElement getElement() {
        return SpellElement.FIRE;
    }

    /*
    public void cast(Player player) {
        Location eye = player.getEyeLocation();
        Vector dir = eye.getDirection();
        Fireball fireball = player.getWorld().spawn(eye.add(dir.multiply(1)), Fireball.class);
        fireball.setVelocity(dir.multiply(1.5));
        fireball.setIsIncendiary(false); // prevent setting blocks on fire
        fireball.setYield(2.0F); // explosion strength
    }
    Basic Minecraft fireball spell when testing*/



    @Override
    public long getCooldown() {
        return 500; // 0.5 second
    }

    //Ember?

    @Override
    public void cast(Player player) {
        Location origin = player.getEyeLocation();
        Vector direction = origin.getDirection().normalize().multiply(1.0); // Launch velocity
        World world = player.getWorld();

        int maxTicks = 2000; // How long until it disappears
        double damage = 5.0;
        double hitRadius = 1.3;
        double gravity = -0.015;

        Vector currentVelocity = direction.clone();
        Location current = origin.clone();
        Set<UUID> hitEntities = new HashSet<>();
        AtomicBoolean exploded = new AtomicBoolean(false);

        world.playSound(current, Sound.ENTITY_GHAST_SHOOT, 0.2f, 2.0f);

        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (exploded.get() || current.getY() < 1) {
                    clearBlockedTargets(player);
                    cancel();
                    return;
                }

                Vector step = currentVelocity.clone().normalize().multiply(0.2);
                int steps = (int) Math.ceil(currentVelocity.length() / 0.2);

                for (int i = 0; i < steps; i++) {
                    current.add(step);

                    // Block collision
                    if (!world.getBlockAt(current).isPassable()) {
                        Vector offset = step.clone().multiply(-0.8);
                        Location safeExplosionLoc = current.clone().add(offset);
                        triggerExplosion(safeExplosionLoc);
                        return;
                    }

                    // Visuals
                    world.spawnParticle(Particle.FLAME, current, 2, 0.05, 0.05, 0.05, 0.01);
                    world.spawnParticle(Particle.SMOKE_NORMAL, current, 1, 0.05, 0.05, 0.05, 0.01);
                    world.playSound(current, Sound.BLOCK_CAMPFIRE_CRACKLE, 0.1f, 2.0f);

                    // Entity hit check
                    for (Entity entity : world.getNearbyEntities(current, hitRadius, hitRadius, hitRadius)) {
                        if (!(entity instanceof LivingEntity target)) continue;

                        //Targetting Check
                        if (!handleBlockedTargetFeedback(player, target)) continue;
                        //Dont hit self
                        if (target.equals(player)) continue;
                        if (hitEntities.contains(target.getUniqueId())) continue;
                        //Block Detection
                        if (!hasClearShot(player, target)) continue;

                        hitEntities.add(target.getUniqueId());
                        target.setFireTicks(100);
                        applySpellDamage(player, target, damage);
                        triggerExplosion(target.getLocation());
                        return;
                    }
                }

                // Apply gravity after stepping
                currentVelocity.setY(currentVelocity.getY() + gravity);

                tick++;
                if (tick > maxTicks) {
                    clearBlockedTargets(player);
                    cancel();
                }
            }

            private void triggerExplosion(Location loc) {
                exploded.set(true);

                world.spawnParticle(Particle.FLAME, loc, 40, 0.5, 0.5, 0.5, 0.02);
                world.spawnParticle(Particle.FLAME, loc, 20, 1.8, 1.8, 1.8, 0.02);
                world.spawnParticle(Particle.LAVA, loc, 10, 0.4, 0.4, 0.4, 0.02);
                world.spawnParticle(Particle.SMOKE_LARGE, loc, 15, 0.5, 0.5, 0.5, 0.03);
                world.playSound(loc, Sound.ITEM_FIRECHARGE_USE, 0.8f, 1.5f);

                for (Entity aoe : world.getNearbyEntities(loc, 1.8, 1.8, 1.8)) {
                    if (!(aoe instanceof LivingEntity target)) continue;
                    //Targetting Check
                    if (!handleBlockedTargetFeedback(player, target)) continue;
                    if (target.equals(player)) continue;
                    if (hitEntities.contains(target.getUniqueId())) continue;

                    if (!isExposedTo(loc, target)) continue;

                    hitEntities.add(target.getUniqueId());
                    target.setFireTicks(100);
                    applySpellDamage(player, target, damage);
                    world.spawnParticle(Particle.FLAME, target.getLocation().add(0, 1, 0), 15, 0.3, 0.3, 0.3, 0.02);
                }
                clearBlockedTargets(player);
            }
        }.runTaskTimer(JavaPlugin.getPlugin(ElementalMagicTesting.class), 0L, 1L);

    }

}