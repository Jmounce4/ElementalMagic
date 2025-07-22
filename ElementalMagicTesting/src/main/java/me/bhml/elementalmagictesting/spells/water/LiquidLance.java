package me.bhml.elementalmagictesting.spells.water;

import me.bhml.elementalmagictesting.ElementalMagicTesting;
import me.bhml.elementalmagictesting.listeners.MobSpawningListener;
import me.bhml.elementalmagictesting.player.PlayerDataManager;
import me.bhml.elementalmagictesting.player.TargetingUtils;
import me.bhml.elementalmagictesting.skills.SkillType;
import me.bhml.elementalmagictesting.spells.PlayerSpellTracker;
import me.bhml.elementalmagictesting.spells.Spell;
import me.bhml.elementalmagictesting.spells.SpellElement;
import me.bhml.elementalmagictesting.spells.SpellUtils;
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

public class LiquidLance implements Spell {


    @Override
    public String getName() {
        return "Liquid Lance";
    }

    @Override
    public SpellElement getElement() {
        return SpellElement.WATER;
    }

    @Override
    public long getCooldown() {
        return 500; // 0.5 second (100 is 0.1 for testing)
    }

    @Override
    public String getId() {
        return "liquidlance";
    }

    @Override
    public int calculateXpGain(Player player, List<Entity> hitEntities) {
        int xp = 400; // Default 0, increased for testing levelups
        int base = 4; // first few enemies
        int decay = 1; // -1 xp per additional

        for (int i = 0; i < hitEntities.size(); i++) {
            int bonus = Math.max(base - i * decay, 1);
            Entity target = hitEntities.get(i);

            //Bonus XP on kill
            if (target.isDead()) {
                bonus += 1;
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

        // --- CONFIGURABLE STATS ---
        int range = 25; // how far the lance travels
        double damage = 4.0; // base damage
        double hitRadius = 1.2; // how close an entity must be to be hit

        World world = player.getWorld();

        // Track already-hit entities so we don't hit the same one multiple times
        Set<UUID> hitEntities = new HashSet<>();

        // --- TRAVEL EFFECT ---
        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (tick > range) {
                    endSpell();
                    cancel();
                    return;
                }

                Location point = origin.clone().add(direction.clone().multiply(tick));

                //BLOCK HIT DETECTION
                if (!world.getBlockAt(point).isPassable()) {
                    endSpell(); // ✅ Spell hit a block early
                    cancel();
                    return;
                }

                // Visual effects
                world.spawnParticle(Particle.WATER_SPLASH, point, 10, 0.2, 0.2, 0.2, 0.01);
                world.spawnParticle(Particle.WATER_SPLASH, point, 10, 0.1, 0.1, 0.1, 0.01);
                Particle.DustOptions waterColor = new Particle.DustOptions(Color.fromRGB(0, 100, 255), 1.0f);
                world.spawnParticle(Particle.REDSTONE, point, 2, 0.2, 0.2, 0.2, 0.01, waterColor);
                world.playSound(point, Sound.ITEM_BUCKET_EMPTY, 0.2f, 2f);

                for (Entity entity : world.getNearbyEntities(point, hitRadius, hitRadius, hitRadius)) {
                    if (!(entity instanceof LivingEntity target)) continue;
                    if (target.equals(player)) continue;
                    if (hitEntities.contains(target.getUniqueId())) continue;
                    if (!handleBlockedTargetFeedback(player, target)) continue;
                    if (!hasClearShot(player, target)) continue;

                    applySpellDamage(player, target, damage);

                    hitEntities.add(target.getUniqueId());

                    // Hit effects
                    world.spawnParticle(Particle.WATER_SPLASH, target.getLocation().add(0, 1, 0), 10, 0.4, 0.5, 0.4, 0.05);
                    world.playSound(target.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.8f, 1.4f);
                }

                tick++;
            }

            // ✅ This runs once when the spell ends or hits a wall
            private void endSpell() {
                List<Entity> hitList = hitEntities.stream()
                        .map(Bukkit::getEntity)
                        .filter(Objects::nonNull)
                        .filter(e -> e instanceof LivingEntity)
                        .toList();

                int xp = calculateXpGain(player, hitList);
                PlayerDataManager.get(player).addXp(SkillType.WATER, xp);
                PlayerDataManager.saveData(player.getUniqueId());

                Bukkit.getLogger().info(xp + " xp for water");

                clearBlockedTargets(player);
            }
        }.runTaskTimer(JavaPlugin.getPlugin(ElementalMagicTesting.class), 0L, 1L);
    }




}