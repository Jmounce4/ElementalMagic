package me.bhml.elementalmagictesting.spells.lightning;

import me.bhml.elementalmagictesting.spells.Spell;

import me.bhml.elementalmagictesting.spells.SpellElement;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import me.bhml.elementalmagictesting.spells.SpellUtils;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Flash implements Spell {
    @Override
    public String getName() {
        return "Flash";
    }

    @Override
    public long getCooldown() {
        return 500; // 0.5 second
    }

    @Override
    public String getId() {
        return "flash";
    }

    public SpellElement getElement() {
        return SpellElement.LIGHTNING;
    }



    private static final double MAX_DISTANCE = 11.0;
    private static final double DAMAGE = 5.0;
    private static final double STEP_SIZE = 0.2;
    private static final double HIT_RADIUS = 0.8;

    @Override
    public void cast(Player player) {
        Location origin = player.getEyeLocation();
        Vector direction = origin.getDirection().normalize();
        World world = player.getWorld();

        Location lastSafe = origin.clone(); // Start from eye location
        Location checkPoint;

        // STEP 1: Raycast forward
        for (double d = 0; d <= MAX_DISTANCE; d += STEP_SIZE) {
            checkPoint = origin.clone().add(direction.clone().multiply(d));

            if (isLocationSafe(checkPoint)) {
                lastSafe = checkPoint.clone();
            } else {
                break; // Found a wall — stop and use last safe
            }
        }

        // STEP 2: Particle trail + damage
        Set<LivingEntity> damaged = new HashSet<>();
        double distance = origin.distance(lastSafe);
        for (double d = 0; d <= distance; d += STEP_SIZE) {
            Location point = origin.clone().add(direction.clone().multiply(d));
            world.spawnParticle(Particle.ELECTRIC_SPARK, point, 2, 0.1, 0.1, 0.1, 0);

            for (Entity entity : point.getWorld().getNearbyEntities(point, HIT_RADIUS, HIT_RADIUS, HIT_RADIUS)) {
                if (entity instanceof LivingEntity target && target != player && !damaged.contains(target)) {
                    SpellUtils.applySpellDamage(player, target, DAMAGE);
                    damaged.add(target);
                }
            }
        }

        // STEP 3: Finalize and teleport
        Location finalDestination = lastSafe.clone().subtract(direction.clone().multiply(0.8));
        if (!isLocationSafe(finalDestination)) {
            finalDestination = lastSafe.clone(); // Fallback if too close to wall
        }
        finalDestination.setYaw(player.getLocation().getYaw());
        finalDestination.setPitch(player.getLocation().getPitch());

        player.teleport(finalDestination);
        world.playSound(finalDestination, Sound.ENTITY_ENDERMAN_TELEPORT, 0.3f, 2.0f);
    }

    private boolean isLocationSafe(Location loc) {
        World world = loc.getWorld();
        Location feet = loc.clone();
        Location head = loc.clone().add(0, 1, 0);
        return !feet.getBlock().getType().isSolid() && !head.getBlock().getType().isSolid();
    }





}

