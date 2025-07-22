package me.bhml.elementalmagictesting.player;

import me.bhml.elementalmagictesting.player.PlayerData;


import me.bhml.elementalmagictesting.skills.SkillProgress;
import me.bhml.elementalmagictesting.skills.SkillType;
import me.bhml.elementalmagictesting.spells.SpellElement;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class PlayerDataManager {

    private static final Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    public static void createData(Player player) {
        UUID uuid = player.getUniqueId();
        playerDataMap.put(uuid, new PlayerData(uuid));
    }

    public static PlayerData get(Player player) {
        return get(player.getUniqueId());
    }

    public static PlayerData get(UUID uuid) {
        PlayerData data = playerDataMap.get(uuid);
        if(data == null){
            Bukkit.getLogger().warning("Tried to get PlayerData for " + uuid + " but none exists.");
        }
        return data;
    }

    public static boolean hasData(UUID uuid) {
        return playerDataMap.containsKey(uuid);
    }

    public static void remove(UUID uuid) {
        playerDataMap.remove(uuid);
    }

    public static Collection<PlayerData> getAllData() {
        return playerDataMap.values();
    }



    public static void saveData(UUID uuid) {
        PlayerData data = playerDataMap.get(uuid);
        if (data == null) return;

        File file = new File(Bukkit.getPluginManager().getPlugin("ElementalMagicTesting").getDataFolder(), "playerdata/" + uuid + ".yml");
        file.getParentFile().mkdirs(); // Ensure directory exists

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        // Preferences
        config.set("preferences.allowFriendlyFire", data.allowsFriendlyFire());
        config.set("preferences.canHitPets", data.canHitPets());
        config.set("preferences.canHitAnimals", data.canHitAnimals());
        config.set("preferences.canHitVillagers", data.canHitVillagers());

        // Party
        List<String> partyList = data.getPartyMembers().stream().map(UUID::toString).toList();
        config.set("party.members", partyList);

        // Mana
        config.set("mana.current", data.getMana());
        config.set("mana.max", data.getMaxMana());

        // XP & Level
        //config.set("progress.level", data.getLevel());
        //config.set("progress.xp", data.getXp());

        for (Map.Entry<SkillType, SkillProgress> entry : data.getAllSkillProgress().entrySet()) {
            String path = "skills." + entry.getKey().name().toLowerCase();
            config.set(path + ".level", entry.getValue().getLevel());
            config.set(path + ".xp", entry.getValue().getXp());
        }

        config.set("starterChosen", data.hasChosenStarter());

        // Unlocked Spells
        List<String> unlocked = data.getUnlockedSpells().stream().toList();
        config.set("spells.unlocked", unlocked);

        // Loadout Spells
        List<String> loadout = data.getLoadoutSpells();
        config.set("spells.loadout", loadout);

        // Can unlock new element
        config.set("pending_element_unlocks", data.getPendingElementUnlocks());


        //Elements unlocked
        List<String> unlockedElements = data.getUnlockedElements().stream()
                .map(SpellElement::name)
                .toList();
        config.set("elements.unlocked", unlockedElements);


        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    public static void loadData(Player player) {
        UUID uuid = player.getUniqueId();
        File file = new File(Bukkit.getPluginManager().getPlugin("ElementalMagicTesting").getDataFolder(), "playerdata/" + uuid + ".yml");

        PlayerData data = new PlayerData(uuid);

        if (file.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);

            // Load allowFriendlyFire
            boolean allowFF = config.getBoolean("preferences.allowFriendlyFire", false);
            data.setAllowFriendlyFire(allowFF);
            Bukkit.getLogger().info("Loaded allowFriendlyFire = " + allowFF + " for player " + player.getName());

            // Load canHitPets
            boolean canHitPets = config.getBoolean("preferences.canHitPets", false);
            data.setCanHitPets(canHitPets);
            Bukkit.getLogger().info("Loaded canHitPets = " + canHitPets + " for player " + player.getName());

            // Load canHitAnimals
            boolean canHitAnimals = config.getBoolean("preferences.canHitAnimals", false);
            data.setCanHitAnimals(canHitAnimals);
            Bukkit.getLogger().info("Loaded canHitAnimals = " + canHitAnimals + " for player " + player.getName());

            // Load canHitVillagers
            boolean canHitVillagers = config.getBoolean("preferences.canHitVillagers", false);
            data.setCanHitVillagers(canHitVillagers);
            Bukkit.getLogger().info("Loaded canHitVillagers = " + canHitVillagers + " for player " + player.getName());

            // Load party members
            List<String> partyList = config.getStringList("party.members");
            for (String id : partyList) {
                try {
                    data.addPartyMember(UUID.fromString(id));
                } catch (IllegalArgumentException ignored) {}
            }

            // Load mana, xp, level
            double mana = config.getDouble("mana.current", 100.0);
            data.setMana(mana);
            Bukkit.getLogger().info("Loaded mana = " + mana + " for player " + player.getName());


            /*Not used, was placeholder @Deprecated
            double xp = config.getDouble("progress.xp", 0.0);
            data.setXp(xp);
            Bukkit.getLogger().info("Loaded xp = " + xp + " for player " + player.getName());

            int level = config.getInt("progress.level", 1);
            data.setLevel(level);
            Bukkit.getLogger().info("Loaded level = " + level + " for player " + player.getName());


             */

            for (SkillType skill : SkillType.values()) {
                String path = "skills." + skill.name().toLowerCase();
                int skillLVL = config.getInt(path + ".level", 1);
                double skillXP = config.getDouble(path + ".xp", 0.0);
                data.getSkillProgress(skill).setLevel(skillLVL);
                data.getSkillProgress(skill).setXp(skillXP);
                Bukkit.getLogger().info("Loaded skill " + skill.name() + ": level=" + skillLVL + ", xp=" + skillXP + " for player " + player.getName());
            }


            boolean starter = config.getBoolean("starterChosen", false);
            data.setStarterChosen(starter);

            // Unlocked Spells
            List<String> unlocked = config.getStringList("spells.unlocked");
            data.setUnlockedSpells(new HashSet<>(unlocked));

            // Loadout Spells
            List<String> loadout = config.getStringList("spells.loadout");
            data.setLoadoutSpells(loadout);

            Bukkit.getLogger().info("Loaded unlocked spells: " + unlocked + " for " + player.getName());
            Bukkit.getLogger().info("Loaded loadout spells: " + loadout + " for " + player.getName());


            //Can unlock element
            int pendingUnlocks = config.getInt("pending_element_unlocks", 0);
            data.setPendingElementUnlocks(pendingUnlocks);


            //Unlocked Elements
            List<String> elementStrings = config.getStringList("elements.unlocked");
            Set<SpellElement> unlockedElements = elementStrings.stream()
                    .map(SpellElement::valueOf)
                    .collect(Collectors.toSet());
            data.setUnlockedElements(unlockedElements);


        }

        playerDataMap.put(uuid, data);
    }



}
