package me.bhml.elementalmagictesting;

import me.bhml.elementalmagictesting.items.GiveElementalCoreCommand;
import me.bhml.elementalmagictesting.items.ItemManager;
import me.bhml.elementalmagictesting.items.ItemUtils;
import me.bhml.elementalmagictesting.listeners.ElementalCoreListener;
import me.bhml.elementalmagictesting.player.PlayerDataManager;
import me.bhml.elementalmagictesting.spells.air.AirGustSpell;
import me.bhml.elementalmagictesting.spells.earth.Rumble;
import me.bhml.elementalmagictesting.spells.fire.FireballSpell;
import me.bhml.elementalmagictesting.spells.lightning.LightningSpell;
import me.bhml.elementalmagictesting.spells.PlayerSpellTracker;
import me.bhml.elementalmagictesting.spells.SpellTestCommand;
import me.bhml.elementalmagictesting.spells.water.LiquidLance;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class ElementalMagicTesting extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        ItemUtils.init(this);
        ItemManager.init(this);

        getServer().getPluginManager().registerEvents(new ElementalCoreListener(), this);

        //Ensure PlayerData for all players online
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!PlayerDataManager.hasData(player.getUniqueId())) {
                PlayerDataManager.createData(player);
            }
        }

        //Commands
        getCommand("giveelementalcore").setExecutor(new GiveElementalCoreCommand());

        getCommand("castspell").setExecutor(new SpellTestCommand());
        getLogger().info("ElementalMagic enabled.");


        PlayerSpellTracker.setAvailableSpells(List.of(
                new FireballSpell(),
                new AirGustSpell(),
                new LightningSpell(),
                new LiquidLance(),
                new Rumble()
        ));


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
