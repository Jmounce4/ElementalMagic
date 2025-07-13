package me.bhml.elementalmagictesting.spells.lightning;

import me.bhml.elementalmagictesting.ElementalMagicTesting;
import me.bhml.elementalmagictesting.spells.PlayerSpellTracker;
import me.bhml.elementalmagictesting.spells.Spell;
import me.bhml.elementalmagictesting.spells.SpellElement;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

import static me.bhml.elementalmagictesting.spells.SpellUtils.*;

public class LightningSpell implements Spell {

    public String getName(){
        return "Zap";
    }

    /*public static void fakeRay(Player player) {
        Location eye = player.getEyeLocation();
        Vector direction = eye.getDirection().normalize();

        for (int i = 0; i < 50; i++) {
            Location point = eye.clone().add(direction.clone().multiply(i));

            player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, point, 2, 0.1, 0.1, 0.1);
            player.getWorld().playSound(point, Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 0.2F, 2F);

            // Optional: Damage first entity hit
            for (Entity nearby : point.getWorld().getNearbyEntities(point, 0.5, 0.5, 0.5)) {
                if (nearby instanceof LivingEntity && !nearby.equals(player)) {
                    ((LivingEntity) nearby).damage(6, player);
                    point.getWorld().strikeLightningEffect(point); // effect only, no fire
                    return;
                }
            }
        }
    }*/

    public SpellElement getElement() {
        return SpellElement.LIGHTNING;
    }

    @Override
    public long getCooldown() {
        return 500; // 0.5 second
    }

    @Override
    public String getId() {
        return "zap";
    }


//zap
    @Override
    public void cast(Player player) {
        Location eye   = player.getEyeLocation();
        World world    = player.getWorld();
        Vector baseDir = eye.getDirection().normalize();

        double maxRange    = 16.0;
        double segmentLen  = 1.0;
        double stepSubdiv  = 0.2;   // how fine the interpolation is
        double jitterAmp   = 1.2;   // wider swing
        double hitRadius   = 1.2;
        double damage      = 5.0;

        int segments = (int)(maxRange / segmentLen);
        Random rand = new Random();

        Set<UUID> hit = new HashSet<>();
        LivingEntity struck     = null;
        Location struckLocation = null;

        // start point
        Vector prev = eye.toVector();

        // build jagged bolt in one tick
        for (int i = 1; i <= segments; i++) {
            // along the straight line
            Vector along = eye.toVector().add(baseDir.clone().multiply(i * segmentLen));

            // random jitter
            Vector jitter = new Vector(
                    (rand.nextDouble() * 2 - 1) * jitterAmp,
                    (rand.nextDouble() * 2 - 1) * jitterAmp,
                    (rand.nextDouble() * 2 - 1) * jitterAmp
            );

            Vector next = along.add(jitter);

            // draw continuous line from prev to next
            Vector delta = next.clone().subtract(prev);
            double dist = delta.length();
            Vector step = delta.clone().normalize().multiply(stepSubdiv);
            int   points = (int)(dist / stepSubdiv);

            for (int p = 0; p < points; p++) {
                Vector point = prev.clone().add(step.clone().multiply(p));
                Location loc = point.toLocation(world);

                /* Previous block detection
                if (!world.getBlockAt(loc).isPassable()) {
                    world.spawnParticle(Particle.SMOKE_NORMAL, loc, 8, 0.2, 0.2, 0.2, 0.01);
                    world.playSound(loc, Sound.BLOCK_STONE_HIT, 0.8f, 1.2f);
                    return;
                }*/


                world.spawnParticle(Particle.ELECTRIC_SPARK, loc, 4, 0,0,0, 0);
                world.playSound(loc, Sound.ENTITY_BEE_STING, 0.005f, 0.4f);


                // hit check
                for (Entity e : world.getNearbyEntities(loc, hitRadius, hitRadius, hitRadius)) {
                    if (!(e instanceof LivingEntity target)) continue;
                    if (target.equals(player)) continue;
                    if (hit.contains(target.getUniqueId())) continue;

                    if (!handleBlockedTargetFeedback(player, target)) continue;

                    //Block Hit Detection
                    if (!hasClearShot(player, target)) continue;
                    //(!player.hasLineOfSight(target)) continue;

                    hit.add(target.getUniqueId());
                    struck = target;
                    struckLocation = target.getEyeLocation().add(0, 0.5, 0);

                    // you can break out of p‑loop, but let’s finish drawing this segment
                    break;
                }
            }

            prev = next;

            if (struck != null) break;
        }

        // final burst and damage
        if (struck != null) {
            applySpellDamage(player, struck, damage);
            world.spawnParticle(Particle.ELECTRIC_SPARK, struckLocation, 12, 0.3,0.3,0.3, 0);
            world.playSound(struckLocation, Sound.ENTITY_EVOKER_FANGS_ATTACK, 0.6f, 1.8f);
        }
        // Clear blocked targets now that the spell cast is done
        clearBlockedTargets(player);
    }


    /*
    public void cast(Player player) {
        Snowball sb = player.launchProjectile(Snowball.class);
        sb.setCustomName("LightningOrb");
        sb.setVelocity(player.getLocation().getDirection().multiply(2.5));
        sb.setGravity(false);

        sb.setGlowing(true); // optional glow effect

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!sb.isValid() || sb.isOnGround()) {
                    sb.getWorld().strikeLightningEffect(sb.getLocation());
                    for (Entity entity : sb.getNearbyEntities(1, 1, 1)) {
                        if (entity instanceof LivingEntity le && !entity.equals(player)) {
                            // --- MARK the target as taking spell damage ---
                            le.setMetadata("em_spell_damage", new FixedMetadataValue(JavaPlugin.getPlugin(ElementalMagicTesting.class), true));

                            // Apply damage
                            le.damage(8, player);

                            // Optional: clean metadata 1 tick later
                            Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(ElementalMagicTesting.class), () -> {
                                le.removeMetadata("em_spell_damage", JavaPlugin.getPlugin(ElementalMagicTesting.class));
                            }, 1L);
                        }
                    }
                    sb.remove();
                    cancel();
                    return;
                }

                sb.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, sb.getLocation(), 3, 0.1, 0.1, 0.1);
            }
        }.runTaskTimer(JavaPlugin.getPlugin(ElementalMagicTesting.class), 0L, 1L);
    }
    */



    /*public static void castKiBlast(Player player) {
        Snowball snowball = player.launchProjectile(Snowball.class);
        snowball.setVelocity(player.getLocation().getDirection().multiply(4.5));
        snowball.setGlowing(true);
        snowball.setCustomName("KiBlast");
        snowball.setCustomNameVisible(false);

        World world = player.getWorld();

        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (snowball.isDead() || !snowball.isValid()) {
                    this.cancel();
                    return;
                }

                Location loc = snowball.getLocation();

                // Bright purple glow around the projectile
                Particle.DustOptions dust = new Particle.DustOptions(Color.FUCHSIA, 1.5F);
                for (int i = 0; i < 20; i++) {
                    double angle = 2 * Math.PI * i / 20;
                    double radius = 0.6;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    Location particleLoc = loc.clone().add(x, 0, z);
                    loc.getWorld().spawnParticle(Particle.REDSTONE, particleLoc, 1, dust);
                }

                // Sparkling core
                loc.getWorld().spawnParticle(Particle.END_ROD, loc, 8, 0.15, 0.15, 0.15, 0.02);

                // Glowing trail behind snowball
                Vector backward = snowball.getVelocity().clone().normalize().multiply(-0.5);
                for (int i = 1; i <= 5; i++) {
                    Location trailLoc = loc.clone().add(backward.clone().multiply(i * 0.4));
                    trailLoc.getWorld().spawnParticle(Particle.REDSTONE, trailLoc, 3, 0.1, 0.1, 0.1, dust);
                    trailLoc.getWorld().spawnParticle(Particle.END_ROD, trailLoc, 2, 0.05, 0.05, 0.05, 0.01);
                }

                // Ambient sound
                if (tick % 10 == 0) {
                    loc.getWorld().playSound(loc, Sound.BLOCK_BEACON_AMBIENT, 0.5f, 1.8f);
                }

                tick++;
            }
        }.runTaskTimer(JavaPlugin.getPlugin(ElementalMagicTesting.class), 0L, 1L);

        // Mark for detection (optional)
        snowball.setMetadata("ki_blast", new FixedMetadataValue(JavaPlugin.getPlugin(ElementalMagicTesting.class), true));

        // Disable gravity for extra floaty look
        Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(ElementalMagicTesting.class), () -> {
            snowball.setGravity(false);
        }, 1L);

        // Collision detection
        new BukkitRunnable() {
            @Override
            public void run() {
                if (snowball.isDead() || !snowball.isValid()) {
                    this.cancel();
                    return;
                }

                for (Entity entity : snowball.getNearbyEntities(1.5, 1.5, 1.5)) {
                    if (entity instanceof LivingEntity target && !entity.equals(player)) {
                        if (!target.isDead()) {
                            // Damage + impact
                            target.damage(6.0, player);
                            Location impact = target.getLocation();

                            world.spawnParticle(Particle.EXPLOSION_LARGE, impact, 1, 0, 0, 0, 0);
                            world.spawnParticle(Particle.CLOUD, impact, 20, 0.5, 0.5, 0.5, 0.1);
                            world.spawnParticle(Particle.SPELL_WITCH, impact, 20, 0.4, 0.7, 0.4, 0.05);
                            world.playSound(impact, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.2f, 2.0f);

                            snowball.remove();
                            this.cancel();
                            return;
                        }
                    }
                }
            }
        }.runTaskTimer(JavaPlugin.getPlugin(ElementalMagicTesting.class), 2L, 1L);
    }*/





}
