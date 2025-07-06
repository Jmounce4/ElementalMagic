package me.bhml.elementalmagictesting.listeners;
import me.bhml.elementalmagictesting.ElementalMagicTesting;
import me.bhml.elementalmagictesting.items.ItemManager;
import me.bhml.elementalmagictesting.spells.PlayerSpellTracker;
import me.bhml.elementalmagictesting.spells.Spell;

import me.bhml.elementalmagictesting.spells.SpellUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import static me.bhml.elementalmagictesting.spells.PlayerSpellTracker.getRemainingCooldown;
import static me.bhml.elementalmagictesting.spells.PlayerSpellTracker.isOnCooldown;


public class ElementalCoreListener implements Listener{

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        Action action = event.getAction();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!ItemManager.isElementalCore(item)) return;

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            PlayerSpellTracker.cycleNextSpell(player);
            Spell current = PlayerSpellTracker.getCurrentSpell(player);

            if (current != null) {
                ChatColor color = current.getElement().getColor();
                player.sendMessage(ChatColor.WHITE + "Selected Spell: " + color + current.getName());
            }
            event.setCancelled(true);
            return;
        }

        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            //castSelectedSpell(player);
            event.setCancelled(true);
        }
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockDamage(BlockDamageEvent event) {
        Player player = event.getPlayer();
        if (ItemManager.isElementalCore(player.getInventory().getItemInMainHand())) {
            event.setCancelled(true);
        }
    }




    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;   // only block punches


        // Only care about “attacks” with the Core
        ItemStack inHand = player.getInventory().getItemInMainHand();
        if (!ItemManager.isElementalCore(inHand)) return;

        // If this is your own spell damage (cast in same tick), let it through
        if (PlayerSpellTracker.isCasting(player)) return;

        // Otherwise it's a normal melee swing—cancel it
        event.setCancelled(true);
    }




        @EventHandler
        public void onPlayerSwing(PlayerAnimationEvent event) {
            // Only react to the ARM_SWING animation (left‑click)
            if (event.getAnimationType() != PlayerAnimationType.ARM_SWING) return;

            Player player = event.getPlayer();
            ItemStack item = player.getInventory().getItemInMainHand();
            if (!ItemManager.isElementalCore(item)) return;

            // Optional: ray‑trace to only cast when aiming at something
            // Entity target = player.getTargetEntity(5);
            // if (target == null) return;

            event.setCancelled(true);       // stop default melee damage
            castSelectedSpell(player);      // unified cast entrypoint
        }


    private void castSelectedSpell(Player player) {
        Spell current = PlayerSpellTracker.getCurrentSpell(player);
        if (current != null) {
            String spellName = current.getName();
            if (PlayerSpellTracker.isOnCooldown(player, spellName)) {
                long remaining = PlayerSpellTracker.getRemainingCooldown(player, spellName);
                //SpellUtils.startCooldownBar(player, remaining, spellName); // only update existing bar
                return;
            }


            current.cast(player);
            long cd = current.getCooldown();
            PlayerSpellTracker.setCooldown(player, spellName, cd);
            //SpellUtils.startCooldownBar(player, cd, spellName);


            Bukkit.getLogger().info("Casting spell: " + spellName + " for player: " + player.getName());
        } else {
            player.sendMessage(ChatColor.RED + "No spell selected!");
        }



    }


}
