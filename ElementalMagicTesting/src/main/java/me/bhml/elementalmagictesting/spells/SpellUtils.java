package me.bhml.elementalmagictesting.spells;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.bhml.elementalmagictesting.ElementalMagicTesting;
import me.bhml.elementalmagictesting.spells.PlayerSpellTracker;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.bukkit.FluidCollisionMode;



import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpellUtils {


    public static void applySpellDamage(Player caster, LivingEntity target, double damage) {
        PlayerSpellTracker.markCasting(caster);
        target.setNoDamageTicks(0);  // ensures spell hits reliably
        target.damage(damage, caster);
        PlayerSpellTracker.unmarkCasting(caster);
    }





    public static boolean hasClearShot(Player player, LivingEntity target) {
        World world = player.getWorld();
        Location from = player.getEyeLocation();

        BoundingBox box = target.getBoundingBox();
        double inset = 0.05;

        double minX = box.getMinX() + inset;
        double maxX = box.getMaxX() - inset;
        double minY = box.getMinY() + inset;
        double maxY = box.getMaxY() - inset;
        double minZ = box.getMinZ() + inset;
        double maxZ = box.getMaxZ() - inset;

        int samplesX = 3;
        int samplesY = 5;
        int samplesZ = 3;

        for (int x = 0; x < samplesX; x++) {
            double sampleX = minX + (x / (double)(samplesX - 1)) * (maxX - minX);
            for (int y = 0; y < samplesY; y++) {
                double sampleY = minY + (y / (double)(samplesY - 1)) * (maxY - minY);
                for (int z = 0; z < samplesZ; z++) {
                    double sampleZ = minZ + (z / (double)(samplesZ - 1)) * (maxZ - minZ);

                    Location to = new Location(world, sampleX, sampleY, sampleZ);

                    RayTraceResult result = world.rayTrace(
                            from,
                            to.toVector().subtract(from.toVector()),
                            from.distance(to),
                            FluidCollisionMode.NEVER,
                            true, // only collidable blocks (ignores grass, flowers, etc)
                            0.1,
                            (e) -> false // no entity checks
                    );

                    if (result == null) {
                        return true; // No block hit = clear shot
                    } else {
                        Block hitBlock = result.getHitBlock();
                        if (hitBlock == null || hitBlock.isPassable()) {
                            continue; // Still acceptable
                        }
                    }
                }
            }
        }

        return false; // All points blocked
    }


    //Hit Detection for Explosive AoE
    public static boolean isExposedTo(Location origin, LivingEntity target) {
        World world = origin.getWorld();
        if (world == null) return false;

        Location targetLoc = target.getEyeLocation();
        Vector direction = targetLoc.toVector().subtract(origin.toVector());
        double distance = direction.length();

        RayTraceResult result = world.rayTraceBlocks(
                origin,
                direction.normalize(),
                distance,
                FluidCollisionMode.NEVER,
                true // ignore passable blocks like grass, flowers, air
        );

        return result == null; // if null, nothing blocked the view = exposed
    }

    private static boolean isActuallyPassable(Block block) {
        if (block == null) return true;

        if (block.isPassable()) return true;

        Material type = block.getType();
        String name = type.name();

        // Check if block is any type of sign or wall sign
        if (name.endsWith("_SIGN") || name.endsWith("_WALL_SIGN")) {
            return true;
        }

        return switch (type) {
            case TALL_GRASS, SHORT_GRASS, FLOWER_POT, SUNFLOWER, LILY_PAD, VINE,
                    DEAD_BUSH, FERN, SWEET_BERRY_BUSH, CARROTS, WHEAT, POTATO, BEETROOTS, LARGE_FERN -> true;
            default -> false;
        };
    }






    //Sends action bar message to the player indicating cooldown time.

    public static void showCooldownTime(Player player, long remainingMillis) {
        double seconds = Math.round(remainingMillis / 100.0) / 10.0; // 1 decimal place
        player.sendActionBar(ChatColor.GRAY + "Cooldown: " + ChatColor.RED + seconds + "s");
    }

    // === Maps to track active cooldown bars ===
    private static final Map<UUID, BukkitTask> barTasks = new HashMap<>();
    private static final Map<UUID, Long> barEndTimes = new HashMap<>();
    private static final Map<UUID, Long> barDurations = new HashMap<>();
    private static final Map<UUID, String> barSpellNames = new HashMap<>();

    /**
     * Starts or restarts the cooldown bar for a specific spell.
     * Cancels any existing bar and schedules updates.
     */
    public static void startCooldownBar(Player player, long totalDurationMs, String spellName) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        long end = now + totalDurationMs;

        // Store metadata for this bar
        barEndTimes.put(uuid, end);
        barDurations.put(uuid, totalDurationMs);
        barSpellNames.put(uuid, spellName);

        // Cancel old task if present
        if (barTasks.containsKey(uuid)) {
            barTasks.get(uuid).cancel();
        }

        // Schedule new bar updates every tick
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                // If player switched spells, clear existing bar
                String current = PlayerSpellTracker.getCurrentSpell(player).getName();
                if (!spellName.equals(current)) {
                    clearCooldownBar(player);
                    return;
                }

                long remaining = barEndTimes.get(uuid) - System.currentTimeMillis();
                if (remaining <= 1) {
                    clearCooldownBar(player);
                    return;
                }

                double fraction = (double) remaining / barDurations.get(uuid);
                int bars = (int) Math.floor(fraction * 10);
                if (bars < 1) {
                    clearCooldownBar(player);
                    return;
                }

                StringBuilder sb = new StringBuilder(ChatColor.RED.toString());
                for (int i = 0; i < bars; i++) sb.append("|");
                player.sendActionBar(sb.toString());
            }
        }.runTaskTimer(JavaPlugin.getPlugin(ElementalMagicTesting.class), 0L, 1L);

        barTasks.put(uuid, task);
    }

    /**
     * Clears any cooldown bar currently shown to the player.
     */
    public static void clearCooldownBar(Player player) {
        UUID uuid = player.getUniqueId();
        if (barTasks.containsKey(uuid)) {
            barTasks.get(uuid).cancel();
            barTasks.remove(uuid);
        }
        barEndTimes.remove(uuid);
        barDurations.remove(uuid);
        barSpellNames.remove(uuid);

        player.sendActionBar("");
    }
}


