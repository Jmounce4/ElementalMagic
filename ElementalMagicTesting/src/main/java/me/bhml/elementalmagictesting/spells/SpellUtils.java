package me.bhml.elementalmagictesting.spells;
import com.destroystokyo.paper.entity.Pathfinder;
import me.bhml.elementalmagictesting.items.SpellbookGUI;
import me.bhml.elementalmagictesting.player.TargetingUtils;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.metadata.FixedMetadataValue;
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
import java.util.*;

public class SpellUtils {


    public static void applySpellDamage(Player caster, LivingEntity target, double damage) {
        PlayerSpellTracker.markCasting(caster);
        target.setNoDamageTicks(0);  // ensures spell hits reliably
        target.damage(damage);
        target.setNoDamageTicks(0);  // ensures next spell hits reliably

        MetadataUtils.set(target, "lastSpellDamager", caster.getUniqueId().toString());
        if (target instanceof Zombie ||
                target instanceof Skeleton ||
                target instanceof Spider ||
                target instanceof CaveSpider ||
                target instanceof Endermite ||
                target instanceof Blaze ||
                target instanceof Witch ||
                target instanceof Vindicator ||
                target instanceof Evoker ||
                target instanceof Pillager ||
                target instanceof Ravager ||
                target instanceof Illusioner ||
                target instanceof Warden ||
                target instanceof Drowned ||
                target instanceof Husk ||
                target instanceof Stray ||
                target instanceof Piglin ||
                target instanceof PiglinBrute ||
                target instanceof Zoglin ||
                target instanceof WitherSkeleton ||
                target instanceof Guardian ||
                target instanceof ElderGuardian ||
                target instanceof IronGolem) { // They can ram if provoked
            double maxAggroRange = 16;
            if (target instanceof Mob mob) {
                if(mob.getLocation().distanceSquared(caster.getLocation()) <= (maxAggroRange * maxAggroRange)){
                    mob.setTarget(caster);
                }
            }
        }

        //String metaKey = "spell_damage_" + caster.getName();
        //MetadataUtils.set(target, metaKey, true);
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


    // Keeps track of which targets each caster has already been notified about
    private static final Map<UUID, Set<UUID>> blockedTargetsMap = new HashMap<>();

    /**
     * Checks targeting rules and, if blocked, sends a single notification per target per cast.
     * Returns true if the target may be hit.
     */
    public static boolean handleBlockedTargetFeedback(Player caster, LivingEntity target) {
        // First, pure logic check
        boolean canHit = TargetingUtils.canHit(caster, target);
        Bukkit.getLogger().info("handleBlockedTargetFeedback: caster=" + caster.getName() + ", target=" + target.getType() + ", canHit=" + canHit);

        if (canHit) {
            return true;
        }

        // If blocked, notify only once per cast
        UUID casterId = caster.getUniqueId();
        blockedTargetsMap.putIfAbsent(casterId, new HashSet<>());
        Set<UUID> blocked = blockedTargetsMap.get(casterId);

        UUID targetId = target.getUniqueId();
        if (!blocked.contains(targetId)) {
            blocked.add(targetId);
            if (target != caster)
                caster.sendMessage("§cBlocked hit on: §7" + target.getType().name());
        }

        return false;
    }

    /**
     * Clears the blocked-targets memory for this caster.
     * Call this once at the end of each spell cast.
     */
    public static void clearBlockedTargets(Player caster) {
        blockedTargetsMap.remove(caster.getUniqueId());
    }




    //Methods to enable/disable magic. Used to account for GUI casting and other issues.
    private static final Set<UUID> magicDisabled = new HashSet<>();

    public static void disableMagic(Player player) {
        Bukkit.getLogger().info("Disabling magic for: " + player.getName());
        magicDisabled.add(player.getUniqueId());
    }

    public static void enableMagic(Player player) {
        Bukkit.getLogger().info("Enabling magic for: " + player.getName());
        magicDisabled.remove(player.getUniqueId());
    }



    public static boolean isMagicDisabled(Player player) {
        //Bukkit.getLogger().info("magic disabled for: " + player.getName());

        if (player.getOpenInventory().getType() == InventoryType.PLAYER){
            Bukkit.getLogger().info("Inventory open for: " + player.getName());
            return true;
        }

        return magicDisabled.contains(player.getUniqueId());
    }


    public static Spell getStarterSpellForElement(SpellElement element) {
        return switch (element) {
            case AIR -> SpellRegistry.get("gust"); // Adjust these IDs to match your actual spell names
            case FIRE -> SpellRegistry.get("fireball");
            case WATER -> SpellRegistry.get("liquidlance");
            case EARTH -> SpellRegistry.get("rumble");
            case LIGHTNING -> SpellRegistry.get("zap");
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
    /*
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
                String current = PlayerSpellTracker.getSelectedSpell(player).getName();
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
    /*
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
    }*/
}



