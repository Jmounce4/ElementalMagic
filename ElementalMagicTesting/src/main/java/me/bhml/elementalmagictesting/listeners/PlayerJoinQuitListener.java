package me.bhml.elementalmagictesting.listeners;

import me.bhml.elementalmagictesting.player.PlayerDataManager;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.entity.Player;

public class PlayerJoinQuitListener implements Listener{


    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!PlayerDataManager.hasData(player.getUniqueId())) {
            PlayerDataManager.createData(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerDataManager.remove(player.getUniqueId());
        // TODO: Save data if persistent saving is implemented
    }




}
