package me.bhml.elementalmagictesting.listeners;

import me.bhml.elementalmagictesting.ElementalMagicTesting;
import me.bhml.elementalmagictesting.gui.StarterElementSelectionGUI;
import me.bhml.elementalmagictesting.player.PlayerData;
import me.bhml.elementalmagictesting.player.PlayerDataManager;
import me.bhml.elementalmagictesting.spells.PlayerSpellTracker;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import static me.bhml.elementalmagictesting.spells.PlayerSpellTracker.remove;

public class PlayerJoinQuitListener implements Listener{


    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerDataManager.loadData(player);


        // Show element selection GUI if new
        PlayerData data = PlayerDataManager.get(player);
        if (!data.hasChosenStarter()) {
            Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(ElementalMagicTesting.class), () -> {
                StarterElementSelectionGUI.open(player); // (you’ll implement this GUI)
            }, 20L); // Delay slightly to avoid conflicts on join
        }


    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerDataManager.saveData(event.getPlayer().getUniqueId());
        PlayerDataManager.remove(player.getUniqueId());
        PlayerSpellTracker.remove(player);

    }




}
