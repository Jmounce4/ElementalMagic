package me.bhml.elementalmagictesting;

import me.bhml.elementalmagictesting.gui.StarterElementSelectionGUI;
import me.bhml.elementalmagictesting.items.GiveElementalCoreCommand;
import me.bhml.elementalmagictesting.items.ItemManager;
import me.bhml.elementalmagictesting.items.ItemUtils;
import me.bhml.elementalmagictesting.listeners.ElementalCoreListener;
import me.bhml.elementalmagictesting.listeners.MobSpawningListener;
import me.bhml.elementalmagictesting.listeners.PlayerJoinQuitListener;
import me.bhml.elementalmagictesting.player.PlayerData;
import me.bhml.elementalmagictesting.player.PlayerDataManager;
import me.bhml.elementalmagictesting.spells.SpellRegistry;
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

    private static ElementalMagicTesting instance;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance=this;

        ItemUtils.init(this);
        ItemManager.init(this);



        getServer().getPluginManager().registerEvents(new ElementalCoreListener(), this);


        getServer().getPluginManager().registerEvents(new PlayerJoinQuitListener(), this);

        getServer().getPluginManager().registerEvents(new StarterElementSelectionGUI(), this);
        SpellRegistry.registerAll();
        getServer().getPluginManager().registerEvents(new MobSpawningListener(), this);




        //Commands
        getCommand("giveelementalcore").setExecutor(new GiveElementalCoreCommand());

        getCommand("castspell").setExecutor(new SpellTestCommand());
        getLogger().info("ElementalMagic enabled.");


        /*PlayerSpellTracker.setAvailableSpells(List.of(
                new FireballSpell(),
                new AirGustSpell(),
                new LightningSpell(),
                new LiquidLance(),
                new Rumble()
        ));*/

        // 🛠️ Load data for players already online (important after /reload)
        for (Player player : Bukkit.getOnlinePlayers()) {
            getLogger().info("Loading player data for " + player.getName() + " (already online)");
            PlayerDataManager.loadData(player);
            PlayerSpellTracker.get(player);
        }


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        for (PlayerData data : PlayerDataManager.getAllData()) {
            PlayerDataManager.saveData(data.getPlayerId());
        }
    }

    public static ElementalMagicTesting getInstance() {
        return instance;
    }
}
