package me.bhml.elementalmagictesting.listeners;
import me.bhml.elementalmagictesting.ElementalMagicTesting;
import me.bhml.elementalmagictesting.items.ItemManager;
import me.bhml.elementalmagictesting.items.SpellbookGUI;
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
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

import static me.bhml.elementalmagictesting.spells.PlayerSpellTracker.getRemainingCooldown;
import static me.bhml.elementalmagictesting.spells.PlayerSpellTracker.isOnCooldown;


public class ElementalCoreListener implements Listener{



    private final Set<UUID> skipNextCast = new HashSet<>();

    /*
    // 1a) Flag a skip when they click *inside* any inventory GUI
    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        Player p = (Player)e.getWhoClicked();
        if (ItemManager.isElementalCore(p.getInventory().getItemInMainHand())) {
            skipNextCast.add(p.getUniqueId());
        }
    }

    // 1b) Flag a skip when they drop an item while holding the Core
    @EventHandler(ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player p = event.getPlayer();
        if (ItemManager.isElementalCore(p.getInventory().getItemInMainHand())) {
            skipNextCast.add(p.getUniqueId());
        }
    }

    // 1c) Clear any pending skips when they close *any* inventory
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        skipNextCast.remove(e.getPlayer().getUniqueId());
    }
    */


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        UUID id = player.getUniqueId();

        // 1d) If they’re flagged to skip *one* cast, consume that and return immediately
        if (skipNextCast.remove(id)) {
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!ItemManager.isElementalCore(item)) return;

        Action action = event.getAction();

        // ─── RIGHT‐CLICK selects spell ─────────────────────────────────────────
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            PlayerSpellTracker.cycleNextSpell(player);
            Spell current = PlayerSpellTracker.getCurrentSpell(player);
            if (current != null) {
                ChatColor color = current.getElement().getColor();
                player.sendMessage(
                        ChatColor.WHITE + "Selected Spell: " +
                                color + current.getName()
                );
            }
            return;
        }

        // ─── LEFT‐CLICK casts spell ────────────────────────────────────────────
        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true);
            Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(ElementalMagicTesting.class), () -> {
                castSelectedSpell(player);
            }, 1L);
            //castSelectedSpell(player);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockDamage(BlockDamageEvent event) {
        Player p = event.getPlayer();
        if (ItemManager.isElementalCore(p.getInventory().getItemInMainHand())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;
        if (!ItemManager.isElementalCore(player.getInventory().getItemInMainHand())) return;
        if (PlayerSpellTracker.isCasting(player)) return; // allow your own spell hits

        event.setCancelled(true);

        castSelectedSpell(player);

    }

    /*@EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        Player player = (Player) e.getPlayer();
        if (ItemManager.isElementalCore(player.getInventory().getItemInMainHand())) {
            e.setCancelled(true);
            SpellbookGUI.open(player); // Replace this with your GUI logic
        }
    }*/



    //Experimental: Making the core the spellbook as well

    private final Set<UUID> inSpellbookGUI = new HashSet<>();

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        if (e.getClick() == ClickType.DROP || e.getClick() == ClickType.CONTROL_DROP) {
            // Player pressed Q on an item or CTRL+Q or dragged out item to drop
            SpellUtils.disableMagic(player);
            Bukkit.getLogger().info("Player dropping item in inventory, disabling magic.");
        }
        SpellUtils.disableMagic(player);

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || !ItemManager.isElementalCore(clicked)) return;

        if (e.getClick() == ClickType.RIGHT) {
            e.setCancelled(true);
            Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(ElementalMagicTesting.class), () -> {
                player.closeInventory(); // prevent click interaction recursion
                SpellbookGUI.open(player);
            }, 1L);
        }





    }


    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        Player player = (Player) e.getPlayer();
        InventoryType type = e.getInventory().getType();

        //Bukkit.getLogger().info("Inventory opened by " + player.getName());
        //if (!ItemManager.isElementalCore(player.getInventory().getItemInMainHand())) return;


        SpellUtils.disableMagic(player);
    }





        @EventHandler
        public void onInventoryClose(InventoryCloseEvent e)
        {
            Player player = (Player) e.getPlayer();
            SpellUtils.enableMagic(player);

            inSpellbookGUI.remove(e.getPlayer().getUniqueId());

        }


    private void castSelectedSpell(Player player) {
        Spell current = PlayerSpellTracker.getCurrentSpell(player);
        if (current == null) {
            player.sendMessage(ChatColor.RED + "No spell selected!");
            return;
        }


        if (SpellUtils.isMagicDisabled(player)) {
            player.sendMessage(ChatColor.RED + "You cannot cast spells right now.");
            return;
        }

        player.sendMessage(ChatColor.RED + "Test");

        String spellName = current.getName();
        if (PlayerSpellTracker.isOnCooldown(player, spellName)) {
            return;
        }

        current.cast(player);
        PlayerSpellTracker.setCooldown(player, spellName, current.getCooldown());
        Bukkit.getLogger().info("Casting spell: " + spellName + " for player: " + player.getName());
    }





    }



