package me.bhml.elementalmagictesting.spells.earth;

import me.bhml.elementalmagictesting.spells.Spell;
import me.bhml.elementalmagictesting.spells.SpellElement;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Set;


public class TerraPath implements Spell {

    @Override
    public String getName() {
        return "Terra Path";
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
        return "terrapath";
    }

    private static final int MAX_BLOCKS = 5;
    private static final int PILLAR_HEIGHT = 5;
    private static final Set<Material> REPLACEABLE = Set.of(
            Material.AIR, Material.WATER, Material.LAVA, Material.SHORT_GRASS, Material.TALL_GRASS,
            Material.SEAGRASS, Material.FIRE, Material.SNOW, Material.CAVE_AIR
    );
    private static final Set<Material> EARTH_MATERIALS = Set.of(
            Material.DIRT, Material.GRASS_BLOCK, Material.COARSE_DIRT, Material.PODZOL,
            Material.SAND, Material.RED_SAND, Material.GRAVEL,
            Material.STONE, Material.COBBLESTONE, Material.ANDESITE, Material.DIORITE, Material.GRANITE,
            Material.OAK_PLANKS, Material.SPRUCE_PLANKS, Material.BIRCH_PLANKS, Material.JUNGLE_PLANKS,
            Material.ACACIA_PLANKS, Material.DARK_OAK_PLANKS, Material.MANGROVE_PLANKS
    );
    private static final Material DEFAULT_MATERIAL = Material.DIRT;

    @Override
    public void cast(Player player) {
        Vector dir = getCardinalDirection(player);
        float pitch = player.getLocation().getPitch(); // -90 (up) to 90 (down)

        Location playerLoc = player.getLocation();
        World world = player.getWorld();

        // Get the block beneath the player (even if jumping)
        Block beneath = player.getLocation().subtract(0, 1, 0).getBlock();
        if (!beneath.getType().isSolid()) {
            if (Math.abs(pitch) <= 80) {
                beneath = getNearbySolidBlock(playerLoc, world, player);
                if (beneath == null) {
                    player.sendMessage(ChatColor.RED + "You must be standing on solid ground.");
                    return;
                }

            } else {
                beneath = player.getLocation().subtract(0, 1.3, 0).getBlock();
                if (!beneath.getType().isSolid()) {
                    player.sendMessage(ChatColor.RED + "You must be standing on solid ground.");
                    return;
                }
            }


        }

        if (Math.abs(pitch) > 80) {
            buildPillar(player);
        } else if (pitch < -35) {
            buildStairs(player, dir, beneath.getLocation(), true);  // Upward stairs
        } else if (pitch > 35) {
            buildStairs(player, dir, beneath.getLocation(), false); // Downward stairs
        } else {
            buildBridge(player, dir, beneath.getLocation());
        }
    }

    private void buildBridge(Player player, Vector direction, Location origin) {
        World world = player.getWorld();
        Material material = findMaterialFromHotbar(player);
        int placed = 0;
        int i = 1;

        while (placed < MAX_BLOCKS && i <= MAX_BLOCKS + 5) {
            Location placeLoc = origin.clone().add(direction.clone().multiply(i)).getBlock().getLocation();
            if (canReplace(placeLoc.getBlock())) {
                placeLoc.getBlock().setType(material);
                consumeMaterialIfNeeded(player, material);
                placed++;
            }
            i++;
        }


        world.playSound(player.getLocation(), Sound.BLOCK_STONE_PLACE, 1f, 1f);
    }

    private void buildStairs(Player player, Vector direction, Location origin, boolean upward) {
        World world = player.getWorld();
        Material material = findMaterialFromHotbar(player);
        int placed = 0;

        for (int i = 1; i <= MAX_BLOCKS; i++) {
            int yOffset = upward ? i : -i;
            Location step = origin.clone()
                    .add(direction.clone().multiply(i))
                    .add(0, yOffset, 0)
                    .getBlock().getLocation();

            if (canReplace(step.getBlock())) {
                step.getBlock().setType(material);
                consumeMaterialIfNeeded(player, material);
                placed++;
            }
        }

        world.playSound(player.getLocation(), Sound.BLOCK_STONE_PLACE, 1f, 1f);
    }

    private void buildPillar(Player player) {
        World world = player.getWorld();
        Location base = player.getLocation().subtract(0, 1, 0).getBlock().getLocation();
        Material material = findMaterialFromHotbar(player);

        for (int i = 1; i <= PILLAR_HEIGHT + 2; i++) {
            Location check = base.clone().add(0, i, 0);
            if (!canReplace(check.getBlock()) && i <= PILLAR_HEIGHT) {
                player.sendMessage(ChatColor.RED + "Not enough vertical space to build pillar.");
                return;
            }
        }

        for (int i = 0; i < PILLAR_HEIGHT; i++) {
            Block b = base.clone().add(0, i, 0).getBlock();
            if (canReplace(b)) {
                b.setType(material);
                consumeMaterialIfNeeded(player, material);
            }
        }

        // Save player's look direction
        Location playerLoc = player.getLocation();
        float yaw = playerLoc.getYaw();
        float pitch = playerLoc.getPitch();

        // Teleport player to top of pillar
        Location tp = base.clone().add(0.5, PILLAR_HEIGHT, 0.5);
        tp.setYaw(yaw);
        tp.setPitch(pitch);
        player.teleport(tp);

        player.setVelocity(new Vector(0, 0.2, 0));
        world.playSound(player.getLocation(), Sound.BLOCK_STONE_PLACE, 1f, 1f);
    }

    private Vector getCardinalDirection(Player player) {
        float yaw = player.getLocation().getYaw();
        yaw = (yaw % 360 + 360) % 360;

        if (yaw >= 45 && yaw < 135) return new Vector(-1, 0, 0); // West
        if (yaw >= 135 && yaw < 225) return new Vector(0, 0, -1); // North
        if (yaw >= 225 && yaw < 315) return new Vector(1, 0, 0);  // East
        return new Vector(0, 0, 1);                               // South
    }

    private Material findMaterialFromHotbar(Player player) {
        for (int i = 0; i < 9; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && EARTH_MATERIALS.contains(item.getType()) && item.getAmount() > 0) {
                return item.getType();
            }
        }
        return DEFAULT_MATERIAL;
    }

    private void consumeMaterialIfNeeded(Player player, Material material) {
        if (material == DEFAULT_MATERIAL) return;
        for (int i = 0; i < 9; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && item.getType() == material) {
                item.setAmount(item.getAmount() - 1);
                if (item.getAmount() <= 0) player.getInventory().setItem(i, null);
                break;
            }
        }
    }

    private boolean canReplace(Block block) {
        return REPLACEABLE.contains(block.getType());
    }

    private Block getNearbySolidBlock(Location loc, World world, Player player) {
        double baseY = loc.getY() - 1.0;
        double baseX = loc.getX();
        double baseZ = loc.getZ();


        //for jumping
        Block block = loc.subtract(0, 1.3, 0).getBlock();
        if (block.getType().isSolid()) {
            player.sendMessage("t");
            return block;
        }


        Block closestBlock = null;
        double closestDistanceSquared = Double.MAX_VALUE;

        for (double x = baseX - 1.3; x <= baseX + 1.3; x += 0.2) {
            for (double z = baseZ - 1.3; z <= baseZ + 1.3; z += 0.2) {
                block = world.getBlockAt((int) x, (int) baseY, (int) z);
                if (block.getType().isSolid()) {
                    double dx = loc.getX() - (block.getX() + 0.5);
                    double dz = loc.getZ() - (block.getZ() + 0.5);
                    double distanceSquared = dx * dx + dz * dz;
                    if (distanceSquared < closestDistanceSquared) {
                        closestDistanceSquared = distanceSquared;
                        closestBlock = block;
                    }
                }
            }
        }
        if (closestBlock != null) {
            return closestBlock;
        }









        return null; // no solid block found nearby
    }

}
