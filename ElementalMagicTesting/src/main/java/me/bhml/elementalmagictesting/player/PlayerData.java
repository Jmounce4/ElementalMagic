package me.bhml.elementalmagictesting.player;

import me.bhml.elementalmagictesting.skills.SkillProgress;
import me.bhml.elementalmagictesting.skills.SkillType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.UUID;
public class PlayerData {
    private final UUID playerId;

    // --- Targeting Preferences ---
    private boolean allowFriendlyFire = false;
    private boolean canHitPets = false;
    private boolean canHitAnimals = true;
    private boolean canHitVillagers = false;

    // --- Relationships ---
    private final Set<UUID> partyMembers = new HashSet<>();

    // --- Spellcasting ---
    private double mana = 100.0;
    private double maxMana = 100.0;

    private int level = 1;
    private double xp = 0.0;


    private final Map<SkillType, SkillProgress> skillProgressMap = new EnumMap<>(SkillType.class);

    public PlayerData(UUID playerId) {
        this.playerId = playerId;
        for (SkillType skill : SkillType.values()) {
            skillProgressMap.put(skill, new SkillProgress(1, 0.0)); // Start at level 1 with 0 XP
        }

    }

    public UUID getPlayerId() {
        return playerId;
    }



    // Getters/Setters for preferences
    public boolean canHitPets() { return canHitPets; }
    public void setCanHitPets(boolean value) { this.canHitPets = value; }

    public boolean canHitAnimals() { return canHitAnimals; }
    public void setCanHitAnimals(boolean value) { this.canHitAnimals = value; }

    public boolean canHitVillagers() { return canHitVillagers; }
    public void setCanHitVillagers(boolean value) { this.canHitVillagers = value; }

    public boolean allowsFriendlyFire() { return allowFriendlyFire; }
    public void setAllowFriendlyFire(boolean value) { this.allowFriendlyFire = value; }

    // Party logic
    public void addPartyMember(UUID uuid) { partyMembers.add(uuid); }
    public void removePartyMember(UUID uuid) { partyMembers.remove(uuid); }
    public boolean isInPartyWith(UUID uuid) { return partyMembers.contains(uuid); }

    public Set<UUID> getPartyMembers() {
        return new HashSet<>(partyMembers); // Return a copy to avoid direct mutation
    }


    // Mana
    public double getMana() { return mana; }
    public void setMana(double mana) { this.mana = Math.min(mana, maxMana); }
    public double getMaxMana() { return maxMana; }

    // XP & Level
    public double getXp() { return xp; }
    public void setXp(double xp) { this.xp = xp; }

    public void addXp(SkillType skillType, double amount) {
        SkillProgress progress = skillProgressMap.get(skillType);
        if (progress != null) {
            int oldLevel = progress.getLevel();
            progress.addXp(amount);
            int newLevel = progress.getLevel();

            if (newLevel > oldLevel) {
                // Check for new spell unlocks, messages, etc.
                handleLevelUp(skillType, newLevel);
            }

            Bukkit.getLogger().info("Gained " + amount + " XP in " + skillType +
                    " | Total XP now: " + progress.getXp());
        }

    }


    private void handleLevelUp(SkillType skill, int level) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            player.sendMessage(ChatColor.GOLD + "Your " + skill.name() + " skill leveled up to " + level + "!");

            // Example: unlock a new spell
            if (skill == SkillType.FIRE && level == 5) {
                unlockSpell("fire_nova");
                player.sendMessage(ChatColor.GREEN + "You unlocked Fire Nova!");
            }
        }
    }


    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public SkillProgress getSkillProgress(SkillType type) {
        return skillProgressMap.get(type);
    }

    public Map<SkillType, SkillProgress> getAllSkillProgress() {
        return new EnumMap<>(skillProgressMap);
    }



    // --- Spell Unlocks & Loadout ---
    private final Set<String> unlockedSpells = new HashSet<>();
    private final List<String> loadoutSpells = new ArrayList<>();

    // Unlocked Spells
    public void unlockSpell(String spellKey) {
        unlockedSpells.add(spellKey.toLowerCase()); // normalize keys
    }

    public boolean hasUnlocked(String spellKey) {
        return unlockedSpells.contains(spellKey.toLowerCase());
    }

    public Set<String> getUnlockedSpells() {
        return new HashSet<>(unlockedSpells); // avoid external mutation
    }

    public void setUnlockedSpells(Set<String> spells) {
        unlockedSpells.clear();
        for (String s : spells) {
            unlockedSpells.add(s.toLowerCase());
        }
    }

    // Loadout
    public List<String> getLoadoutSpells() {
        return new ArrayList<>(loadoutSpells);
    }

    public void setLoadoutSpells(List<String> spells) {
        loadoutSpells.clear();
        loadoutSpells.addAll(spells.stream().map(String::toLowerCase).toList());
    }

    private boolean starterChosen = false;

    public boolean hasChosenStarter() {
        return starterChosen;
    }
    public void setStarterChosen(boolean value) {
        this.starterChosen = value;
    }


}
