package me.bhml.elementalmagictesting.listeners;
import com.destroystokyo.paper.event.player.PlayerAttackEntityCooldownResetEvent;
import io.papermc.paper.event.player.PlayerArmSwingEvent;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import me.bhml.elementalmagictesting.ElementalMagicTesting;
import me.bhml.elementalmagictesting.gui.ElementUnlockConfirmGUI;
import me.bhml.elementalmagictesting.gui.SpellSelectionGUI;
import me.bhml.elementalmagictesting.items.ItemManager;
import me.bhml.elementalmagictesting.items.SpellbookGUI;
import me.bhml.elementalmagictesting.player.PlayerData;
import me.bhml.elementalmagictesting.player.PlayerDataManager;
import me.bhml.elementalmagictesting.spells.*;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
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

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

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

        /* 1d) If they’re flagged to skip *one* cast, consume that and return immediately
        if (skipNextCast.remove(id)) {
            return;
        }*/

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

    /* Attempt to redo casting, issues with doors/villagers/etc.
    @EventHandler
    public void onLeftClickSwing(PlayerArmSwingEvent event){
        Player player = event.getPlayer();
        UUID id = player.getUniqueId();
        if (event.getHand() != EquipmentSlot.HAND) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!ItemManager.isElementalCore(item)) return;

        event.setCancelled(true);
        event.getPlayer().get



            Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(ElementalMagicTesting.class), () -> {
                castSelectedSpell(player);
            }, 1L);

    }
    */


    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockDamage(BlockDamageEvent event) {
        Player p = event.getPlayer();
        if (ItemManager.isElementalCore(p.getInventory().getItemInMainHand())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPrePlayerAttack(PrePlayerAttackEntityEvent event){
        Player player = event.getPlayer();
        if (!(event.getAttacked() instanceof LivingEntity target)) return;

        // only when holding your core
        if (!ItemManager.isElementalCore(player.getInventory().getItemInMainHand())) return;

        // prevent Minecraft’s normal melee damage
        event.setCancelled(true);

        // now trigger your spell cast instead
        castSelectedSpell(player);



    }



    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;
        if (!ItemManager.isElementalCore(player.getInventory().getItemInMainHand())) return;
        //if (PlayerSpellTracker.isCasting(player)) return; // allow your own spell hits (this is to stop infinite damage)


        /*LivingEntity target = (event.getEntity() instanceof LivingEntity le) ? le : null;
        target.setNoDamageTicks(0);

         */



/*
        if (target == null) return;


        String metaKey = "spell_damage_" + player.getName();
        if (target.hasMetadata(metaKey)){
            event.setCancelled(true);
            return;
        }

        // ✅ PREVENT double casting immediately
        MetadataUtils.set(target, metaKey, true);
        Bukkit.getLogger().info("this is the metakey: " + metaKey);
        Bukkit.getScheduler().runTaskLater(ElementalMagicTesting.getInstance(), () -> {
            MetadataUtils.remove(target, metaKey);
            Bukkit.getLogger().info("Metadata removed");
        }, 2L);
*/


        //Next Solution attempt: try using setHealth for spell damage.
        //castSelectedSpell(player);
        //event.setCancelled(true);

    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        //target.setNoDamageTicks(0);
        //Bukkit.getLogger().info("damage tick = 0");

        if (event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK){
            //LivingEntity target = (event.getEntity() instanceof LivingEntity le) ? le : null;

        }


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

        // Handle selection GUI first
        if (e.getView().getTitle().equals(ChatColor.BLUE + "Select a Spell")) {
            SpellSelectionGUI.handleClick(e);
            return;
        }

        // Disabling magic on drop
        if (e.getClick() == ClickType.DROP || e.getClick() == ClickType.CONTROL_DROP) {
            SpellUtils.disableMagic(player);
            Bukkit.getLogger().info("Player dropping item in inventory, disabling magic.");
            return;
        }

        PlayerData data = PlayerDataManager.get(player);
        String title = e.getView().getTitle();
        ItemStack clicked = e.getCurrentItem();
        //e.setCancelled(true);

        // Handle clicks inside Spellbook GUI =-=-=-=-=
        if (e.getView().getTitle().equals(ChatColor.DARK_PURPLE + "Spellbook")) {
            e.setCancelled(true); // Prevent item movement


            //Loadout!
            int slot = e.getRawSlot();
            if (slot >= 38 && slot <= 42) {
                int loadoutIndex = slot - 38;

                // RIGHT‑click: clear **just** this one slot
                if (e.getClick() == ClickType.RIGHT) {
                    List<String> loadout = new ArrayList<>(data.getLoadoutSpells());

                    // make sure the list is big enough
                    while (loadout.size() <= loadoutIndex) {
                        loadout.add(null);
                    }
                    // clear only that one slot
                    loadout.set(loadoutIndex, null);

                    // **important**: filter out nulls only when saving,
                    // so your internal list can keep positional integrity
                    data.setLoadoutSpells(
                            loadout.stream()
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toList())
                    );
                    //Persist data
                    PlayerDataManager.saveData(player.getUniqueId());
                    PlayerSpellTracker.get(player).refreshAvailableSpells();

                    player.sendMessage(ChatColor.RED + "Cleared slot " + (loadoutIndex + 1));
                    SpellbookGUI.open(player); // refresh
                }
                // LEFT‑click: open selection menu
                else {
                    SpellSelectionGUI.open(player, loadoutIndex);
                }
            }

            String elementName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            String[] splitElement = elementName.split(" ");
            SpellElement clickedElement = tryParseElement(splitElement[0]);

            if (clickedElement != null && !data.getUnlockedElements().contains(clickedElement)) {
                if (data.getPendingElementUnlocks() > 0) {
                    // Open confirmation GUI
                    ElementUnlockConfirmGUI.open(player, data, clickedElement);
                } else {
                    player.sendMessage(ChatColor.RED + "You have no element unlocks available.");
                }
            }






            return;
        }

        // === Confirmation GUI ===
        if (title.startsWith(ChatColor.DARK_GREEN + "Unlock ")) {
            SpellElement pending = data.getTempElementToUnlock();
            if (pending == null) {
                player.sendMessage(ChatColor.RED + "Error: no element selected.");
                player.closeInventory();
                return;
            }

            Material type = clicked.getType();

            if (type == Material.LIME_WOOL) {
                // Confirm unlock
                data.getUnlockedElements().add(pending);
                data.setPendingElementUnlocks(data.getPendingElementUnlocks() - 1);

                // Grant starter spell
                Spell starter = SpellUtils.getStarterSpellForElement(pending);
                if (starter != null) {
                    data.unlockSpell(starter.getId());
                    player.sendMessage(ChatColor.GREEN + "Unlocked " + pending.name() + " and learned " + starter.getName() + "!");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
                } else{
                    player.sendMessage(ChatColor.RED + "Unlocked " + pending.name() + ", but no starter spell was found!");


                }

                // Cleanup
                data.setTempElementToUnlock(null);
                PlayerDataManager.saveData(player.getUniqueId());

                player.closeInventory();

            } else if (type == Material.RED_WOOL) {
                // Cancel
                data.setTempElementToUnlock(null);
                player.closeInventory();
            }
            return;
        }



        // Handle right-click on Elemental Core

        if (clicked == null || !ItemManager.isElementalCore(clicked)) return;

        if (e.getClick() == ClickType.RIGHT) {
            e.setCancelled(true);
            Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(ElementalMagicTesting.class), () -> {
                player.closeInventory();
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

        //player.sendMessage(ChatColor.RED + "Test");

        String spellName = current.getName();
        if (PlayerSpellTracker.isOnCooldown(player, spellName)) {
            player.sendMessage(ChatColor.RED + "Cooldown.");
            return;
        }

        current.cast(player);
        PlayerSpellTracker.setCooldown(player, spellName, current.getCooldown());
        Bukkit.getLogger().info("Casting spell: " + spellName + " for player: " + player.getName());
        player.sendMessage("Casting spell: " + spellName + " for player: " + player.getName());
    }

    @Nullable
    private static SpellElement tryParseElement(String name) {
        for (SpellElement element : SpellElement.values()) {
            if (element.name().equalsIgnoreCase(name)) {
                return element;
            }
        }
        return null;
    }





    }



